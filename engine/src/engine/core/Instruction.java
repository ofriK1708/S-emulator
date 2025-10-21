package engine.core;

import dto.engine.InstructionDTO;
import engine.core.basicCommand.Decrease;
import engine.core.basicCommand.Increase;
import engine.core.basicCommand.JumpNotZero;
import engine.core.basicCommand.Neutral;
import engine.core.syntheticCommand.*;
import engine.exception.FunctionNotFound;
import engine.generated_2.SInstruction;
import engine.generated_2.SInstructionArguments;
import engine.utils.ProgramUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static engine.utils.ProgramUtils.PC_NAME;

public abstract class Instruction implements Command {
    protected String mainVarName;
    protected final Map<String, String> args;
    protected @NotNull String label;
    protected @Nullable Instruction derivedFrom = null;
    protected int derivedFromIndex;

    protected Instruction(String mainVarName, Map<String, String> args, @Nullable String label,
                          @NotNull Instruction derivedFrom, int derivedFromIndex) {
        this.mainVarName = mainVarName;
        this.args = args;
        this.label = label == null ? "" : label;
        this.derivedFrom = derivedFrom;
        this.derivedFromIndex = derivedFromIndex;
    }

    protected Instruction(String mainVarName, Map<String, String> args, @Nullable String label) {
        this.mainVarName = mainVarName;
        this.args = args;
        this.label = label == null ? "" : label;
    }

    public static @NotNull Instruction createInstruction(@NotNull SInstruction sInstruction,
                                                         @NotNull FunctionManager functionManager,
                                                         int instructionIndex,
                                                         @NotNull String enclosingFunctionName) {

        Map<String, String> args = Optional.ofNullable(sInstruction.getSInstructionArguments())
                .map(SInstructionArguments::getSInstructionArgument)
                .map(argsList -> argsList.stream()
                        .collect(Collectors.toMap(
                                arg -> arg.getName().trim(),
                                arg -> arg.getValue().trim()
                        )))
                .orElse(Collections.emptyMap());
        String mainVarName = Optional.of(sInstruction.getSVariable().trim()).orElseThrow(
                () -> new IllegalArgumentException("Instruction must have main variable!"));
        String labelName = Optional.ofNullable(sInstruction.getSLabel()).map(String::trim).orElse("");

        return switch (sInstruction.getName().trim()) {
            case "INCREASE" -> new Increase(mainVarName, args, labelName);
            case "DECREASE" -> new Decrease(mainVarName, args, labelName);
            case "JUMP_NOT_ZERO" -> new JumpNotZero(mainVarName, args, labelName);
            case "NEUTRAL" -> new Neutral(mainVarName, args, labelName);
            case "ZERO_VARIABLE" -> new ZeroVariable(mainVarName, args, labelName);
            case "GOTO_LABEL" -> new GOTOLabel(mainVarName, args, labelName);
            case "ASSIGNMENT" -> new Assignment(mainVarName, args, labelName);
            case "CONSTANT_ASSIGNMENT" -> new ConstantAssignment(mainVarName, args, labelName);
            case "JUMP_ZERO" -> new JumpZero(mainVarName, args, labelName);
            case "JUMP_EQUAL_CONSTANT" -> new JumpEqualConstant(mainVarName, args, labelName);
            case "JUMP_EQUAL_VARIABLE" -> new JumpEqualVariable(mainVarName, args, labelName);
            case "QUOTE" -> Quote.createInitialQuote(mainVarName, args, labelName, functionManager, instructionIndex,
                    enclosingFunctionName);
            case "JUMP_EQUAL_FUNCTION" ->
                    new JumpEqualFunction(mainVarName, args, labelName, functionManager, instructionIndex,
                            enclosingFunctionName);
            default -> throw new IllegalArgumentException("Unknown instruction type: " + sInstruction.getName());
        };
    }

    private void setDerivedFrom(@Nullable Instruction derivedFrom) {
        this.derivedFrom = derivedFrom;
    }

    private void setDerivedFromIndex(int derivedFromIndex) {
        this.derivedFromIndex = derivedFromIndex;
    }

    protected static @NotNull Quote createSubFunctionCall(@NotNull String argName,
                                                          @NotNull FunctionManager functionManager,
                                                          int instructionIndex,
                                                          @NotNull String enclosingFunctionName) throws FunctionNotFound {
        String functionCallContent = ProgramUtils.extractFunctionContent(argName);
        List<String> parts = ProgramUtils.splitArgs(functionCallContent);
        String functionName = parts.getFirst().trim();
        String functionArgs = String.join(",", parts.subList(1, parts.size()));
        Map<String, String> quoteArgs = new HashMap<>();
        quoteArgs.put(Quote.functionNameArgumentName, functionName);
        quoteArgs.put(Quote.functionArgumentsArgumentName, functionArgs);
        return Quote.createSubFunctionQuote("", quoteArgs, "", functionManager, instructionIndex,
                enclosingFunctionName);
    }

    public String getMainVarName() {
        return mainVarName;
    }

    private void setMainVarName(String mainVarName) {
        this.mainVarName = mainVarName;
    }

    public Map<String, String> getArgs() {
        return args;
    }

    public @NotNull String getLabel() {
        return label;
    }

    private void setLabel(@NotNull String newLabel) {
        label = newLabel;
    }

    protected void incrementProgramCounter(@NotNull Map<String, Integer> contextMap) {
        contextMap.put(PC_NAME, contextMap.get(PC_NAME) + 1);
    }

    private @NotNull List<InstructionDTO> getDerivedInstructions() {
        List<InstructionDTO> derivedInstructions = new LinkedList<>();
        Instruction tempDerivedFrom = this.derivedFrom;
        int tempDerivedFromIndex = this.derivedFromIndex;
        while (tempDerivedFrom != null) {
            derivedInstructions.add(tempDerivedFrom.toSimpleDTO(tempDerivedFromIndex));
            tempDerivedFromIndex = tempDerivedFrom.derivedFromIndex;
            tempDerivedFrom = tempDerivedFrom.derivedFrom;
        }
        return derivedInstructions;
    }

    /**
     * Creates a DTO representation of the instruction including derived from info.
     *
     * @param idx The index of the instruction.
     * @return A new InstructionDTO instance.
     */
    public @NotNull InstructionDTO toDTO(int idx) {
        return new InstructionDTO(
                idx,
                getType(),
                label,
                getStringRepresentation(),
                getCycles(),
                getDerivedInstructions()
        );
    }

    /**
     * Creates a simple DTO representation of the instruction without derived from info.
     *
     * @param index The index of the instruction.
     * @return A new InstructionDTO instance.
     */
    @Contract("_ -> new")
    public @NotNull InstructionDTO toSimpleDTO(int index) {
        return new InstructionDTO(
                index,
                getType(),
                label,
                getStringRepresentation(),
                getCycles(),
                Collections.emptyList()
        );
    }

    protected @NotNull List<Instruction> getUpdatedFunctionInstructions(
            @NotNull Map<String, Integer> mainContextMap,
            @NotNull Engine function,
            @NotNull String outputVar,
            @NotNull Map<String, String> argsReplacements,
            @NotNull Instruction derivedFrom,
            int derivedFromIndex) {

        List<Instruction> functionInstructions = null;
        try {
            functionInstructions = function.getTempFunctionBasicInstructions();
        } catch (Exception ignored) {
        }
        Map<String, String> allReplacements = new HashMap<>(argsReplacements);
        setupConflictAvoidanceReplacements(mainContextMap, argsReplacements, allReplacements, function);
        String endLabel = ProgramUtils.getNextFreeLabelName(mainContextMap);
        allReplacements.put(ProgramUtils.EXIT_LABEL_NAME, endLabel);

        // now we can go and replace all the old workVariables with new ones, after we made sure they won't mix
        //noinspection DataFlowIssue
        for (Instruction instruction : functionInstructions) {
            // first we set the derived from info
            instruction.setDerivedFrom(derivedFrom);
            instruction.setDerivedFromIndex(derivedFromIndex);

            // check for label swap
            String oldLabel = instruction.getLabel();
            // special case for exit label, we need to add a new neutral instruction after the function instructions
            replaceIfNeeded(mainContextMap, oldLabel, allReplacements, instruction::setLabel);

            // check for main var swap
            String oldMainVar = instruction.getMainVarName();
            replaceIfNeeded(mainContextMap, oldMainVar, allReplacements, instruction::setMainVarName);

            // check for args swap
            processInstructionArguments(instruction, mainContextMap, allReplacements);
        }
        functionInstructions.addLast(new Assignment(outputVar, Map.of(Assignment.sourceArgumentName,
                allReplacements.get(ProgramUtils.OUTPUT_NAME)), allReplacements.get(ProgramUtils.EXIT_LABEL_NAME),
                derivedFrom, derivedFromIndex));

        return functionInstructions;
    }

    private void setupConflictAvoidanceReplacements(@NotNull Map<String, Integer> mainContextMap,
                                                    @NotNull Map<String, String> argsReplacements,
                                                    @NotNull Map<String, String> allReplacements,
                                                    @NotNull Engine function) {

        /* first we need to reserve a new work variable for the output of the function,
         * so we won't mix it with any other variable in the main program */
        String oldOutputReplacement = ProgramUtils.getNextFreeWorkVariableName(mainContextMap);
        allReplacements.put(ProgramUtils.OUTPUT_NAME, oldOutputReplacement);
        // if the output variable is already in the context, we need to replace it too
        if (function.isVariableInContext(oldOutputReplacement)) {
            String nextFreeWorkVariableName = ProgramUtils.getNextFreeWorkVariableName(mainContextMap);
            allReplacements.put(oldOutputReplacement, nextFreeWorkVariableName);
        }


        /* we need to check if out replacements of arguments are already taken and if so we replace them too */
        for (String newArgsName : argsReplacements.values()) {
            if (function.isVariableInContext(newArgsName)) {
                String newWorkVarInPlaceOfArg = ProgramUtils.getNextFreeWorkVariableName(mainContextMap);
                allReplacements.put(newArgsName, newWorkVarInPlaceOfArg);
            }
        }
    }

    private void replaceIfNeeded(@NotNull Map<String, Integer> mainContextMap,
                                 @NotNull String oldVar,
                                 @NotNull Map<String, String> allReplacements, @NotNull Consumer<String> valueSetter) {
        if (ProgramUtils.isVariableOrLabel(oldVar) &&
                (mainContextMap.containsKey(oldVar) || allReplacements.containsKey(oldVar))) {
            if (oldVar.equals(ProgramUtils.OUTPUT_NAME)) {
                // special case for output var, if we already replaced it with a new work var, we need to use it
                valueSetter.accept(allReplacements.getOrDefault(ProgramUtils.OUTPUT_NAME, ProgramUtils.OUTPUT_NAME));
            } else if (ProgramUtils.isArgument(oldVar)) {
                // special case for argument, we need to replace it with the new argument name
                if (allReplacements.containsKey(oldVar)) {
                    valueSetter.accept(allReplacements.get(oldVar));
                } else {
                    throw new IllegalStateException("Argument " + oldVar + " not found in replacements map");
                }
            } else if (ProgramUtils.isLabel(oldVar) || oldVar.equals(ProgramUtils.EXIT_LABEL_NAME)) {
                String newVar = allReplacements.computeIfAbsent(oldVar,
                        k -> ProgramUtils.getNextFreeLabelName(mainContextMap));
                valueSetter.accept(newVar);
            } else { // work variable
                String newVar = allReplacements.computeIfAbsent(oldVar,
                        k -> ProgramUtils.getNextFreeWorkVariableName(mainContextMap));
                valueSetter.accept(newVar);
            }
        }
        if (ProgramUtils.isVariableOrLabel(oldVar)) {
            mainContextMap.putIfAbsent(oldVar, 0); // just to reserve the name
            allReplacements.putIfAbsent(oldVar, oldVar); // we don't need to replace it so map it to itself
        }
    }

    private void processInstructionArguments(
            @NotNull Instruction instruction,
            @NotNull Map<String, Integer> mainContextMap,
            @NotNull Map<String, String> allReplacements
    ) {

        for (Map.Entry<String, String> argsEntry : instruction.getArgs().entrySet()) {
            // special case for quote in argument, we need to extract all the workVariables from the arguments and
            // replace them all
            if (argsEntry.getKey().equals(Quote.functionArgumentsArgumentName)) {
                Quote currentInstructionAsQuote = (Quote) instruction;
                String functionArguments = argsEntry.getValue();
                swapIfNeededInFunctionCall(functionArguments, allReplacements, argsEntry::setValue);
                currentInstructionAsQuote.updateArguments(argsEntry.getValue());
            } else {
                String argValue = argsEntry.getValue();
                replaceIfNeeded(mainContextMap, argValue, allReplacements, argsEntry::setValue);
            }
        }
    }

    private void swapIfNeededInFunctionCall(@NotNull String functionArguments,
                                            @NotNull Map<String, String> replacements,
                                            @NotNull Consumer<String> valueSetter) {

        String pattern = "\\b(" + String.join("|", replacements.keySet()) + ")\\b";
        Pattern regex = Pattern.compile(pattern);

        Matcher matcher = regex.matcher(functionArguments);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String match = matcher.group(1); // the actual variable name matched
            matcher.appendReplacement(result, replacements.get(match));
        }
        matcher.appendTail(result);
        valueSetter.accept(result.toString());
    }
}
