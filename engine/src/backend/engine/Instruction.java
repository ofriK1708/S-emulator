package backend.engine;

import backend.engine.basicCommand.Decrease;
import backend.engine.basicCommand.Increase;
import backend.engine.basicCommand.JumpNotZero;
import backend.engine.basicCommand.Neutral;
import backend.engine.syntheticCommand.*;
import backend.system.generated.SInstruction;
import backend.system.generated.SInstructionArgument;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Instruction
{
    protected String mainVarName;
    protected Map<String, String> args;
    protected final String PCName = "PC";

    protected Instruction(String mainVarName, Map<String, String> args)
    {
        this.mainVarName = mainVarName;
        this.args = args;
    }

    public static Instruction createInstruction(SInstruction sInstruction)
    {
        Map<String, String> args = sInstruction
                .getSInstructionArguments()
                .getSInstructionArgument()
                .stream()
                .collect(Collectors.toMap(SInstructionArgument::getName, SInstructionArgument::getValue));
        String mainVarName = Optional.ofNullable(sInstruction.getName()).orElseThrow(
                () -> new IllegalArgumentException("Instruction must have main variable!"));

        return switch (sInstruction.getName())
        {
            case "Increase" -> new Increase(mainVarName, args);
            case "Decrease" -> new Decrease(mainVarName, args);
            case "JUMP_NOT_ZERO" -> new JumpNotZero(mainVarName, args);
            case "NEUTRAL" -> new Neutral(mainVarName, args);
            case "ZERO_VARIABLE" -> new ZeroVariable(mainVarName, args);
            case "GOTO_LABEL" -> new GOTOLabel(mainVarName, args);
            case "ASSIGNMENT" -> new Assignment(mainVarName, args);
            case "CONSTANT_ASSIGNMENT" -> new ConstantAssignment(mainVarName, args);
            case "JUMP_ZERO" -> new JumpZero(mainVarName, args);
            case "JUMP_EQUAL_CONSTANT" -> new JumpEqualConstant(mainVarName, args);
            case "JUMP_EQUAL_VARIABLE" -> new JumpEqualVariable(mainVarName, args);
            default -> throw new IllegalArgumentException("Unknown instruction type: " + sInstruction.getName());
        };

    }
}
