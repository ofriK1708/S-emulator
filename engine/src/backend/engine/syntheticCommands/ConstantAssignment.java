package backend.engine.syntheticCommands;

import backend.engine.Command;
import backend.engine.CommandType;
import backend.engine.Instruction;

import java.util.List;
import java.util.Map;

public class ConstantAssignment extends Instruction implements Command
{
    protected ConstantAssignment(String mainVarName, Map<String, String> args)
    {
        super(mainVarName, args);
    }

    @Override
    public void execute(Map<String, Integer> contextMap) throws IllegalArgumentException
    {
        try
        {
            int constantValue = Integer.parseInt(args.get("value"));
            contextMap.put(mainVarName, constantValue);
        } catch(NumberFormatException e)
        {
            throw new IllegalArgumentException("Invalid value for constant assignment: " + args.get("value"));
        }
    }

    @Override
    public int getCycles() {
        return 2;
    }

    @Override
    public CommandType getType() {
        return CommandType.SYNTHETIC;
    }

    @Override
    public List<Command> expand(int level) {
        return List.of();
    }

    @Override
    public int getNumberOfArgs() {
        return 1;
    }

    @Override
    public String getDisplayFormat() {
        try
        {
            int constantValue = Integer.parseInt(args.get("value"));
            return String.format("%s <- %d", mainVarName, constantValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid value for constant assignment: " + args.get("value"));
        }
    }
}
