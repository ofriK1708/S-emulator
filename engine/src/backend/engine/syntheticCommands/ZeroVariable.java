package backend.engine.syntheticCommands;

import backend.engine.Command;
import backend.engine.CommandType;
import backend.engine.Instruction;

import java.util.List;
import java.util.Map;

public class ZeroVariable extends Instruction implements Command
{
    protected ZeroVariable(String mainVarName, Map<String, Integer> contextMap)
    {
        super(mainVarName, contextMap);
    }

    @Override
    public void execute(Map<String, String> args)
    {
        contextMap.put(mainVarName, 0);
    }

    @Override
    public int getCycles()
    {
        return 1;
    }

    @Override
    public CommandType getType()
    {
        return CommandType.SYNTHETIC;
    }

    @Override
    public List<Command> expand(int level)
    {
        return List.of();
    }

    @Override
    public int getNumberOfArgs()
    {
        return 0;
    }

    @Override
    public String getDisplayFormat(Map<String, String> args)
    {
        return String.format("%s <- 0", mainVarName);
    }
}
