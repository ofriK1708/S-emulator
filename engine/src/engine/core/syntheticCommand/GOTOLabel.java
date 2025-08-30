package engine.core.syntheticCommand;

import engine.core.CommandType;
import engine.core.Instruction;
import engine.core.basicCommand.Increase;
import engine.core.basicCommand.JumpNotZero;
import engine.utils.ProgramUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GOTOLabel extends Instruction
{
    public static final String labelArgumentName = "gotoLabel";
    private static int expandLevel = -1;

    public GOTOLabel(String mainVarName, Map<String, String> args, String labelName)
    {
        super(mainVarName, args, labelName);
        expandLevel = ProgramUtils.calculateExpandedLevel(this, expandLevel);
    }

    public GOTOLabel(String mainVarName, Map<String, String> args, String label, Instruction derivedFrom, int derivedFromIndex)
    {
        super(mainVarName, args, label, derivedFrom, derivedFromIndex);
        expandLevel = ProgramUtils.calculateExpandedLevel(this, expandLevel);
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
    public int getExpandLevel()
    {
        if (expandLevel == -1)
        {
            expandLevel = ProgramUtils.calculateExpandedLevel(this, expandLevel);
        }
        return expandLevel;
    }

    @Override
    public List<Instruction> expand(Map<String, Integer> contextMap, int originalInstructionIndex)
    {

        List<Instruction> instructions = new ArrayList<>();
        String workVarName = ProgramUtils.getNextFreeWorkVariableName(contextMap);
        String originalLabel = args.get(labelArgumentName);
        instructions.add(new Increase(workVarName, null, label, this, originalInstructionIndex));
        instructions.add(new JumpNotZero(workVarName,
                Map.of(JumpNotZero.labelArgumentName, originalLabel),
                null, this, originalInstructionIndex));
        return instructions;
    }

    @Override
    public int getNumberOfArgs(Map<String, Integer> contextMap)
    {
        return 0;
    }

    @Override
    public String toString()
    {
        String labelName = args.get(labelArgumentName);
        return String.format("GOTO %s", labelName);
    }
}
