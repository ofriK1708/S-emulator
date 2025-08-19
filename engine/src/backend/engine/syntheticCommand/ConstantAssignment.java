package backend.engine.syntheticCommand;

import backend.engine.Command;
import backend.engine.CommandType;
import backend.engine.Instruction;

import java.util.List;
import java.util.Map;

public class ConstantAssignment extends Instruction
{
    private final String valueArgumentName = "constantValue";

    public ConstantAssignment(String mainVarName, Map<String, String> args, String labelName)
    {
        super(mainVarName, args, labelName);
    }

    @Override
    public void execute(Map<String, Integer> contextMap) throws IllegalArgumentException
    {
        try
        {
            int constantValue = Integer.parseInt(args.get(valueArgumentName));
            contextMap.put(mainVarName, constantValue);
            incrementProgramCounter(contextMap);
        } catch(NumberFormatException e)
        {
            throw new IllegalArgumentException("Invalid value for constant assignment: " + args.get(valueArgumentName));
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
    public String getDisplayFormat(int instructionNumber) {
        try
        {
            int constantValue = Integer.parseInt(args.get(valueArgumentName));
            String commandPart = String.format("%s <- %d", mainVarName, constantValue);
            return formatDisplay(instructionNumber, commandPart);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid value for constant assignment: " + args.get(valueArgumentName));
        }
    }
}
