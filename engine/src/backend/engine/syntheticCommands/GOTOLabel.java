package backend.engine.syntheticCommands;

import backend.engine.Command;
import backend.engine.CommandType;
import backend.engine.Instruction;

import java.util.List;
import java.util.Map;

public class GOTOLabel extends Instruction implements Command
{
    protected GOTOLabel(String mainVarName, Map<String, Integer> contextMap)
    {
        super(mainVarName, contextMap);
    }

    @Override
    public void execute(Map<String, String> args)
    {
        String labelName = args.get("label");
        if (contextMap.containsKey(labelName))
        {
            int labelLineNumber = contextMap.get(labelName);
            contextMap.put(PCName, labelLineNumber);
        } else
        {
            throw new IllegalArgumentException("No such label : " + labelName);
        }
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
        String labelName = args.get("label");
        return String.format("GOTO %s", labelName);
    }
}
