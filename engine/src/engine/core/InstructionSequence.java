package engine.core;

import engine.core.syntheticCommand.Quote;
import engine.exception.LabelNotExist;
import engine.generated_2.SFunction;
import engine.generated_2.SInstruction;
import engine.generated_2.SProgram;
import engine.utils.ArchitectureType;
import engine.utils.ProgramUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static engine.utils.ProgramUtils.EXIT_LABEL_NAME;
import static engine.utils.ProgramUtils.extractAllVariableAndLabelNamesUnsorted;

/**
 * Represents the final, read-only sequence of executable instructions for a program or function.
 * <p>
 * This class is immutable. It's created once from an {@link SProgram} and an existing
 * set of functions. During creation, it crated all the instructions from all the expand levels.
 * </p>
 */
public final class InstructionSequence {
    // region Fields
    private final Map<String, Integer> originalContextMap = new HashMap<>();
    private final @NotNull List<Instruction> originalInstructions;
    private final @NotNull Set<String> originalLabels;
    private final List<List<Instruction>> instructionExpansionLevels = new ArrayList<>();
    private final List<Map<String, Integer>> contextMapsByExpandLevel = new ArrayList<>();
    private final List<Set<String>> labelsByExpandLevel = new ArrayList<>();
    private int maxExpandLevel = -1;
    private final List<ArchitectureType> minimumArchitectureTypeNeededByExpandLevel = new ArrayList<>();
    // endregion

    // region Constructors and Initialization

    /**
     * Private constructor to enforce immutability and controlled creation.
     *
     * @param originalInstructions The base list of instructions before any expansion.
     * @param originalLabels       The set of labels defined in the original instructions.
     * @throws LabelNotExist if a label referenced in the instructions does not exist.
     */
    private InstructionSequence(@NotNull List<Instruction> originalInstructions, @NotNull Set<String> originalLabels,
                                @NotNull String functionName)
            throws LabelNotExist {
        this.originalInstructions = List.copyOf(originalInstructions);
        this.originalLabels = originalLabels;
        continueInitialization();
    }

    private void continueInitialization() throws LabelNotExist {
        instructionExpansionLevels.add(originalInstructions);
        labelsByExpandLevel.add(originalLabels);
        initializeContextMap();
        contextMapsByExpandLevel.add(new HashMap<>(originalContextMap));
    }

    /**
     * Factory method to create an InstructionSequence from a raw SProgram.
     * This is the entry point for parsing and expanding instructions.
     *
     * @param sProgram        The JAXB-generated program object from the XML.
     * @param functionManager The manager containing all available functions for resolution.
     * @return A new, immutable InstructionSequence.
     */
    public static @NotNull InstructionSequence createFrom(@NotNull SProgram sProgram,
                                                          @NotNull FunctionManager functionManager)
            throws LabelNotExist {
        ParsedComponents components = parseRawInstructions(sProgram.getSInstructions().getSInstruction(),
                functionManager, sProgram.getName());

        return new InstructionSequence(components.originalInstructions(), components.originalLabels(),
                sProgram.getName());
    }

    public static @NotNull InstructionSequence createFrom(@NotNull SFunction sFunction,
                                                          @NotNull FunctionManager functionManager)
            throws LabelNotExist {
        ParsedComponents components = parseRawInstructions(sFunction.getSInstructions().getSInstruction(),
                functionManager, sFunction.getUserString());

        return new InstructionSequence(components.originalInstructions(), components.originalLabels(),
                sFunction.getUserString());
    }

    private static @NotNull ParsedComponents parseRawInstructions(@NotNull List<SInstruction> rawInstructions,
                                                                  @NotNull FunctionManager functionManager,
                                                                  @NotNull String enclosingFunctionName) {
        List<Instruction> originalInstructions = buildInstructions(rawInstructions, functionManager,
                enclosingFunctionName);
        Set<String> originalLabels = buildLabels(rawInstructions);
        return new ParsedComponents(originalInstructions, originalLabels);
    }

    private static @NotNull List<Instruction> buildInstructions(@NotNull List<SInstruction> rawInstructions,
                                                                @NotNull FunctionManager functionManager,
                                                                @NotNull String enclosingFunctionName) {
        List<Instruction> instructions = new ArrayList<>();
        for (int i = 0; i < rawInstructions.size(); i++) {
            SInstruction sInstruction = rawInstructions.get(i);
            instructions.add(Instruction.createInstruction(sInstruction, functionManager, i, enclosingFunctionName));
        }
        return instructions;
    }

    private static @NotNull Set<String> buildLabels(@NotNull List<SInstruction> rawInstructions) {
        return rawInstructions.stream()
                .map(SInstruction::getSLabel)
                .filter(Objects::nonNull)
                .filter(label -> !label.isBlank())
                .map(String::trim)
                .filter(label -> label.startsWith("L"))
                .collect(Collectors.toSet());
    }

    public void finalizeInitialization() {
        expandToMax();
        calcMinimumArchitectureForEachExpandLevel();
    }

    /**
     * Fills in any labels that were defined but not used in the original context map.
     * Unused labels are initialized with a value of -1 to indicate they are not used.
     */
    private void fillUnusedLabels() {
        for (String label : originalLabels) {
            if (!originalContextMap.containsKey(label)) {
                originalContextMap.put(label.trim(), -1); // Initialize unused labels with -1 to indicate they are
                // not used
            }
        }
    }

    private void initializeContextMap() throws LabelNotExist {
        originalContextMap.clear();
        originalContextMap.put(ProgramUtils.OUTPUT_NAME, 0);
        originalContextMap.put(ProgramUtils.PC_NAME, 0); // Program Counter
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
    // endregion

    // region Private helpers
    private void expandToMax() {
        if (maxExpandLevel == -1) {
            maxExpandLevel = ProgramUtils.getMaxExpandLevel(originalInstructions);
        }
        if (maxExpandLevel > 0) {
            for (int currLevel = instructionExpansionLevels.size(); currLevel <= maxExpandLevel; currLevel++) {
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

    private boolean validateLabel(String labelName) {
        return originalLabels.contains(labelName);
    }

    private void calcMinimumArchitectureForEachExpandLevel() {
        for (List<Instruction> instructionExpansionLevel : instructionExpansionLevels) {
            ArchitectureType minArchNeeded = ProgramUtils.calcMinimumArchitectureLevelNeeded(instructionExpansionLevel);
            minimumArchitectureTypeNeededByExpandLevel.add(minArchNeeded);
        }
    }

    private void updateLabelsAfterExpanding() {
        List<Instruction> LatestExpanded = instructionExpansionLevels.getLast();
        Map<String, Integer> latestContextMap = contextMapsByExpandLevel.getLast();
        Set<String> latestLabels = new HashSet<>(labelsByExpandLevel.getLast());

        // update context map with new labels and their indices
        for (int instructionIndex = 0; instructionIndex < LatestExpanded.size(); instructionIndex++) {
            Instruction instr = LatestExpanded.get(instructionIndex);
            if (!instr.getLabel().isBlank()) {
                latestContextMap.put(instr.getLabel(), instructionIndex);
            }
        }

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
    // endregion

    // region Public getters
    public boolean isVariableInContext(String varName) {
        return originalContextMap.containsKey(varName);
    }

    public @NotNull Map<String, Integer> getContextMapCopy(int expandLevel) {
        return new HashMap<>(contextMapsByExpandLevel.get(expandLevel));
    }

    public @NotNull Set<String> getSortedArgumentsNames() {
        return ProgramUtils.extractSortedArguments(originalContextMap).keySet();
    }

    public @NotNull List<Instruction> getInstructionsCopy(int expandLevel) {
        if (expandLevel < 0 || expandLevel >= instructionExpansionLevels.size()) {
            throw new IllegalArgumentException("Invalid expand level: " + expandLevel);
        }
        return new ArrayList<>(instructionExpansionLevels.get(expandLevel));
    }

    public @NotNull List<Instruction> getBasicInstructionsCopy() {
        return getInstructionsCopy(0);
    }

    public @NotNull Set<String> getLabels(int expandLevel) {
        return ProgramUtils.extractLabels(labelsByExpandLevel, expandLevel);
    }

    public @NotNull Set<String> getAllVariablesNames(int expandLevel, boolean includeLabels) {
        if (expandLevel < 0 || expandLevel >= labelsByExpandLevel.size()) {
            throw new IllegalArgumentException("Expand level out of bounds");
        }
        return extractAllVariableAndLabelNamesUnsorted(contextMapsByExpandLevel.get(expandLevel), includeLabels);
    }

    public @NotNull Map<String, Integer> getSortedArgumentsMap(int expandLevel) {
        return ProgramUtils.extractSortedArguments(contextMapsByExpandLevel.get(expandLevel));
    }

    public @NotNull Map<String, Integer> getSortedWorkVars(int expandLevel) {
        if (expandLevel < 0 || expandLevel >= contextMapsByExpandLevel.size()) {
            throw new IllegalArgumentException("Expand level out of bounds");
        }
        return ProgramUtils.extractSortedWorkVars(contextMapsByExpandLevel.get(expandLevel));
    }

    public int getMaxExpandLevel() {
        if (maxExpandLevel == -1) {
            maxExpandLevel = ProgramUtils.getMaxExpandLevel(originalInstructions);
        }
        return maxExpandLevel;
    }

    public int getOriginalInstructionCount() {
        return originalInstructions.size();
    }

    public ArchitectureType getMinimumArchitectureTypeNeededAtExpandLevel(int expandLevel) {
        return minimumArchitectureTypeNeededByExpandLevel.get(expandLevel);
    }
    // endregion

    // region Inner Classes
    private record ParsedComponents(List<Instruction> originalInstructions, Set<String> originalLabels) {
    }
    // endregion
}
