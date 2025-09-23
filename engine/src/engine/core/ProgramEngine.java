package engine.core;

import dto.engine.ExecutionResultDTO;
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
    private final List<Integer> cyclesPerExpandLevel = new ArrayList<>();
    private final List<ExecutionStatistics> executionStatisticsList = new ArrayList<>();
    private Map<String, ProgramEngine> allFunctionsInMain;
    private @NotNull Set<String> originalLabels;
    private SFunction originalSFunction = null;
    private String funcName;

    // Enhanced debugger fields with proper state management:
    private boolean debugMode = false;
    private int currentDebugPC = 0;
    private final List<Map<String, Integer>> debugStateHistory = new ArrayList<>();
    private final List<Integer> debugCyclesHistory = new ArrayList<>(); // Track cycles per step
    private List<Instruction> currentDebugInstructions;
    private Map<String, Integer> currentDebugContext;
    private final Map<String, Integer> debugArguments = new HashMap<>(); // Store debug arguments
    private int debugExpandLevel = 0;
    private int totalDebugCycles = 0; // Monotonic cycle counter

    public ProgramEngine(@NotNull SProgram program) throws LabelNotExist {
        this.programName = program.getName();

        allFunctionsInMain = buildFunctions(program);

        allFunctionsInMain.forEach((name, function) -> function.finishInitFunction(allFunctionsInMain));

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
                        throw new RuntimeException("Error initializing function: " + func.getName(), e); // TODO - better error handling
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

    public Integer getOutput() {
        return contextMapsByExpandLevel.getFirst().get(ProgramUtils.OUTPUT_NAME);
    }

    private void finishInitialization() throws LabelNotExist {
        instructionExpansionLevels.add(originalInstructions);
        labelsByExpandLevel.add(originalLabels);
        initializeContextMap();
        contextMapsByExpandLevel.add(new HashMap<>(originalContextMap));
        cyclesPerExpandLevel.add(calcTotalCycles(0));
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
                        if (ProgramUtils.isLabel(argValueName) && !validateLabel(argValueName)) {
                            throw new LabelNotExist(
                                    instruction.getClass().getSimpleName(),
                                    instruction_index + 1,
                                    argValueName);

                        } else if (ProgramUtils.isSingleValidArgument(argValueName)) {
                            originalContextMap.put(argValueName.trim(), 0);
                        }
                    }
                    if (argValueName.equals(ProgramUtils.EXIT_LABEL_NAME)) {
                        originalContextMap.put(argValueName, originalInstructions.size()); // EXIT label is set to the end of the program
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
                originalContextMap.put(label.trim(), -1); // Initialize unused labels with -1 to indicate they are not used
            }
        }
    }

    private boolean validateLabel(String labelName) {
        return originalLabels.contains(labelName);
    }

    public void run(int expandLevel, @NotNull Map<String, Integer> arguments) {
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
        executionStatisticsList.add(exStats);
    }

    public void run(@NotNull Map<String, Integer> arguments) {
        run(0, arguments);
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
                cyclesPerExpandLevel.add(calcTotalCycles(currLevel));
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

    public @NotNull ExecutionResultDTO toExecutionResultDTO(int expandLevel) {
        if (executionStatisticsList.isEmpty()) {
            throw new IllegalStateException("No execution has been run yet.");
        }
        ExecutionStatisticsDTO executionStatisticsDTO = executionStatisticsList.getLast().toDTO();
        return new ExecutionResultDTO(executionStatisticsDTO.result(),
                executionStatisticsDTO.arguments(),
                extractSortedWorkVars(contextMapsByExpandLevel.get(expandLevel)),
                contextMapsByExpandLevel.get(expandLevel).get(ProgramUtils.OUTPUT_NAME),
                executionStatisticsDTO.cyclesUsed()
        );
    }

    public @NotNull List<ExecutionStatisticsDTO> getAllExecutionStatistics() {
        return executionStatisticsList.stream()
                .map(ExecutionStatistics::toDTO)
                .toList();
    }

    public @NotNull Set<String> getProgramArgsNames() {
        return ProgramUtils.extractSortedArguments(originalContextMap).keySet();
    }

    private int calcTotalCycles(int expandLevel) {
        return instructionExpansionLevels.get(expandLevel).stream()
                .mapToInt(Instruction::getCycles)
                .sum();
    }

    public int getTotalCycles(int expandLevel) {
        if (expandLevel < 0 || expandLevel >= cyclesPerExpandLevel.size()) {
            throw new IllegalArgumentException("Expand level out of bounds");
        }
        return cyclesPerExpandLevel.get(expandLevel);
    }

    public int getTotalCycles() {
        return calcTotalCycles(0);
    }

    public @NotNull Set<String> getAllVariablesNamesAndLabels(int expandLevel) {
        if (expandLevel < 0 || expandLevel >= labelsByExpandLevel.size()) {
            throw new IllegalArgumentException("Expand level out of bounds");
        }
        return extractAllVariableAndLabelNamesUnsorted(contextMapsByExpandLevel.get(expandLevel));
    }

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

        debugMode = true;
        debugExpandLevel = expandLevel;
        currentDebugPC = 0;
        totalDebugCycles = 0;

        // Store arguments for persistent access
        debugArguments.clear();
        debugArguments.putAll(arguments);

        // Apply arguments to original context first
        originalContextMap.putAll(arguments);

        // Expand to required level - this preserves the arguments
        expand(expandLevel);

        // Setup debug context with arguments properly applied
        currentDebugInstructions = new ArrayList<>(instructionExpansionLevels.get(expandLevel));
        currentDebugContext = new HashMap<>(contextMapsByExpandLevel.get(expandLevel));

        // Ensure arguments are preserved in debug context
        currentDebugContext.putAll(debugArguments);
        currentDebugContext.put(Instruction.ProgramCounterName, 0);

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
        currentDebugInstructions = null;
        currentDebugContext = null;
        totalDebugCycles = 0;
        currentDebugPC = 0;
    }

    public @NotNull ExecutionResultDTO debugStep() {
        if (!debugMode) {
            throw new IllegalStateException("Debug session not started");
        }

        if (currentDebugPC >= currentDebugInstructions.size()) {
            return getCurrentDebugState(); // Program finished
        }

        // Execute current instruction
        Instruction currentInstruction = currentDebugInstructions.get(currentDebugPC);
        System.out.println("Executing debug step " + currentDebugPC + ": " + currentInstruction);

        try {
            // Execute instruction
            currentInstruction.execute(currentDebugContext);

            // Add cycles from this instruction to total (monotonic increment)
            int instructionCycles = currentInstruction.getCycles();
            totalDebugCycles += instructionCycles;

            // Update PC from context
            currentDebugPC = currentDebugContext.get(Instruction.ProgramCounterName);

            // Save state after execution with accumulated cycles
            debugStateHistory.add(new HashMap<>(currentDebugContext));
            debugCyclesHistory.add(totalDebugCycles);

        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Error executing debug instruction at PC=" + currentDebugPC + ": " + e.getMessage(), e);
        }

        return getCurrentDebugState();
    }

    public @NotNull ExecutionResultDTO debugStepBackward() {
        if (!debugMode) {
            throw new IllegalStateException("Debug session not started");
        }

        if (debugStateHistory.size() <= 1) {
            // Can't go back further than initial state
            return getCurrentDebugState();
        }

        // Remove current state and restore previous
        debugStateHistory.removeLast();
        debugCyclesHistory.removeLast();

        // Restore previous state
        currentDebugContext = new HashMap<>(debugStateHistory.get(debugStateHistory.size() - 1));
        currentDebugPC = currentDebugContext.get(Instruction.ProgramCounterName);

        // Update totalDebugCycles to match the current state
        totalDebugCycles = debugCyclesHistory.get(debugCyclesHistory.size() - 1);

        System.out.println("Debug stepped backward to PC=" + currentDebugPC +
                ", cycles: " + totalDebugCycles);
        return getCurrentDebugState();
    }

    public @NotNull ExecutionResultDTO debugResume() {
        if (!debugMode) {
            throw new IllegalStateException("Debug session not started");
        }

        // Execute remaining instructions
        while (currentDebugPC < currentDebugInstructions.size()) {
            debugStep();
        }

        return getCurrentDebugState();
    }

    public void stopDebugSession() {
        debugMode = false;
        resetDebugState();
        System.out.println("Debug session stopped");
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

    private @NotNull ExecutionResultDTO getCurrentDebugState() {
        if (!debugMode || currentDebugContext == null) {
            throw new IllegalStateException("No debug session active");
        }

        int result = currentDebugContext.get(ProgramUtils.OUTPUT_NAME);
        Map<String, Integer> arguments = new HashMap<>(debugArguments); // Use stored debug arguments
        Map<String, Integer> workVars = ProgramUtils.extractSortedWorkVars(currentDebugContext);

        return new ExecutionResultDTO(result, arguments, workVars, result, totalDebugCycles);
    }

    public Map<String, Integer> getDebugWorkVariables() {
        if (!debugMode || currentDebugContext == null) {
            throw new IllegalStateException("Debug session not active");
        }

        // Reuse existing extractWorkVars logic from ProgramUtils
        return ProgramUtils.extractWorkVars(currentDebugContext);
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
        cyclesPerExpandLevel.clear();
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
}