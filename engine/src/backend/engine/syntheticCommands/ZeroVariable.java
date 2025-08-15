package backend.engine.syntheticCommands;

import backend.engine.Command;
import backend.engine.CommandType;

import java.util.List;
import java.util.ArrayList;

public class ZeroVariable implements Command
{
    @Override
    public int execute(Object... args) {
        return 0;
    }

    @Override
    public int getCycles() {
        return 1;
    }

    @Override
    public CommandType getType() {
        return CommandType.SYNTHETIC;
    }

    @Override
    public List<Command> expand(int level)
    {
        return new ArrayList<>();
    }

    @Override
    public int getNumberOfArgs() {
        return 0;
    }

    @Override
    public String getDisplayFormat(Object... argsNames) {
        if (argsNames[0] instanceof Integer)
        {
            return String.format("X%d <- 0", (int)argsNames[0]);
        }
        else
        {
            throw new IllegalArgumentException("first argument must be int!");
        }
    }
}
