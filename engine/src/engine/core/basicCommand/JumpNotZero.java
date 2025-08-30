package engine.core.basicCommand;

import engine.core.CommandType;
import engine.core.Instruction;

import java.util.List;
import java.util.Map;

public class JumpNotZero extends Instruction
{
    public static final String labelArgumentName = "JNZLabel";
    private static final int expandLevel = 0;

    public JumpNotZero(String mainVarName, Map<String, String> args, String labelArgumentName)
    {
        super(mainVarName, args, labelArgumentName);
    }

    public JumpNotZero(String mainVarName, Map<String, String> args, String label, Instruction derivedFrom, int derivedFromIndex)
    {
        super(mainVarName, args, label, derivedFrom, derivedFromIndex);
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
    public List<Instruction> expand(Map<String, Integer> contextMap, int originalInstructionIndex)
    {
        return List.of(this);
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
    public int getExpandLevel()
    {
        return expandLevel;
    }

    @Override
    public String toString()
    {
        return String.format("if %s != 0 GOTO %s", mainVarName, args.get(labelArgumentName));
    }
}

