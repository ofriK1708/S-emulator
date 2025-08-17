package backend.engine.syntheticCommand;

import backend.engine.Command;
import backend.engine.CommandType;
import backend.engine.Instruction;

import java.util.List;
import java.util.Map;

public class GOTOLabel extends Instruction implements Command
{
    private final String labelArgumentName = "gotoLabel";

    public GOTOLabel(String mainVarName, Map<String, String> args, String labelName)
    {
        super(mainVarName, args, labelName);
    }

    @Override
    public void execute(Map<String, Integer> contextMap) throws IllegalArgumentException
    {
        String labelName = args.get(labelArgumentName);
        if (contextMap.containsKey(labelName))
        {
            int labelLineNumber = contextMap.get(labelName);
            contextMap.put(ProgramCounter, labelLineNumber);
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
    public String getDisplayFormat()
    {
        String labelName = args.get(labelArgumentName);
        return String.format("GOTO %s", labelName);
    }
}
