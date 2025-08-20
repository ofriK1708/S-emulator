package backend.engine.syntheticCommand;

import backend.engine.CommandType;
import backend.engine.Instruction;
import backend.engine.basicCommand.Increase;

import java.util.ArrayList;
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
    public List<Instruction> expand(Map<String, Integer> contextMap, int originalInstructionIndex, int expandedInstructionIndex)
    {
        derivedFromIndex = originalInstructionIndex;
        List<Instruction> instructions = new ArrayList<Instruction>();
        instructions.add(new ZeroVariable(mainVarName, null, null));
        try
        {
            int constantValue = Integer.parseInt(args.get(valueArgumentName));
            for (int i = 0; i < constantValue; i++)
            {
                instructions.add(new Increase(mainVarName, null, null));
            }
        } catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Invalid value for constant assignment: " + args.get(valueArgumentName));
        }
        return instructions;
    }

    @Override
    public int getNumberOfArgs(Map<String, Integer> contextMap)
    {
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
