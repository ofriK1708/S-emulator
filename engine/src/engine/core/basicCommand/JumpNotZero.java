package engine.core.basicCommand;

import engine.core.Instruction;
import engine.utils.CommandType;
import org.jetbrains.annotations.NotNull;

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
    public void execute(@NotNull Map<String, Integer> contextMap) throws IllegalArgumentException
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
    public @NotNull List<Instruction> expand(Map<String, Integer> contextMap, int originalInstructionIndex)
    {
        return List.of(this);
    }

    @Override
    public int getCycles()
    {
        return 2;
    }

    @Override
    public @NotNull CommandType getType()
    {
        return CommandType.BASIC;
    }

    @Override
    public int getExpandLevel()
    {
        return expandLevel;
    }

    @Override
    public @NotNull String getStringRepresentation()
    {
        return String.format("if %s != 0 GOTO %s", mainVarName, args.get(labelArgumentName));
    }
}

