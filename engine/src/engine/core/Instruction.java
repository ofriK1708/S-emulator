package engine.core;

import dto.engine.InstructionDTO;
import engine.core.basicCommand.Decrease;
import engine.core.basicCommand.Increase;
import engine.core.basicCommand.JumpNotZero;
import engine.core.basicCommand.Neutral;
import engine.core.syntheticCommand.*;
import engine.generated_2.SInstruction;
import engine.generated_2.SInstructionArguments;
import engine.utils.ProgramUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class Instruction implements Command, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    protected String mainVarName;
    protected final Map<String, String> args;
    protected @NotNull String label;
    protected final static String ProgramCounterName = "PC";
    protected @Nullable Instruction derivedFrom = null;
    protected int derivedFromIndex;

    protected Instruction(String mainVarName, Map<String, String> args, @Nullable String label, @NotNull Instruction derivedFrom, int derivedFromIndex) {
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

    public static @NotNull Instruction createInstruction(@NotNull SInstruction sInstruction, @NotNull ProgramEngine engine) {

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
            case "JUMP_NOT_ZERO" ->
                    new JumpNotZero(mainVarName, args, labelName);
            case "NEUTRAL" -> new Neutral(mainVarName, args, labelName);
            case "ZERO_VARIABLE" ->
                    new ZeroVariable(mainVarName, args, labelName);
            case "GOTO_LABEL" ->
                    new GOTOLabel(mainVarName, args, labelName);
            case "ASSIGNMENT" ->
                    new Assignment(mainVarName, args, labelName);
            case "CONSTANT_ASSIGNMENT" ->
                    new ConstantAssignment(mainVarName, args, labelName);
            case "JUMP_ZERO" -> new JumpZero(mainVarName, args, labelName);
            case "JUMP_EQUAL_CONSTANT" ->
                    new JumpEqualConstant(mainVarName, args, labelName);
            case "JUMP_EQUAL_VARIABLE" ->
                    new JumpEqualVariable(mainVarName, args, labelName);
            case "QUOTE" ->
                    new Quote(mainVarName, args, labelName, engine);
            case "JUMP_EQUAL_FUNCTION" ->
                    new JumpEqualFunction(mainVarName, args, labelName, engine);
            default ->
                    throw new IllegalArgumentException("Unknown instruction type: " + sInstruction.getName());
        };
    }

    private void setDerivedFrom(@Nullable Instruction derivedFrom) {
        this.derivedFrom = derivedFrom;
    }

    private void setDerivedFromIndex(int derivedFromIndex) {
        this.derivedFromIndex = derivedFromIndex;
    }

    protected static Quote createQuoteFromString(String argName, @NotNull ProgramEngine mainFunction) {
        String functionCallContent = ProgramUtils.extractFunctionContent(argName);
        List<String> parts = ProgramUtils.splitArgs(functionCallContent);
        String functionName = parts.getFirst().trim();
        String functionArgs = String.join(",", parts.subList(1, parts.size()));
        Map<String, String> quoteArgs = new HashMap<>();
        quoteArgs.put(Quote.functionNameArgumentName, functionName);
        quoteArgs.put(Quote.functionArgumentsArgumentName, functionArgs);
        return new Quote("", quoteArgs, "", mainFunction);
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
        contextMap.put(ProgramCounterName, contextMap.get(ProgramCounterName) + 1);
    }

    private @NotNull List<InstructionDTO> getDerivedInstructions() {
        List<InstructionDTO> derivedInstructions = new LinkedList<>();
        Instruction tempDerivedFrom = this.derivedFrom;
        int tempDerivedFromIndex = this.derivedFromIndex;
        while (tempDerivedFrom != null) {
            derivedInstructions.add(tempDerivedFrom.simpleToDTO(tempDerivedFromIndex));
            tempDerivedFromIndex = tempDerivedFrom.derivedFromIndex;
            tempDerivedFrom = tempDerivedFrom.derivedFrom;
        }
        return derivedInstructions;
    }

    @Override
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

    @Contract("_ -> new")
    private @NotNull InstructionDTO simpleToDTO(int index) {
        return new InstructionDTO(
                index,
                getType(),
                label,
                getStringRepresentation(),
                getCycles(),
                Collections.emptyList()
        );
    }

    protected List<Instruction> getUpdatedFunctionInstructions(
            @NotNull Map<String, Integer> mainContextMap,
            @NotNull ProgramEngine function,
            @NotNull String mainVarName,
            @NotNull List<String> newArgsNames,
            @NotNull Instruction derivedFrom,
            int derivedFromIndex) {

        List<Instruction> functionInstructions = function.getFunctionInstructions();
        Iterator<String> newArgsNamesIterator = newArgsNames.iterator();
        Set<String> replaced = new HashSet<>();

        String nextFreeWorkVariableName = ProgramUtils.getNextFreeWorkVariableName(mainContextMap);
        findAndChangeAllOccurrences(functionInstructions, mainVarName, nextFreeWorkVariableName);
        replaced.add(nextFreeWorkVariableName);

        for (String newArgsName : newArgsNames) {
            String newWorkVarInPlaceOfArg = ProgramUtils.getNextFreeWorkVariableName(mainContextMap);
            findAndChangeAllOccurrences(functionInstructions, newArgsName, newWorkVarInPlaceOfArg);
            replaced.add(newWorkVarInPlaceOfArg);
        }

        for (Instruction instruction : functionInstructions) {
            instruction.setDerivedFrom(derivedFrom);
            instruction.setDerivedFromIndex(derivedFromIndex);
            replaceIfNeeded(mainContextMap, replaced, functionInstructions, instruction.getLabel(),
                    mainVarName,
                    instruction::setLabel, newArgsNamesIterator);

            replaceIfNeeded(mainContextMap, replaced, functionInstructions, instruction.getMainVarName(), mainVarName,
                    instruction::setMainVarName, newArgsNamesIterator);

            processInstructionArguments(mainContextMap, replaced, functionInstructions, instruction, newArgsNamesIterator);
        }
        checkForExitLabel(functionInstructions, mainContextMap, derivedFrom, derivedFromIndex);
        function.resetFunction();
        return functionInstructions;
    }

    private void checkForExitLabel(List<Instruction> functionInstructions,
                                   @NotNull Map<String, Integer> mainContextMap, Instruction derivedFrom, int derivedFromIndex) {
        for (int i = 0; i < functionInstructions.size(); i++) {
            Instruction instruction = functionInstructions.get(i);
            for (String varValues : instruction.getArgs().values()) {
                if (varValues.equals(ProgramUtils.EXIT_LABEL_NAME)) {
                    String newExitLabel = ProgramUtils.getNextFreeLabelName(mainContextMap);
                    findAndChangeAllOccurrences(functionInstructions, ProgramUtils.EXIT_LABEL_NAME, newExitLabel);
                    functionInstructions.addLast(new Neutral(ProgramUtils.OUTPUT_NAME, Map.of(), newExitLabel, derivedFrom, derivedFromIndex));
                }
            }
        }
    }

    private void processInstructionArguments(
            Map<String, Integer> mainContextMap,
            Set<String> alreadyReplaced,
            List<Instruction> functionInstructions,
            Instruction instruction,
            Iterator<String> newArgsNamesIterator
    ) {

        for (Map.Entry<String, String> argsEntry : instruction.getArgs().entrySet()) {
            if (argsEntry.getKey().equals(Quote.functionNameArgumentName)) {
                Set<String> quoteArguments = ProgramUtils.extractAllVariablesFromQuoteArguments(argsEntry.getValue());
                for (String argValue : quoteArguments) {
                    replaceIfNeeded(mainContextMap, alreadyReplaced, functionInstructions, argValue, mainVarName,
                            newVal -> argsEntry.setValue(argsEntry.getValue().replace(argValue, newVal)),
                            newArgsNamesIterator);
                }
            } else {
                String argValue = argsEntry.getValue();
                replaceIfNeeded(mainContextMap, alreadyReplaced, functionInstructions, argValue, mainVarName,
                        argsEntry::setValue,
                        newArgsNamesIterator);
            }
        }
    }

    private void replaceIfNeeded(Map<String, Integer> mainContextMap,
                                 Set<String> alreadyReplaced,
                                 List<Instruction> functionInstructions,
                                 String oldValue,
                                 String mainVarName,
                                 Consumer<String> valueSetter,
                                 Iterator<String> newArgsNamesIterator) {
        if (!alreadyReplaced.contains(oldValue)) {
            alreadyReplaced.add(oldValue);
            String newValue = null;
            if (ProgramUtils.isLabel(oldValue) && !mainContextMap.containsKey(oldValue)) {
                mainContextMap.put(oldValue, 0); // just for saving the name
            } else if (ProgramUtils.isArgument(oldValue) && newArgsNamesIterator.hasNext()) {
                newValue = newArgsNamesIterator.next();
                valueSetter.accept(newValue);
                findAndChangeAllOccurrences(functionInstructions, oldValue, newValue);
            } else {
                if (mainContextMap.containsKey(oldValue)) {
                    if (oldValue.equals(ProgramUtils.OUTPUT_NAME)) {
                        newValue = mainVarName;
                        alreadyReplaced.add(mainVarName);
                    } else if (ProgramUtils.isLabel(oldValue)) {
                        newValue = ProgramUtils.getNextFreeLabelName(mainContextMap);
                    } else if (ProgramUtils.isWorkVariable(oldValue)) {
                        newValue = ProgramUtils.getNextFreeWorkVariableName(mainContextMap);
                    }
                    if (newValue != null) {
                        valueSetter.accept(newValue);
                        findAndChangeAllOccurrences(functionInstructions, oldValue, newValue);
                    }
                }
            }
        }
    }


    private void findAndChangeAllOccurrences(List<Instruction> functionInstructions, String oldValue, String newValue) {
        for (Instruction instruction : functionInstructions) {
            if (instruction.getLabel().equals(oldValue)) {
                instruction.setLabel(newValue);
            }
            if (instruction.getMainVarName().equals(oldValue)) {
                instruction.setMainVarName(newValue);
            }
            for (Map.Entry<String, String> entry : instruction.getArgs().entrySet()) {
                if (entry.getValue().equals(oldValue)) {
                    entry.setValue(newValue);
                }
            }
        }
    }
}
