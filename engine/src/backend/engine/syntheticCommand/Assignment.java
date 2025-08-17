package backend.engine.syntheticCommand;

import backend.engine.Command;
import backend.engine.CommandType;
import backend.engine.Instruction;

import java.util.List;
import java.util.Map;

public class Assignment extends Instruction implements Command
{
    private final String sourceArgumentName = "assignedVariable";

    public Assignment(String mainVarName, Map<String, String> args, String labelName)
    {
        super(mainVarName, args, labelName);
    }

    @Override
    public void execute(Map<String, Integer> contextMap) throws IllegalArgumentException
    {
        String sourceName = args.get(sourceArgumentName);
        if (contextMap.containsKey(sourceName))
        {
            int sourceValue = contextMap.get(sourceName);
            contextMap.put(mainVarName, sourceValue);
        } else
        {
            throw new IllegalArgumentException("No such variable: " + sourceName);
        }
    }

    @Override
    public int getCycles()
    {
        return 4;
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
        return 1;
    }

    @Override
    public String getDisplayFormat()
    {
        String sourceName = args.get(sourceArgumentName);
        return String.format("%s <- %s", mainVarName, sourceName);
    }
}
