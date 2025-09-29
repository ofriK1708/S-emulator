package engine.core;

import dto.engine.ExecutionStatisticsDTO;
import dto.engine.ProgramDTO;
import engine.core.syntheticCommand.Quote;
import engine.exception.LabelNotExist;
import engine.generated_2.SFunction;
import engine.generated_2.SInstruction;
import engine.generated_2.SInstructions;
import engine.generated_2.SProgram;
import engine.utils.ProgramUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static engine.utils.ProgramUtils.*;

public class ProgramEngine implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final String programName;
    private final Map<String, Integer> originalContextMap = new HashMap<>();
    private final List<Map<String, Integer>> contextMapsByExpandLevel = new ArrayList<>();
    private final List<Quote> uninitializedQuotes = new ArrayList<>();
    private final List<List<Instruction>> instructionExpansionLevels = new ArrayList<>();
    private @NotNull List<Instruction> originalInstructions;
    private final List<Set<String>> labelsByExpandLevel = new ArrayList<>();
    private final List<ExecutionStatistics> executionStatisticsList = new ArrayList<>();
    private Map<String, ProgramEngine> allFunctionsInMain;
    private @NotNull Set<String> originalLabels;
    private @Nullable SFunction originalSFunction = null;
    private String funcName;

    // Enhanced debugger fields with proper state management:
    private boolean debugMode = false;
    private int currentDebugPC = 0;
    private final List<Map<String, Integer>> debugStateHistory = new ArrayList<>();
    private final List<Integer> debugCyclesHistory = new ArrayList<>(); // Track cycles per step
    private List<Instruction> currentDebugInstructions = new ArrayList<>();
    private Map<String, Integer> currentDebugContext = new HashMap<>();
    private final Map<String, Integer> debugArguments = new HashMap<>(); // Store debug arguments
    private int totalDebugCycles = 0; // Monotonic cycle counter
    private @Nullable ExecutionStatistics currentDebugStatistics = null;


    public ProgramEngine(@NotNull SProgram program) throws LabelNotExist {
        this.programName = program.getName();

        if (program.getSFunctions() != null) {

            allFunctionsInMain = buildFunctions(program);

            allFunctionsInMain.forEach((name, function) -> function.finishInitFunction(allFunctionsInMain));
        }

        SInstructions sInstructions = program.getSInstructions();

        originalInstructions = buildInstructions(sInstructions);

        originalLabels = buildLabels(sInstructions);

        finishInitialization();

    }

    public ProgramEngine(@NotNull SFunction function) throws LabelNotExist {
        this.programName = function.getName();

        funcName = function.getUserString();

        SInstructions sInstructions = function.getSInstructions();

        originalInstructions = buildInstructions(sInstructions);

        originalLabels = buildLabels(sInstructions);

        finishInitialization();

        originalSFunction = function;
    }

    private static @NotNull Set<String> buildLabels(@NotNull SInstructions sInstructions) {
        return sInstructions.getSInstruction().stream()
                .map(SInstruction::getSLabel)
                .filter(Objects::nonNull)
                .filter(label -> !label.isBlank())
                .map(String::trim)
                .filter(label -> label.startsWith("L"))
                .collect(Collectors.toSet());
    }

    private @NotNull Map<String, ProgramEngine> buildFunctions(@NotNull SProgram program) {
        return program.getSFunctions().getSFunction().stream()
                .map(func -> {
                    try {
                        return new ProgramEngine(func);
                    } catch (LabelNotExist e) {
                        throw new RuntimeException("Error initializing function: " + func.getName(), e); // TODO -
                        // better error handling
                    }
                })
                .collect(Collectors.toMap(ProgramEngine::getProgramName, funcEngine -> funcEngine));
    }

    public String getProgramName() {
        return programName;
    }

    public Map<String, ProgramEngine> getAllFunctionsInMain() {
        return allFunctionsInMain;
    }

    public void addToUninitializedQuotes(Quote quote) {
        uninitializedQuotes.add(quote);
    }

    public boolean isVariableInContext(String varName) {
        return originalContextMap.containsKey(varName);
    }

    private void finishInitFunction(@NotNull Map<String, ProgramEngine> allFunctionsInMain) {
        setFunctions(allFunctionsInMain);
        for (Quote quote : uninitializedQuotes) {
            quote.initAndValidateQuote();
        }
        uninitializedQuotes.clear();
    }


    private void setFunctions(@NotNull Map<String, ProgramEngine> allFunctionsInMain) {
        this.allFunctionsInMain = allFunctionsInMain.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(programName))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private @NotNull List<Instruction> buildInstructions(@NotNull SInstructions sInstructions) {
        return sInstructions.getSInstruction().stream()
                .map(sInstruction -> Instruction.createInstruction(sInstruction, this))
                .collect(Collectors.toList());
    }

    /**
     * Get the output value after execution at a specific expansion level.
     * used for debugging program, because output is changing during execution
     *
     * @param expandLevel the expansion level to get the output from
     * @return the output value during execution at the specified expansion level
     */
    public Integer getOutput(int expandLevel) {
        return contextMapsByExpandLevel.get(expandLevel).get(ProgramUtils.OUTPUT_NAME);
    }

    /**
     * Get the output value after execution at the last expansion level.
     * used for normal program execution, because output is final after execution
     *
     * @return the final output value after execution at the last expansion level
     */
    public Integer getOutput() {
        return contextMapsByExpandLevel.getLast().get(ProgramUtils.OUTPUT_NAME);
    }

    private void finishInitialization() throws LabelNotExist {
        instructionExpansionLevels.add(originalInstructions);
        labelsByExpandLevel.add(originalLabels);
        initializeContextMap();
        contextMapsByExpandLevel.add(new HashMap<>(originalContextMap));
    }


    private void initializeContextMap() throws LabelNotExist {
        originalContextMap.clear();
        originalContextMap.put(ProgramUtils.OUTPUT_NAME, 0);
        originalContextMap.put(Instruction.ProgramCounterName, 0); // Program Counter
        for (int instruction_index = 0; instruction_index < originalInstructions.size(); instruction_index++) {
            Instruction instruction = originalInstructions.get(instruction_index);
            originalContextMap.put(instruction.getMainVarName().trim(), 0);
            if (!instruction.getLabel().isEmpty()) {
                originalContextMap.put(instruction.getLabel().trim(), instruction_index);
            }
            for (Map.Entry<String, String> argsEntry : instruction.getArgs().entrySet()) {
                if (argsEntry.getKey().equals(Quote.functionArgumentsArgumentName)) {
                    ProgramUtils.initAllVariablesFromQuoteArguments(argsEntry.getValue(), originalContextMap);
                } else {
                    String argValueName = argsEntry.getValue().trim();
                    if (!originalContextMap.containsKey(argValueName)) {
                        if (ProgramUtils.isNumberedLabel(argValueName) && !validateLabel(argValueName)) {
                            throw new LabelNotExist(
                                    instruction.getClass().getSimpleName(),
                                    instruction_index + 1,
                                    argValueName);

                        } else if (ProgramUtils.isSingleValidArgument(argValueName)) {
                            originalContextMap.put(argValueName.trim(), 0);
                        }
                    }
                    if (argValueName.equals(ProgramUtils.EXIT_LABEL_NAME)) {
                        originalContextMap.put(argValueName, originalInstructions.size()); // EXIT label is set to
                        // the end of the program
                        originalLabels.add(argValueName);
                    }
                }
            }
        }
        fillUnusedLabels();
    }

    private void fillUnusedLabels() {
        for (String label : originalLabels) {
            if (!originalContextMap.containsKey(label)) {
                originalContextMap.put(label.trim(), -1); // Initialize unused labels with -1 to indicate they are
                // not used
            }
        }
    }

    private boolean validateLabel(String labelName) {
        return originalLabels.contains(labelName);
    }

    public void run(int expandLevel, @NotNull Map<String, Integer> arguments, boolean saveToStats) {
        if (expandLevel < contextMapsByExpandLevel.size()) {
            clearPreviousRunData(expandLevel);
        }
        ExecutionStatistics exStats = new ExecutionStatistics(executionStatisticsList.size() + 1,
                expandLevel, arguments);
        expand(expandLevel);
        contextMapsByExpandLevel.get(expandLevel).putAll(arguments);
        List<Instruction> executedInstructions = instructionExpansionLevels.get(expandLevel);
        Map<String, Integer> executedContextMap = contextMapsByExpandLevel.get(expandLevel);
        while (executedContextMap.get(Instruction.ProgramCounterName) < executedInstructions.size()) {
            int currentPC = executedContextMap.get(Instruction.ProgramCounterName);
            Instruction instruction = executedInstructions.get(currentPC);
            try {
                instruction.execute(executedContextMap);
                exStats.incrementCycles(instruction.getCycles());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Error executing instruction at PC=" + currentPC + ": " + e.getMessage(), e);
            }
        }
        exStats.setY(executedContextMap.get(ProgramUtils.OUTPUT_NAME));
        if (saveToStats) {
            executionStatisticsList.add(exStats);
        }
    }

    public void run(@NotNull Map<String, Integer> arguments, boolean saveToStats) {
        run(0, arguments, saveToStats);
    }

    private void clearPreviousRunData(int expandLevel) {
        Map<String, Integer> currentContextMap = contextMapsByExpandLevel.get(expandLevel);
        for (Map.Entry<String, Integer> entry : currentContextMap.entrySet()) {
            if (ProgramUtils.isVariable(entry.getKey())) {
                entry.setValue(0);
            }
        }
        currentContextMap.put(Instruction.ProgramCounterName, 0); // Reset PC
    }

    private void expand(int level) {
        if (level > 0) {
            for (int currLevel = instructionExpansionLevels.size(); currLevel <= level; currLevel++) {
                List<Instruction> tempExpanded = new ArrayList<>();
                List<Instruction> previouslyExpanded = instructionExpansionLevels.getLast();
                Map<String, Integer> latestContextMap = new HashMap<>(contextMapsByExpandLevel.getLast());
                for (int i = 0; i < previouslyExpanded.size(); i++) {
                    Instruction instruction = previouslyExpanded.get(i);
                    List<Instruction> furtherExpanded = instruction.expand(latestContextMap, i);
                    tempExpanded.addAll(furtherExpanded);
                }
                instructionExpansionLevels.add(tempExpanded);
                contextMapsByExpandLevel.add(latestContextMap);
                updateLabelsAfterExpanding();
            }
        }
    }

    private void updateLabelsAfterExpanding() {
        List<Instruction> LatestExpanded = instructionExpansionLevels.getLast();
        Map<String, Integer> latestContextMap = contextMapsByExpandLevel.getLast();
        Set<String> latestLabels = new HashSet<>(labelsByExpandLevel.getLast());
        // update context map with new labels and their indices
        LatestExpanded.stream()
                .filter(instr -> !instr.getLabel().isBlank())
                .forEach(instr -> latestContextMap.put(instr.getLabel(), LatestExpanded.indexOf(instr)));
        // update EXIT label to point to the end of the expanded program
        if (latestLabels.contains(EXIT_LABEL_NAME)) {
            latestContextMap.put(EXIT_LABEL_NAME, LatestExpanded.size());
        }
        // add any new labels introduced during expansion
        latestContextMap.keySet()
                .stream()
                .filter(var -> var.startsWith("L"))
                .forEach(latestLabels::add);
        labelsByExpandLevel.add(latestLabels);
    }

    public int getMaxExpandLevel() {
        return ProgramUtils.getMaxExpandLevel(originalInstructions);
    }

    public @NotNull ProgramDTO toDTO(int expandLevel) {
        expand(expandLevel);
        Map<String, Integer> argsMap = ProgramUtils.extractSortedArguments(contextMapsByExpandLevel.get(expandLevel));
        List<Instruction> instructionsAtLevel = instructionExpansionLevels.get(expandLevel);
        return new ProgramDTO(
                programName,
                argsMap.keySet(),
                extractLabels(labelsByExpandLevel, expandLevel),
                java.util.stream.IntStream.range(0, instructionsAtLevel.size())
                        .mapToObj(i -> instructionsAtLevel.get(i).toDTO(i))
                        .toList()
        );
    }

    public @NotNull List<ExecutionStatisticsDTO> getAllExecutionStatistics() {
        return executionStatisticsList.stream()
                .map(ExecutionStatistics::toDTO)
                .toList();
    }

    public @NotNull List<String> getSortedProgramArgsNames() {
        return ProgramUtils.extractSortedArguments(originalContextMap).keySet().stream()
                .sorted(Comparator.comparingInt(str -> Integer.parseInt(str.substring(1))))
                .collect(Collectors.toList());
    }

    private int calcTotalCycles() {
        return instructionExpansionLevels.getFirst().stream()
                .mapToInt(Instruction::getCycles)
                .sum();
    }

    public int getTotalCycles() {
        return calcTotalCycles();
    }

    public int getLastExecutionCycles() {
        if (executionStatisticsList.isEmpty()) {
            return 0;
        }
        return executionStatisticsList.getLast().toDTO().cyclesUsed();
    }

    public @NotNull Set<String> getAllVariablesNamesAndLabels(int expandLevel) {
        if (expandLevel < 0 || expandLevel >= labelsByExpandLevel.size()) {
            throw new IllegalArgumentException("Expand level out of bounds");
        }
        return extractAllVariableAndLabelNamesUnsorted(contextMapsByExpandLevel.get(expandLevel));
    }

    /**
     * Get sorted arguments at a specific expansion level.
     * Used in debugging a program, because arguments can change during execution.
     *
     * @param expandLevel the expansion level to get the arguments from
     * @return a map of argument names to their values at the specified expansion level
     */
    public @NotNull Map<String, Integer> getSortedArguments(int expandLevel) {
        return ProgramUtils.extractSortedArguments(contextMapsByExpandLevel.get(expandLevel));
    }

    /**
     * Get sorted arguments at the original expansion level (0).
     * Used in normal program execution, because arguments are fixed after execution.
     *
     * @return a map of argument names to their values at the original expansion level
     */
    public @NotNull Map<String, Integer> getSortedArguments() {
        return ProgramUtils.extractSortedArguments(originalContextMap);
    }

    public @NotNull Map<String, Integer> getSortedWorkVars(int expandLevel) {
        if (expandLevel < 0 || expandLevel >= contextMapsByExpandLevel.size()) {
            throw new IllegalArgumentException("Expand level out of bounds");
        }
        return ProgramUtils.extractSortedWorkVars(contextMapsByExpandLevel.get(expandLevel));
    }

    public String getFuncName() {
        return funcName;
    }

    // Enhanced debugger methods with proper state management

    public void startDebugSession(int expandLevel, @NotNull Map<String, Integer> arguments) {
        clearPreviousRunData(expandLevel);
        resetDebugState();

        // Expand to required level
        expand(expandLevel);

        // Apply arguments to context map at the specified expand level
        contextMapsByExpandLevel.get(expandLevel).putAll(arguments);


        // Setup debug context with arguments properly applied
        currentDebugInstructions = instructionExpansionLevels.get(expandLevel);
        currentDebugContext = contextMapsByExpandLevel.get(expandLevel);

        // CREATE DEBUG STATISTICS - same pattern as normal run
        currentDebugStatistics = new ExecutionStatistics(
                executionStatisticsList.size() + 1,  // execution number
                expandLevel,                         // expand level
                new HashMap<>(arguments)             // input arguments copy
        );
        // Note: result and cycles will be set when debug session completes

        // Save initial state with zero cycles
        debugStateHistory.add(new HashMap<>(currentDebugContext));
        debugCyclesHistory.add(0);

        System.out.println("Debug session started at expand level " + expandLevel);
        System.out.println("Debug arguments applied: " + debugArguments);
    }

    private void resetDebugState() {
        debugStateHistory.clear();
        debugCyclesHistory.clear();
        debugArguments.clear();
        currentDebugInstructions = Collections.emptyList();
        currentDebugContext = Collections.emptyMap();
        totalDebugCycles = 0;
        currentDebugPC = 0;
        currentDebugStatistics = null;
        debugMode = true;
    }

    public void debugStep() {
        if (!debugMode) {
            throw new IllegalStateException("Debug session not started");
        }

        if (currentDebugPC >= currentDebugInstructions.size()) {
            return; // Already at the end
        }

        // Execute current instruction
        Instruction currentInstruction = currentDebugInstructions.get(currentDebugPC);
        System.out.println("Executing debug step " + currentDebugPC + ": " + currentInstruction.getStringRepresentation());

        try {
            // Execute instruction
            currentInstruction.execute(currentDebugContext);

            // Add cycles from this instruction to total (monotonic increment)
            totalDebugCycles += currentInstruction.getCycles();

            // Update PC from context
            currentDebugPC = currentDebugContext.get(PC_NAME);

            // Save state after execution with accumulated cycles
            debugStateHistory.add(new HashMap<>(currentDebugContext));
            debugCyclesHistory.add(totalDebugCycles);

            if (currentDebugPC >= currentDebugInstructions.size()) {
                finalizeDebugStatistics();
            }


        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Error executing debug instruction at PC=" + currentDebugPC + ": " + e.getMessage(), e);
        }
    }

    public void debugStepBackward() {
        if (!debugMode) {
            throw new IllegalStateException("Debug session not started");
        }

        if (debugStateHistory.size() <= 1) {
            // Can't go back further than initial state
            return;
        }

        // Remove current state and restore previous
        debugStateHistory.removeLast();
        debugCyclesHistory.removeLast();

        // Restore previous state
        currentDebugContext.putAll(debugStateHistory.getLast());
        currentDebugPC = currentDebugContext.get(Instruction.ProgramCounterName);

        // Update totalDebugCycles to match the current state
        totalDebugCycles = debugCyclesHistory.getLast();

        System.out.println("Debug stepped backward to PC=" + currentDebugPC +
                ", cycles: " + totalDebugCycles);
    }

    public void debugResume() {
        if (!debugMode) {
            throw new IllegalStateException("Debug session not started");
        }

        // Execute remaining instructions
        while (currentDebugPC < currentDebugInstructions.size()) {
            debugStep(); // This will finalize statistics when program completes
        }
    }

    public void stopDebugSession() {
        if (debugMode && currentDebugStatistics != null) {
            // Finalize statistics even if session was stopped manually
            finalizeDebugStatistics();
        }

        debugMode = false;
        resetDebugState();
        System.out.println("Debug session stopped");
    }

    // New method to finalize debug statistics:
    public void finalizeDebugStatistics() {
        if (currentDebugStatistics != null && !currentDebugContext.isEmpty()) {
            // Set final result (Y value) and cycles used
            int finalResult = currentDebugContext.get(ProgramUtils.OUTPUT_NAME);
            currentDebugStatistics.setY(finalResult);
            currentDebugStatistics.incrementCycles(totalDebugCycles);

            // Add to statistics list
            executionStatisticsList.add(currentDebugStatistics);

            System.out.println("Debug statistics finalized: " +
                    ", result=" + finalResult +
                    ", cycles=" + totalDebugCycles);

            currentDebugStatistics = null; // Clear reference
        }
    }

    public int getCurrentDebugPC() {
        return debugMode ? currentDebugPC : -1;
    }

    public boolean isInDebugMode() {
        return debugMode;
    }

    public boolean isDebugFinished() {
        return debugMode && currentDebugPC >= currentDebugInstructions.size();
    }

    public int getCurrentDebugCycles() {
        if (!debugMode) {
            throw new IllegalStateException("Debug session not active");
        }

        return totalDebugCycles;
    }


    protected @NotNull List<Instruction> getFunctionInstructions() {
        return instructionExpansionLevels.getFirst();
    }

    protected void resetFunction() {
        originalContextMap.clear();
        instructionExpansionLevels.clear();
        contextMapsByExpandLevel.clear();
        labelsByExpandLevel.clear();
        currentDebugStatistics = null; // Clear statistics reference
        try {
            if (originalSFunction != null) {
                SInstructions sInstructions = originalSFunction.getSInstructions();

                originalInstructions = buildInstructions(sInstructions);

                originalLabels = buildLabels(sInstructions);

                finishInitialization();
            }
        } catch (LabelNotExist e) {
            throw new RuntimeException(e);
        }
    }

    public @NotNull Map<String, String> getAllFunctionNamesAndStrName() {
        if (allFunctionsInMain == null) {
            return Collections.emptyMap();
        }
        return allFunctionsInMain.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().getFuncName()
        ));
    }

    public @NotNull ProgramEngine getFunctionByName(@NotNull String name) {
        if (name.equals(programName)) {
            return this;
        }
        if (allFunctionsInMain == null || !allFunctionsInMain.containsKey(name)) {
            throw new IllegalArgumentException("Function " + name + " does not exist in program " + programName);
        }
        return allFunctionsInMain.get(name);
    }

    public int getLastDebugCycles() {
        if (debugCyclesHistory.isEmpty()) {
            return 0;
        }
        return debugCyclesHistory.getLast();
    }
}