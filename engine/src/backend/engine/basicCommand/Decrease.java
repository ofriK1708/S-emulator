package backend.engine.basicCommand;

import backend.engine.Command;
import backend.engine.CommandType;
import backend.engine.Instruction;

import java.util.List;
import java.util.Map;


public class Decrease extends Instruction implements Command
{
    public Decrease(String mainVarName, Map<String, String> args, String labelName)
    {
        super(mainVarName, args, labelName);
    }

    @Override
    public void execute(Map<String,Integer> contextMap) throws IllegalArgumentException
    {
        try
        {
            contextMap.put(mainVarName, Math.max((contextMap.get(mainVarName) - 1),0));
            contextMap.put(ProgramCounter, contextMap.get(ProgramCounter) + 1);
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
    public String getDisplayFormat()
    {
        return String.format("%s <- %s - 1", mainVarName, mainVarName);
    }
}
