package core.basicCommand;

import core.CommandType;
import core.Instruction;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Increase extends Instruction
{
    private static final int expandLevel = 0;

    public Increase(String mainVarName, Map<String, String> args, String labelName)
    {
        super(mainVarName, args, labelName);
    }

    public Increase(String mainVarName, Map<String, String> args, String label, Instruction derivedFrom, int derivedFromIndex)
    {
        super(mainVarName, args, label, derivedFrom, derivedFromIndex);
    }

    @Override
    public void execute(Map<String, Integer> contextMap) throws IllegalArgumentException
    {
        try
        {
            contextMap.put(mainVarName, contextMap.get(mainVarName) + 1);
            incrementProgramCounter(contextMap);
        } catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("expected integer value for " + mainVarName);
        }

    }

    @Override
    public List<Instruction> expand(Map<String, Integer> contextMap, int originalInstructionIndex)
    {
        return new LinkedList<>(List.of(this));
    }

    @Override
    public int getCycles() {
        return 1;
    }

    @Override
    public CommandType getType() {
        return CommandType.BASIC;
    }

    @Override
    public int getNumberOfArgs(Map<String, Integer> contextMap)
    {
        return 0;
    }

    @Override
    public String getDisplayFormat(int instructionIndex)
    {
        String commandPart = String.format("%s <- %s + 1", mainVarName, mainVarName);
        return formatDisplay(instructionIndex, commandPart);
    }

    @Override
    public int getExpandLevel()
    {
        return expandLevel;
    }
}
