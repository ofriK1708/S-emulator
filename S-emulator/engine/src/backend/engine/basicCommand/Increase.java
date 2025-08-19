package backend.engine.basicCommand;

import backend.engine.Command;
import backend.engine.CommandType;
import backend.engine.Instruction;

import java.util.List;
import java.util.Map;

public class Increase extends Instruction
{
    public Increase(String mainVarName, Map<String, String> args, String labelName)
    {
        super(mainVarName, args, labelName);
    }

    @Override
    public void execute(Map<String, Integer> contextMap) throws IllegalArgumentException
    {
        try
        {
            contextMap.put(mainVarName, contextMap.get(mainVarName) + 1);
            contextMap.put(ProgramCounterName, contextMap.get(ProgramCounterName) + 1);
        } catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("expected integer value for " + mainVarName);
        }

    }

    @Override
    public List<Command> expand(int level) {
        return List.of();
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
    public int getNumberOfArgs()
    {
        return 0;
    }

    @Override
    public String getDisplayFormat(int instructionNumber)
    {
        String commandPart = String.format("%s <- %s + 1", mainVarName, mainVarName);
        return formatDisplay(instructionNumber, commandPart);
    }
}
