package backend.engine.basicCommands;

import backend.engine.Command;
import backend.engine.CommandType;
import backend.engine.Instruction;

import java.util.List;
import java.util.Map;

public class Neutral extends Instruction implements Command
{

    protected Neutral(String mainVarName, Map<String, Integer> contextMap)
    {
        super(mainVarName, contextMap);
    }

    @Override
    public void execute(Map<String, String> args)
    {
        return; // No operation, does nothing
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
    public String getDisplayFormat(Map<String, String> args) {
        return String.format("%s <- %s", mainVarName, mainVarName);
    }
}
