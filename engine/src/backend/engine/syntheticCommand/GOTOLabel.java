package backend.engine.syntheticCommand;

import backend.engine.CommandType;
import backend.engine.Instruction;
import backend.engine.ProgramUtils;
import backend.engine.basicCommand.Increase;
import backend.engine.basicCommand.JumpNotZero;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GOTOLabel extends Instruction
{
    public static final String labelArgumentName = "gotoLabel";

    public GOTOLabel(String mainVarName, Map<String, String> args, String labelName)
    {
        super(mainVarName, args, labelName);
    }

    public GOTOLabel(String mainVarName, Map<String, String> args, String label, Instruction derivedFrom)
    {
        super(mainVarName, args, label, derivedFrom);
    }

    @Override
    public void execute(Map<String, Integer> contextMap) throws IllegalArgumentException
    {
        String labelName = args.get(labelArgumentName);
        if (contextMap.containsKey(labelName))
        {
            int labelLineNumber = contextMap.get(labelName);
            contextMap.put(ProgramCounterName, labelLineNumber);
        } else
        {
            throw new IllegalArgumentException("No such label : " + labelName);
        }
    }

    @Override
    public int getCycles()
    {
        return 1;
    }

    @Override
    public CommandType getType()
    {
        return CommandType.SYNTHETIC;
    }

    @Override
    public List<Instruction> expand(Map<String, Integer> contextMap, int originalInstructionIndex, int expandedInstructionIndex)
    {
        derivedFromIndex = originalInstructionIndex;
        List<Instruction> instructions = new ArrayList<Instruction>();
        String workVarName = ProgramUtils.getNextFreeWorkVariableName(contextMap);
        String labelName = args.get(labelArgumentName);
        instructions.add(new Increase(workVarName, null, label, this));
        instructions.add(new JumpNotZero(workVarName,
                Map.of(JumpNotZero.labelArgumentName, labelName),
                labelArgumentName, this));
        return instructions;
    }

    @Override
    public int getNumberOfArgs(Map<String, Integer> contextMap)
    {
        return 0;
    }

    @Override
    public String getDisplayFormat(int instructionNumber)
    {
        String labelName = args.get(labelArgumentName);
        String commandPart = String.format("GOTO %s", labelName);
        return formatDisplay(instructionNumber, commandPart);
    }
}
