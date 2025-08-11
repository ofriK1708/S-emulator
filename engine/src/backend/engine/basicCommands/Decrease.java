package backend.engine.basicCommands;

import backend.engine.Command;
import backend.engine.CommandType;
import backend.engine.Instruction;

import java.util.List;
import java.util.Map;

public class Decrease extends Instruction implements Command
{
    public Decrease(String self, Map<String, Integer> context)
    {
        super(self, context);
    }

    @Override
    public void execute(Map<String,String> args)
    {
        try
        {
            contextMap.put(mainVarName, contextMap.get(mainVarName) - 1);
            contextMap.put(PCName, contextMap.get(PCName) + 1);
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
    public String getDisplayFormat(Map<String,String> args)
    {
        return String.format("%s <- %s - 1", mainVarName, mainVarName);
    }
}
