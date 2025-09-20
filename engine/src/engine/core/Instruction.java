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
import java.util.stream.Collectors;

public abstract class Instruction implements Command, Serializable
{
    @Serial
    private static final long serialVersionUID = 1L;
    protected final String mainVarName;
    protected final Map<String, String> args;
    protected final @NotNull String label;
    protected final static String ProgramCounterName = "PC";
    protected @Nullable Instruction derivedFrom = null;
    protected int derivedFromIndex;

    protected Instruction(String mainVarName, Map<String, String> args, @Nullable String label, @NotNull Instruction derivedFrom, int derivedFromIndex)
    {
        this.mainVarName = mainVarName;
        this.args = args;
        this.label = label == null ? "" : label;
        this.derivedFrom = derivedFrom;
        this.derivedFromIndex = derivedFromIndex;
    }

    protected Instruction(String mainVarName, Map<String, String> args, @Nullable String label)
    {
        this.mainVarName = mainVarName;
        this.args = args;
        this.label = label == null ? "" : label;
    }

    public static @NotNull Instruction createInstruction(@NotNull SInstruction sInstruction, @NotNull ProgramEngine engine)
    {

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

        return switch (sInstruction.getName().trim())
        {
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
            case "QUOTE" ->
                    new Quote(mainVarName, args, labelName, engine);
            case "JUMP_EQUAL_FUNCTION" ->
                    new JumpEqualFunction(mainVarName, args, labelName, engine);
            default -> throw new IllegalArgumentException("Unknown instruction type: " + sInstruction.getName());
        };
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

    public String getMainVarName()
    {
        return mainVarName;
    }

    public Map<String, String> getArgs()
    {
        return args;
    }

    public @NotNull String getLabel()
    {
        return label;
    }

    protected void incrementProgramCounter(@NotNull Map<String, Integer> contextMap)
    {
        contextMap.put(ProgramCounterName, contextMap.get(ProgramCounterName) + 1);
    }

    private @NotNull List<InstructionDTO> getDerivedInstructions()
    {
        List<InstructionDTO> derivedInstructions = new LinkedList<>();
        Instruction tempDerivedFrom = this.derivedFrom;
        int tempDerivedFromIndex = this.derivedFromIndex;
        while (tempDerivedFrom != null)
        {
            derivedInstructions.add(tempDerivedFrom.simpleToDTO(tempDerivedFromIndex));
            tempDerivedFromIndex = tempDerivedFrom.derivedFromIndex;
            tempDerivedFrom = tempDerivedFrom.derivedFrom;
        }
        return derivedInstructions;
    }

    @Override
    public @NotNull InstructionDTO toDTO(int idx)
    {
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
    private @NotNull InstructionDTO simpleToDTO(int index)
    {
        return new InstructionDTO(
                index,
                getType(),
                label,
                getStringRepresentation(),
                getCycles(),
                Collections.emptyList()
        );
    }
}
