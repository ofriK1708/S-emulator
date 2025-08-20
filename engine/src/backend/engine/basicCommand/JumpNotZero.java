package backend.engine.basicCommand;

import backend.engine.CommandType;
import backend.engine.Instruction;

import java.util.List;
import java.util.Map;

public class JumpNotZero extends Instruction
{
    public static final String labelArgumentName = "JNZLabel";

    public JumpNotZero(String mainVarName, Map<String, String> args, String labelArgumentName)
    {
        super(mainVarName, args, labelArgumentName);
    }

    public JumpNotZero(String mainVarName, Map<String, String> args, String label, Instruction derivedFrom)
    {
        super(mainVarName, args, label, derivedFrom);
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
    public List<Instruction> expand(Map<String, Integer> contextMap, int originalInstructionIndex, int expandedInstructionIndex)
    {
        // This command does not expand further, it is already in its final form.
        // It can be used directly in the program.
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
    public int getNumberOfArgs(Map<String, Integer> contextMap)
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

