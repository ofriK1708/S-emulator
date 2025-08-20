package backend.engine;

import backend.engine.basicCommand.Decrease;
import backend.engine.basicCommand.Increase;
import backend.engine.basicCommand.JumpNotZero;
import backend.engine.basicCommand.Neutral;
import backend.engine.syntheticCommand.*;
import backend.system.generated.SInstruction;
import backend.system.generated.SInstructionArgument;
import backend.system.generated.SInstructionArguments;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class Instruction implements Command
{
    protected String mainVarName;

    protected Map<String, String> args;
    protected String label;
    protected final static String ProgramCounterName = "PC";
    protected Instruction derivedFrom = null;
    protected int derivedFromIndex;

    protected Instruction(String mainVarName, Map<String, String> args, String label, Instruction derivedFrom)
    {
        this.mainVarName = mainVarName;
        this.args = args;
        this.label = label;
        this.derivedFrom = derivedFrom;
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
                .map(argsList -> argsList.stream().collect(Collectors.toMap(
                        SInstructionArgument::getName,
                        SInstructionArgument::getValue
                )))
                .orElse(Collections.emptyMap());
        String mainVarName = Optional.ofNullable(sInstruction.getSVariable()).orElseThrow(
                () -> new IllegalArgumentException("Instruction must have main variable!"));
        String labelName = sInstruction.getSLabel();

        return switch (sInstruction.getName())
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
    protected String formatDisplay(int instructionNumber, String commandPart)
    {
        String numberPart = "#" + instructionNumber;
        String typePart = getType() == CommandType.BASIC ? "(B)" : "(S)";
        String labelPart = "[ " + String.format("%-4s", label) + "]";
        String cyclesPart = "(" + getCycles() + ")";
        String full = String.format("%s %s %s %s %s", numberPart, typePart, labelPart, commandPart, cyclesPart);
        if (derivedFrom != null)
        {
            full += "<<<" + derivedFrom.getDisplayFormat(derivedFromIndex);
        }
        return full;
    }
    protected void incrementProgramCounter(Map<String, Integer> contextMap)
    {
        contextMap.put(ProgramCounterName, contextMap.get(ProgramCounterName) + 1);
    }
}
