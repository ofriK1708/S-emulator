package core.syntheticCommand;

import core.CommandType;
import core.Instruction;
import core.ProgramEngine;
import core.ProgramUtils;
import core.basicCommand.Decrease;
import core.basicCommand.JumpNotZero;
import core.basicCommand.Neutral;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JumpEqualConstant extends Instruction
{
    private final String labelArgumentName = "JEConstantLabel";
    private final String constantArgumentName = "constantValue";
    private static int expandLevel = -1;

    public JumpEqualConstant(String mainVarName, Map<String, String> args, String labelName)
    {
        super(mainVarName, args, labelName);
        expandLevel = ProgramUtils.calculateExpandedLevel(this, expandLevel);
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
    public List<Instruction> expand(Map<String, Integer> contextMap, int originalInstructionIndex)
    {
        List<Instruction> instructions = new ArrayList<Instruction>();
        String freeLabelName = ProgramUtils.getNextFreeLabelName(contextMap);
        String freeWorkVariableName = ProgramUtils.getNextFreeWorkVariableName(contextMap);
        String originalLabel = args.get(labelArgumentName);

        try
        {
            int checkConstant = Integer.parseInt(args.get(constantArgumentName));
            instructions.add(new Assignment(freeWorkVariableName, Map.of(Assignment.sourceArgumentName, mainVarName),
                    label, this, originalInstructionIndex));
            for (int i = 0; i < checkConstant; i++)
            {
                instructions.add(new JumpZero(freeWorkVariableName,
                        Map.of(JumpZero.labelArgumentName, freeLabelName), null, this, originalInstructionIndex));
                instructions.add(new Decrease(freeWorkVariableName, null, null, this, originalInstructionIndex));
            }
            instructions.add(new JumpNotZero(freeWorkVariableName, Map.of(JumpNotZero.labelArgumentName, freeLabelName),
                    null, this, originalInstructionIndex));
            instructions.add(new GOTOLabel("", Map.of(GOTOLabel.labelArgumentName, originalLabel),
                    null, this, originalInstructionIndex));
            instructions.add(new Neutral(ProgramEngine.outputName, null, freeLabelName, this, originalInstructionIndex));
        } catch (NumberFormatException e)
        {
            throw new RuntimeException("Invalid constant value: " + args.get(constantArgumentName));
        }
        return instructions;
    }

    @Override
    public int getNumberOfArgs(Map<String, Integer> contextMap)
    {
        return 2;
    }

    @Override
    public String getDisplayFormat(int instructionIndex)
    {
        try
        {
            String labelName = args.get(labelArgumentName);
            int checkConstant = Integer.parseInt(args.get(constantArgumentName));
            String CommandPart = String.format("IF %s == %d GOTO %s", mainVarName, checkConstant, labelName);
            return formatDisplay(instructionIndex, CommandPart);
        } catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Invalid constant value: " + args.get(constantArgumentName));
        }
    }

    @Override
    public int getExpandLevel()
    {
        return expandLevel;
    }

}
