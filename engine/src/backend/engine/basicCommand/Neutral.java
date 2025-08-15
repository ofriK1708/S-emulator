package backend.engine.basicCommand;

import backend.engine.Command;
import backend.engine.CommandType;
import backend.engine.Instruction;

import java.util.List;
import java.util.Map;

public class Neutral extends Instruction implements Command
{

    public Neutral(String mainVarName, Map<String, String> args)
    {
        super(mainVarName, args);
    }

    @Override
    public void execute(Map<String, Integer> contextMap) throws IllegalArgumentException
    {
        // This command does nothing, so we don't change the context map.
    }

    @Override
    public List<Command> expand(int level) {
        return List.of();
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
    public String getDisplayFormat() {
        return String.format("%s <- %s", mainVarName, mainVarName);
    }
}
