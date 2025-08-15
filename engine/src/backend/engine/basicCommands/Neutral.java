package backend.engine.basicCommands;

import backend.engine.Command;
import backend.engine.CommandType;

import java.util.List;
import java.util.ArrayList;

public class Neutral implements Command
{
    @Override
    public int execute(Object... args)
    {
        if (args[0] instanceof Integer)
        {
            return (int)args[0];
        }
        else
        {
            throw new IllegalArgumentException("v must be int!");
        }
    }

    @Override
    public List<Command> expand(int level) {
        return new ArrayList<>();
    }

    @Override
    public int getCycles() {
        return 0;
    }

    @Override
    public CommandType getType() {
        return null;
    }

    @Override
    public int getNumberOfArgs() {
        return 0;
    }

    @Override
    public String getDisplayFormat(Object... argsNames) {
        return "";
    }
}
