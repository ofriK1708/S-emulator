package engine.core.syntheticCommand;

import engine.core.CommandType;
import engine.core.Instruction;
import engine.core.ProgramUtils;
import engine.core.basicCommand.JumpNotZero;
import engine.core.basicCommand.Neutral;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JumpZero extends Instruction
{
    public static final String labelArgumentName = "JZLabel";
    private static int expandLevel;

    public JumpZero(String mainVarName, Map<String, String> args, String labelName)
    {
        super(mainVarName, args, labelName);
        expandLevel = ProgramUtils.calculateExpandedLevel(this, expandLevel);
    }

    public JumpZero(String mainVarName, Map<String, String> args, String label, Instruction derivedFrom, int derivedFromIndex)
    {
        super(mainVarName, args, label, derivedFrom, derivedFromIndex);
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
                incrementProgramCounter(contextMap);
            } else
            {
                contextMap.put(ProgramCounterName, labelLineNumber);
            }
        } else
        {
            throw new IllegalArgumentException("No such label : " + labelName);
        }
    }

    @Override
    public List<Instruction> expand(Map<String, Integer> contextMap, int originalInstructionIndex)
    {
        List<Instruction> instructions = new ArrayList<>();
        String freeLabelName = ProgramUtils.getNextFreeLabelName(contextMap);
        String labelName = args.get(labelArgumentName);

        instructions.add(new JumpNotZero(mainVarName,
                Map.of(JumpNotZero.labelArgumentName, freeLabelName), label, this, originalInstructionIndex));
        instructions.add(new GOTOLabel("",
                Map.of(GOTOLabel.labelArgumentName, labelName), null, this, originalInstructionIndex));
        instructions.add(new Neutral(ProgramUtils.outputName, null, freeLabelName, this, originalInstructionIndex));

        return instructions;
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
    public int getNumberOfArgs(Map<String, Integer> contextMap)
    {
        return 1;
    }

    @Override
    public int getExpandLevel()
    {
        return expandLevel;
    }

    @Override
    public String toString()
    {
        String labelName = args.get(labelArgumentName);
        return String.format("IF %s == 0 GOTO %s", mainVarName, labelName);
    }
}
