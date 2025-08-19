package backend.engine.basicCommand;

import backend.engine.Command;
import backend.engine.CommandType;
import backend.engine.Instruction;

import java.util.List;
import java.util.Map;

public class JumpNotZero extends Instruction
{
    private String labelArgumentName = "JNZLabel";
    public JumpNotZero(String mainVarName, Map<String, String> args, String labelArgumentName)
    {
        super(mainVarName, args, labelArgumentName);
    }

    @Override
    public void execute(Map<String, Integer> contextMap) throws IllegalArgumentException
    {
        String labelName = args.get(labelArgumentName);
        if (contextMap.containsKey(labelName))
        {
            int value = contextMap.get(mainVarName);
            int labelLineNumber = contextMap.get(labelName);
            if (value != 0)
            {
                contextMap.put(ProgramCounterName, labelLineNumber);
            } else
            {
                incrementProgramCounter(contextMap);
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
    public String getDisplayFormat(int instructionNumber)
    {
        String commandPart = String.format("if %s != 0 GOTO %s", mainVarName, args.get(labelArgumentName));
        return formatDisplay(instructionNumber, commandPart);
    }
}

