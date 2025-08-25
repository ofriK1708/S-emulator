package core.syntheticCommand;

import core.CommandType;
import core.Instruction;
import core.ProgramUtils;
import core.basicCommand.Decrease;
import core.basicCommand.Increase;
import core.basicCommand.JumpNotZero;
import core.basicCommand.Neutral;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Assignment extends Instruction
{
    public static final String sourceArgumentName = "assignedVariable";
    private static int expandLevel = -1;
    public Assignment(String mainVarName, Map<String, String> args, String labelName)
    {
        super(mainVarName, args, labelName);
        expandLevel = ProgramUtils.calculateExpandedLevel(this, expandLevel);
    }

    public Assignment(String mainVarName, Map<String, String> args, String label, Instruction derivedFrom, int derivedFromIndex)
    {
        super(mainVarName, args, label, derivedFrom, derivedFromIndex);
        expandLevel = ProgramUtils.calculateExpandedLevel(this, expandLevel);
    }

    @Override
    public void execute(Map<String, Integer> contextMap) throws IllegalArgumentException
    {
        String sourceName = args.get(sourceArgumentName);
        if (contextMap.containsKey(sourceName))
        {
            int sourceValue = contextMap.get(sourceName);
            contextMap.put(mainVarName, sourceValue);
            incrementProgramCounter(contextMap);
        } else
        {
            throw new IllegalArgumentException("No such variable: " + sourceName);
        }
    }

    @Override
    public int getCycles()
    {
        return 4;
    }

    @Override
    public CommandType getType()
    {
        return CommandType.SYNTHETIC;
    }

    @Override
    public List<Instruction> expand(Map<String, Integer> contextMap, int originalInstructionIndex)
    {
        List<Instruction> instructions = new ArrayList<Instruction>();
        String freeLabel1 = ProgramUtils.getNextFreeLabelName(contextMap);
        String freeLabel2 = ProgramUtils.getNextFreeLabelName(contextMap);
        String freeLabel3 = ProgramUtils.getNextFreeLabelName(contextMap);
        String freeWorkVar = ProgramUtils.getNextFreeWorkVariableName(contextMap);
        String srcVarName = args.get(sourceArgumentName);

        instructions.add(new ZeroVariable(mainVarName, null, label, this, originalInstructionIndex));
        instructions.add(new JumpNotZero(srcVarName, Map.of(JumpNotZero.labelArgumentName, freeLabel1), null, this, originalInstructionIndex));
        instructions.add(new GOTOLabel("", Map.of(GOTOLabel.labelArgumentName, freeLabel3), null, this, originalInstructionIndex));
        instructions.add(new Decrease(srcVarName, null, freeLabel1, this, originalInstructionIndex));
        instructions.add(new Increase(freeWorkVar, null, null, this, originalInstructionIndex));
        instructions.add(new JumpNotZero(srcVarName, Map.of(JumpNotZero.labelArgumentName, freeLabel1), null, this, originalInstructionIndex));
        instructions.add(new Decrease(freeWorkVar, null, freeLabel2, this, originalInstructionIndex));
        instructions.add(new Increase(mainVarName, null, null, this, originalInstructionIndex));
        instructions.add(new Increase(srcVarName, null, null, this, originalInstructionIndex));
        instructions.add(new JumpNotZero(freeWorkVar, Map.of(JumpNotZero.labelArgumentName, freeLabel2), null, this, originalInstructionIndex));
        instructions.add(new Neutral(mainVarName, null, freeLabel3, this, originalInstructionIndex));

        return instructions;
    }

    @Override
    public int getNumberOfArgs(Map<String, Integer> contextMap)
    {
        return 1;
    }

    @Override
    public String getDisplayFormat(int instructionIndex)
    {
        String sourceName = args.get(sourceArgumentName);
        String commandPart = String.format("%s <- %s", mainVarName, sourceName);
        return formatDisplay(instructionIndex, commandPart);
    }

    @Override
    public int getExpandLevel()
    {
        return expandLevel;
    }
}
