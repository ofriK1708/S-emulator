package backend.engine.syntheticCommands;

import backend.engine.Command;
import backend.engine.CommandType;

import java.util.List;

public class Assignment implements Command
{
    @Override
    public int execute(Object... args) {
        if(args.length == 2 && args[0] instanceof Integer && args[1] instanceof Integer)
        {
            return (int)args[1];
        }
        else
        {
            throw new IllegalArgumentException("invalid arguments!!!");
        }
    }

    @Override
    public int getCycles() {
        return 4;
    }

    @Override
    public CommandType getType() {
        return CommandType.SYNTHETIC;
    }

    @Override
    public List<Command> expand(int level) {
        return List.of();
    }

    @Override
    public int getNumberOfArgs() {
        return 1;
    }

    @Override
    public String getDisplayFormat(Object... argsNames) {
        if(argsNames.length == 2 && argsNames[0] instanceof Integer && argsNames[1] instanceof Integer)
        {
            return String.format("Z%d <- X%d", (int)argsNames[0],(int)argsNames[1]);
        }
        else
        {
            throw new IllegalArgumentException("invalid arguments!!!");
        }
    }
}
