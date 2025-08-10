package backend.engine.syntheticCommands;

import backend.engine.Command;
import backend.engine.CommandType;

import java.util.List;

public class JumpEqualConstant implements Command
{
    @Override
    public int execute(Object... args) {
        if(args.length == 2 && args[0] instanceof Integer && args[1] instanceof Integer)
        {
            return (int)args[1] - (int)args[0];
        }
        else
        {
            throw new IllegalArgumentException("invalid arguments!!!");
        }
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
    public List<Command> expand(int level) {
        return List.of();
    }

    @Override
    public int getNumberOfArgs() {
        return 2;
    }

    @Override
    public String getDisplayFormat(Object... argsNames) {
        if (argsNames.length == 3 && argsNames[0] instanceof Integer
                && argsNames[1] instanceof Integer && argsNames[2] instanceof String)
        {
            int numOfArgument = (int)argsNames[0];
            int numOfConstant = (int)argsNames[1];
            String labelName = argsNames[2].toString();
            return String.format("IF X%d != %d GOTO %s",numOfArgument,numOfConstant,labelName);
        }
        else
        {
            throw new IllegalArgumentException("incorrect display arguments!");
        }
    }
}
