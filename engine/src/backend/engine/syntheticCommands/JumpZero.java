package backend.engine.syntheticCommands;

import backend.engine.Command;
import backend.engine.CommandType;

import java.util.List;

public class JumpZero implements Command
{
    // return the value of int - the system should manage the flow
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
        return List.of();
    }

    @Override
    public int getCycles() {
        return 2;
    }

    @Override
    public CommandType getType() {
        return CommandType.SYNTHETIC;
    }

    @Override
    public int getNumberOfArgs() {
        return 1;
    }

    @Override
    public String getDisplayFormat(Object... argsNames) {
        if (argsNames.length == 2 && argsNames[0] instanceof Integer
                && argsNames[1] instanceof String)
        {
            int numOfArgument = (int)argsNames[0];
            String labelName = argsNames[1].toString();
            return String.format("IF X%d = 0 GOTO %s",numOfArgument,labelName);
        }
        else
        {
            throw new IllegalArgumentException("incorrect display arguments!");
        }
    }
}
