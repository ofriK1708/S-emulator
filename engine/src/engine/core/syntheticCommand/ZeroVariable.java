package engine.core.syntheticCommand;

import engine.core.Instruction;
import engine.core.basicCommand.Decrease;
import engine.core.basicCommand.JumpNotZero;
import engine.core.basicCommand.Neutral;
import engine.utils.CommandType;
import engine.utils.ProgramUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ZeroVariable extends Instruction
{
    private static int expandLevel = -1;

    public ZeroVariable(String mainVarName, Map<String, String> args, String labelName)
    {
        super(mainVarName, args, labelName);
        expandLevel = ProgramUtils.calculateExpandedLevel(this, expandLevel);
    }

    public ZeroVariable(String mainVarName, Map<String, String> args, String label, Instruction derivedFrom, int derivedFromIndex)
    {
        super(mainVarName, args, label, derivedFrom, derivedFromIndex);
        expandLevel = ProgramUtils.calculateExpandedLevel(this, expandLevel);
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
    public int getExpandLevel()
    {
        if (expandLevel == -1)
        {
            expandLevel = ProgramUtils.calculateExpandedLevel(this, expandLevel);
        }
        return expandLevel;
    }

    @Override
    public String getStringRepresentation()
    {
        return String.format("%s <- 0", mainVarName);
    }
}
