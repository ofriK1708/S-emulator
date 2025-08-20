package backend.engine.basicCommand;

import backend.engine.CommandType;
import backend.engine.Instruction;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class Decrease extends Instruction
{

    private static final int expandLevel = 0;

    public Decrease(String mainVarName, Map<String, String> args, String labelName)
    {
        super(mainVarName, args, labelName);
    }

    public Decrease(String mainVarName, Map<String, String> args, String label, Instruction derivedFrom)
    {
        super(mainVarName, args, label, derivedFrom);
    }

    @Override
    public void execute(Map<String,Integer> contextMap) throws IllegalArgumentException
    {
        try
        {
            contextMap.put(mainVarName, Math.max((contextMap.get(mainVarName) - 1),0));
            incrementProgramCounter(contextMap);
        } catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("expected integer value for " + mainVarName);
        }

    }

    @Override
    public List<Instruction> expand(Map<String, Integer> contextMap, int originalInstructionIndex, int expandedInstructionIndex)
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
    public String getDisplayFormat(int instructionNumber)
    {
        String commandPart = String.format("%s <- %s - 1", mainVarName, mainVarName);
        return formatDisplay(instructionNumber, commandPart);
    }

    public int getExpandLevel()
    {
        return expandLevel;
    }
}
