package backend.engine.syntheticCommand;

import backend.engine.CommandType;
import backend.engine.Instruction;
import backend.engine.ProgramUtils;
import backend.engine.basicCommand.Decrease;
import backend.engine.basicCommand.JumpNotZero;
import backend.engine.basicCommand.Neutral;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ZeroVariable extends Instruction
{
    public ZeroVariable(String mainVarName, Map<String, String> args, String labelName)
    {
        super(mainVarName, args, labelName);
    }

    public ZeroVariable(String mainVarName, Map<String, String> args, String label, Instruction derivedFrom, int derivedFromIndex)
    {
        super(mainVarName, args, label, derivedFrom, derivedFromIndex);
    }

    @Override
    public void execute(Map<String, Integer> contextMap) throws IllegalArgumentException
    {
        contextMap.put(mainVarName, 0);
        incrementProgramCounter(contextMap);
    }

    @Override
    public int getCycles()
    {
        return 1;
    }

    @Override
    public CommandType getType()
    {
        return CommandType.SYNTHETIC;
    }

    @Override
    public List<Instruction> expand(Map<String, Integer> contextMap, int originalInstructionIndex)
    {
        List<Instruction> expanded = new LinkedList<>();
        String freeLabelName = ProgramUtils.getNextFreeLabelName(contextMap);
        expanded.add(new Neutral(mainVarName, null, label, this, originalInstructionIndex));
        expanded.add(new Decrease(mainVarName, null, freeLabelName, this, originalInstructionIndex));
        expanded.add(new JumpNotZero(mainVarName,
                Map.of(JumpNotZero.labelArgumentName, freeLabelName), null, this, originalInstructionIndex));
        return expanded;
    }

    @Override
    public int getNumberOfArgs(Map<String, Integer> contextMap)
    {
        return 0;
    }

    @Override
    public String getDisplayFormat(int instructionIndex)
    {
        String commandPart = String.format("%s <- 0", mainVarName);
        return formatDisplay(instructionIndex, commandPart);
    }
}
