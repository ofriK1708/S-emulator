package backend.engine.syntheticCommand;

import backend.engine.Command;
import backend.engine.CommandType;
import backend.engine.Instruction;

import java.util.List;
import java.util.Map;

public class JumpEqualConstant extends Instruction
{
    private final String labelArgumentName = "JEConstantLabel";
    private final String constantArgumentName = "constantValue";

    public JumpEqualConstant(String mainVarName, Map<String, String> args, String labelName)
    {
        super(mainVarName, args, labelName);
    }

    @Override
    public void execute(Map<String, Integer> contextMap) throws IllegalArgumentException
    {
        try
        {
            String labelName = args.get(labelArgumentName);
            int checkConstant = Integer.parseInt(args.get(constantArgumentName));
            if (contextMap.containsKey(labelName))
            {
                int mainVarValue = contextMap.get(mainVarName);
                int labelLineNumber = contextMap.get(labelName);
                if (mainVarValue != checkConstant)
                {
                    incrementProgramCounter(contextMap);
                } else
                {
                    contextMap.put(ProgramCounterName, labelLineNumber); // if we are equal, we go to the label line number
                }
            } else
            {
                throw new IllegalArgumentException("No such label : " + labelName);
            }
        } catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Invalid constant value: " + args.get(constantArgumentName));
        }
    }

    @Override
    public int getCycles()
    {
        return 2;
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
        return 2;
    }

    @Override
    public String getDisplayFormat(int instructionNumber)
    {
        try
        {
            String labelName = args.get(labelArgumentName);
            int checkConstant = Integer.parseInt(args.get(constantArgumentName));
            String CommandPart = String.format("IF %s == %d GOTO %s", mainVarName, checkConstant, labelName);
            return formatDisplay(instructionNumber, CommandPart);
        } catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Invalid constant value: " + args.get(constantArgumentName));
        }
    }
}
