package backend.engine.syntheticCommand;

import backend.engine.CommandType;
import backend.engine.Instruction;
import backend.engine.ProgramEngine;
import backend.engine.ProgramUtils;
import backend.engine.basicCommand.Decrease;
import backend.engine.basicCommand.JumpNotZero;
import backend.engine.basicCommand.Neutral;

import java.util.ArrayList;
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
    public List<Instruction> expand(Map<String, Integer> contextMap, int originalInstructionIndex, int expandedInstructionIndex)
    {
        derivedFromIndex = originalInstructionIndex;
        List<Instruction> instructions = new ArrayList<Instruction>();
        String freeLabelName = ProgramUtils.getNextFreeWorkVariableName(contextMap);
        String freeWorkVariableName = ProgramUtils.getNextFreeWorkVariableName(contextMap);
        String srcVarName = args.get(labelArgumentName);
        String originalLabel = args.get(labelArgumentName);
        contextMap.put(freeLabelName, expandedInstructionIndex + 5);
        try
        {
            int checkConstant = Integer.parseInt(args.get(constantArgumentName));
            instructions.add(new Assignment(freeWorkVariableName, Map.of(Assignment.sourceArgumentName, srcVarName),
                    label, this));
            for (int i = 0; i < checkConstant; i++)
            {
                instructions.add(new JumpZero(freeWorkVariableName,
                        Map.of(JumpZero.labelArgumentName, freeLabelName), null, this));
                instructions.add(new Decrease(freeWorkVariableName, null, null, this));
            }
            instructions.add(new JumpNotZero(freeWorkVariableName, Map.of(JumpZero.labelArgumentName, freeLabelName),
                    null, this));
            instructions.add(new GOTOLabel("", Map.of(GOTOLabel.labelArgumentName, originalLabel),
                    null, this));
            instructions.add(new Neutral(ProgramEngine.outputName, null, freeLabelName, this));
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
}
