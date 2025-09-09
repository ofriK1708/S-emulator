package engine.core;

import dto.engine.InstructionDTO;
import engine.core.basicCommand.Decrease;
import engine.core.basicCommand.Increase;
import engine.core.basicCommand.JumpNotZero;
import engine.core.basicCommand.Neutral;
import engine.core.syntheticCommand.*;
import engine.generated.SInstruction;
import engine.generated.SInstructionArguments;

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
    protected final String label;
    protected final static String ProgramCounterName = "PC";
    protected Instruction derivedFrom = null;
    protected int derivedFromIndex;

    protected Instruction(String mainVarName, Map<String, String> args, String label, Instruction derivedFrom, int derivedFromIndex)
    {
        this.mainVarName = mainVarName;
        this.args = args;
        this.label = label == null ? "" : label;
        this.derivedFrom = derivedFrom;
        this.derivedFromIndex = derivedFromIndex;
    }

    protected Instruction(String mainVarName, Map<String, String> args, String label)
    {
        this.mainVarName = mainVarName;
        this.args = args;
        this.label = label == null ? "" : label;
    }

    public String getLabel()
    {
        return label;
    }

    public String getMainVarName()
    {
        return mainVarName;
    }

    public Map<String, String> getArgs()
    {
        return args;
    }

    public static Instruction createInstruction(SInstruction sInstruction)
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
            default -> throw new IllegalArgumentException("Unknown instruction type: " + sInstruction.getName());
        };
    }

    protected void incrementProgramCounter(Map<String, Integer> contextMap)
    {
        contextMap.put(ProgramCounterName, contextMap.get(ProgramCounterName) + 1);
    }

    private List<InstructionDTO> getDerivedInstructions()
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
    public InstructionDTO toDTO(int idx)
    {
        return new InstructionDTO(
                idx,
                getType().getSymbol(),
                label,
                toString(),
                getCycles(),
                getDerivedInstructions()
        );
    }

    private InstructionDTO simpleToDTO(int index)
    {
        return new InstructionDTO(
                index,
                getType().getSymbol(),
                label,
                toString(),
                getCycles(),
                Collections.emptyList()
        );
    }
}
