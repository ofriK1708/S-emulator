package backend.engine.basicCommand;

import backend.engine.Command;
import backend.engine.CommandType;
import backend.engine.Instruction;

import java.util.List;
import java.util.Map;

public class JumpNotZero extends Instruction implements Command
{
    public JumpNotZero(String mainVarName, Map<String, String> args)
    {
        super(mainVarName, args);
    }


    @Override
    public void execute(Map<String, Integer> contextMap) throws IllegalArgumentException
    {
        String labelName = args.get("label");
        if (contextMap.containsKey(labelName))
        {
            int value = contextMap.get(mainVarName);
            int labelLineNumber = contextMap.get(labelName);
            if (value == 0)
            {
                contextMap.put(PCName, contextMap.get(PCName) + 1);
            } else
            {
                contextMap.put(PCName, labelLineNumber);
            }
        } else
        {
            throw new IllegalArgumentException("No such label : " + labelName);
        }
    }


    @Override
    public List<Command> expand(int level)
    {
        return List.of();
    }

    @Override
    public int getCycles()
    {
        return 2;
    }

    @Override
    public CommandType getType()
    {
        return CommandType.BASIC;
    }

    @Override
    public int getNumberOfArgs()
    {
        return 0;
    }

    @Override
    public String getDisplayFormat()
    {
        String labelName = args.get("label");
        return String.format("if %s != 0 GOTO %s", mainVarName, labelName);
    }
}

