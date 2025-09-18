package engine.core;

import dto.engine.ExecutionResultDTO;
import dto.engine.ExecutionStatisticsDTO;
import dto.engine.ProgramDTO;
import engine.exception.LabelNotExist;
import engine.generated.SInstruction;
import engine.generated.SProgram;
import engine.utils.ProgramUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static engine.utils.ProgramUtils.*;

public class ProgramEngine implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final String programName;
    private final Map<String, Integer> originalContextMap = new HashMap<>();
    private final List<Map<String, Integer>> contextMapsByExpandLevel = new ArrayList<>();
    private final List<Instruction> originalInstructions;
    private final List<List<Instruction>> instructionExpansionLevels = new ArrayList<>();
    private final Set<String> originalLabels;
    private final List<Set<String>> labelsByExpandLevel = new ArrayList<>();
    private final List<Integer> cyclesPerExpandLevel = new ArrayList<>();
    private final List<ExecutionStatistics> executionStatisticsList = new ArrayList<>();
    private final Map<String, Integer> extraArguments = new HashMap<>();

    // Enhanced debugger fields with proper state management:
    private boolean debugMode = false;
    private int currentDebugPC = 0;
    private final List<Map<String, Integer>> debugStateHistory = new ArrayList<>();
    private final List<Integer> debugCyclesHistory = new ArrayList<>(); // NEW: Track cycles per step
    private List<Instruction> currentDebugInstructions;
    private Map<String, Integer> currentDebugContext;
    private Map<String, Integer> debugArguments = new HashMap<>(); // NEW: Store debug arguments
    private int debugExpandLevel = 0;
    private int totalDebugCycles = 0; // NEW: Monotonic cycle counter

    public ProgramEngine(SProgram program) throws LabelNotExist {
        this.programName = program.getName();

        originalInstructions = program.getSInstructions().getSInstruction().stream()
                .map(Instruction::createInstruction)
                .collect(Collectors.toList());

        originalLabels = program.getSInstructions().getSInstruction().stream()
                .map(SInstruction::getSLabel)
                .filter(Objects::nonNull)
                .filter(label -> !label.isBlank())
                .map(String::trim)
                .filter(label -> label.startsWith("L"))
                .collect(Collectors.toSet());
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
            for (String argName : instruction.getArgs().values()) {
                if (!originalContextMap.containsKey(argName)) {
                    if (isLabelArgument(argName) && !validateLabel(argName)) {
                        throw new LabelNotExist(
                                instruction.getClass().getSimpleName(),
                                instruction_index + 1,
                                argName);

                    } else if (!ProgramUtils.isNumber(argName)) {
                        originalContextMap.put(argName.trim(), 0);
                    }
                }
                if (argName.equals(ProgramUtils.EXIT_LABEL_NAME)) {
                    originalContextMap.put(argName, originalInstructions.size()); // EXIT label is set to the end of the program
                    originalLabels.add(argName);
                }
            }
        }
        fillUnusedLabels();
    }

    private boolean isLabelArgument(String argName) {
        return argName.startsWith("L");
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

    public void run(int expandLevel, Map<String, Integer> arguments) {
        clearPreviousRunData();
        ExecutionStatistics exStats = new ExecutionStatistics(executionStatisticsList.size() + 1,
                expandLevel, arguments);
        originalContextMap.putAll(arguments);
        expand(expandLevel);
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

    private void clearPreviousRunData() {
        extraArguments.clear();
        contextMapsByExpandLevel.clear();
        contextMapsByExpandLevel.add(new HashMap<>(originalContextMap));
        instructionExpansionLevels.clear();
        instructionExpansionLevels.add(originalInstructions);
        labelsByExpandLevel.clear();
        labelsByExpandLevel.add(new HashSet<>(originalLabels));
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

    public ProgramDTO toDTO(int expandLevel) {
        expand(expandLevel);
        Map<String, Integer> argsMap = extractArguments(contextMapsByExpandLevel.get(expandLevel));
        argsMap.putAll(extraArguments);
        List<Instruction> instructionsAtLevel = instructionExpansionLevels.get(expandLevel);
        return new ProgramDTO(
                programName,
                argsMap.keySet(),
                extractLabels(labelsByExpandLevel, expandLevel),
                IntStream.range(0,instructionsAtLevel.size())
                        .mapToObj(i -> instructionsAtLevel.get(i).toDTO(i))
                        .toList()
        );
    }

    public ExecutionResultDTO toExecutionResultDTO(int expandLevel) {
        if (executionStatisticsList.isEmpty()) {
            throw new IllegalStateException("No execution has been run yet.");
        }
        ExecutionStatisticsDTO executionStatisticsDTO = executionStatisticsList.getLast().toDTO();
        return new ExecutionResultDTO(executionStatisticsDTO.result(),
                executionStatisticsDTO.arguments(),
                extractWorkVars(contextMapsByExpandLevel.get(expandLevel)),
                contextMapsByExpandLevel.get(expandLevel).get(ProgramUtils.OUTPUT_NAME),
                executionStatisticsDTO.cyclesUsed()
        );
    }

    public List<ExecutionStatisticsDTO> getAllExecutionStatistics() {
        return executionStatisticsList.stream()
                .map(ExecutionStatistics::toDTO)
                .toList();
    }

    public Set<String> getProgramArgsNames() {
        return extractArguments(originalContextMap).keySet();
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

    // FIXED: Enhanced debugger methods with proper state management

    public void startDebugSession(int expandLevel, Map<String, Integer> arguments) {
        clearPreviousRunData();
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

    public ExecutionResultDTO debugStep() {
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

    public ExecutionResultDTO debugStepBackward() {
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


        System.out.println("Debug stepped backward to PC=" + currentDebugPC +
                ", total cycles remain at: " + totalDebugCycles);
        return getCurrentDebugState();
    }

    public ExecutionResultDTO debugResume() {
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

    private ExecutionResultDTO getCurrentDebugState() {
        if (!debugMode || currentDebugContext == null) {
            throw new IllegalStateException("No debug session active");
        }

        int result = currentDebugContext.get(ProgramUtils.OUTPUT_NAME);
        Map<String, Integer> arguments = new HashMap<>(debugArguments); // Use stored debug arguments
        Map<String, Integer> workVars = ProgramUtils.extractWorkVars(currentDebugContext);

        return new ExecutionResultDTO(result, arguments, workVars, result, totalDebugCycles);
    }
}