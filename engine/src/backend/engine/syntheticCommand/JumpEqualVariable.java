package backend.engine.syntheticCommand;

import backend.engine.Command;
import backend.engine.CommandType;
import backend.engine.Instruction;

import java.util.List;
import java.util.Map;

public class JumpEqualVariable extends Instruction implements Command
{
    private final String labelArgumentName = "JEVariableLabel";
    private final String variableArgumentName = "variableName";

    public JumpEqualVariable(String mainVarName, Map<String, String> args)
    {
        super(mainVarName, args);
    }

    @Override
    public void execute(Map<String, Integer> contextMap) throws IllegalArgumentException
    {
        try
        {
            String labelName = args.get(labelArgumentName);
            String variableName = args.get(variableArgumentName);

            if (contextMap.containsKey(labelName) && contextMap.containsKey(variableName))
            {
                int mainVarValue = contextMap.get(mainVarName);
                int labelLineNumber = contextMap.get(labelName);
                int variableValue = contextMap.get(variableName);
                if (mainVarValue != variableValue)
                {
                    contextMap.put(PCName, contextMap.get(PCName) + 1); // if we are not equal, we go to the next instruction
                } else
                {
                    contextMap.put(PCName, labelLineNumber); // if we are equal, we go to the label line number
                }
            } else
            {
                throw new IllegalArgumentException("No such label : " + labelName);
            }
        } catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Invalid constant value: " + args.get(variableArgumentName));
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
    public String getDisplayFormat()
    {
        String labelName = args.get(labelArgumentName);
        String checkConstant = args.get(variableArgumentName);
        return String.format("IF %s == %s GOTO %s", mainVarName, checkConstant, labelName);
    }
}
