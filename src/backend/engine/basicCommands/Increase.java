package backend.engine.basicCommands;

import backend.engine.Command;
import backend.engine.CommandType;

import java.util.List;

public class Increase implements Command
{
    @Override
    public int execute(Object... args)
    {
        if (args[0] instanceof Integer)
        {
            int v = (int)args[0];
            return v + 1;
        }
        else
        {
            throw new IllegalArgumentException("v must be int!");
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
    public String getDisplayFormat(Object... argsNames)
    {
        if (argsNames[0] instanceof Integer)
        {
            return String.format("X%d <- X%d + 1", (int)argsNames[0],(int)argsNames[0]);
        }
        else
        {
            throw new IllegalArgumentException("first argument must be int!");
        }
    }
}
