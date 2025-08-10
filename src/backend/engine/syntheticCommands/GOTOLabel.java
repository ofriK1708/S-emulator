package backend.engine.syntheticCommands;

import backend.engine.Command;
import backend.engine.CommandType;

import java.util.List;

public class GOTOLabel implements Command
{
    @Override
    public int execute(Object... args) // this function return result should be ignored
    {
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
    public List<Command> expand(int level) {
        return List.of();
    }

    @Override
    public int getNumberOfArgs() {
        return 0;
    }

    @Override
    public String getDisplayFormat(Object... argsNames) {
        if (argsNames[0] instanceof String)
        {
            String labelName = argsNames[0].toString();
            return String.format("GOTO %s", labelName);
        }
        else
        {
            throw new IllegalArgumentException("incorrect display arguments!");
        }
    }
}
