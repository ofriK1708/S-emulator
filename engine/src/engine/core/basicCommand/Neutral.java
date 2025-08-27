package engine.core.basicCommand;

import engine.core.CommandType;
import engine.core.Instruction;

import java.util.List;
import java.util.Map;

public class Neutral extends Instruction
{
    private static final int expandLevel = 0;
    public Neutral(String mainVarName, Map<String, String> args, String labelName)
    {
        super(mainVarName, args, labelName);
    }

    public Neutral(String mainVarName, Map<String, String> args, String label, Instruction derivedFrom, int derivedFromIndex)
    {
        super(mainVarName, args, label, derivedFrom, derivedFromIndex);
    }

    @Override
    public void execute(Map<String, Integer> contextMap) throws IllegalArgumentException
    {
        incrementProgramCounter(contextMap);
    }

    @Override
    public List<Instruction> expand(Map<String, Integer> contextMap, int originalInstructionIndex)
    {
        // This command does not expand further, it is already in its final form.
        // It can be used directly in the program.
        return List.of(this);
    }

    @Override
    public int getCycles()
    {
        return 0;
    }

    @Override
    public CommandType getType()
    {
        return CommandType.BASIC;
    }

    @Override
    public int getNumberOfArgs(Map<String, Integer> contextMap)
    {
        return 0;
    }

    @Override
    public int getExpandLevel()
    {
        return expandLevel;
    }

    @Override
    public String toString()
    {
        return String.format("%s <- %s", mainVarName, mainVarName);
    }
}
