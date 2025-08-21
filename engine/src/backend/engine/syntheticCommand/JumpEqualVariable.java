package backend.engine.syntheticCommand;

import backend.engine.CommandType;
import backend.engine.Instruction;
import backend.engine.ProgramEngine;
import backend.engine.ProgramUtils;
import backend.engine.basicCommand.Decrease;
import backend.engine.basicCommand.Neutral;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JumpEqualVariable extends Instruction
{
    private static final String labelArgumentName = "JEVariableLabel";
    private static final String variableArgumentName = "variableName";

    public JumpEqualVariable(String mainVarName, Map<String, String> args, String labelName)
    {
        super(mainVarName, args, labelName);
    }

    @Override
    public void execute(Map<String, Integer> contextMap) throws IllegalArgumentException
    {
        try
        {
            String labelName = args.get(labelArgumentName);
            String variableName = args.get(variableArgumentName);

            if (contextMap.containsKey(labelName))
            {
                int mainVarValue = contextMap.get(mainVarName);
                int labelLineNumber = contextMap.get(labelName);
                int variableValue = contextMap.get(variableName);
                if (mainVarValue != variableValue)
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
    public List<Instruction> expand(Map<String, Integer> contextMap, int originalInstructionIndex, int expandedInstructionIndex)
    {
        derivedFromIndex = originalInstructionIndex;
        List<Instruction> instructions = new ArrayList<Instruction>();
        String originalLabelName = args.get(labelArgumentName);
        String freeLabelName1 = ProgramUtils.getNextFreeWorkVariableName(contextMap);
        String freeLabelName2 = ProgramUtils.getNextFreeWorkVariableName(contextMap);
        String freeLabelName3 = ProgramUtils.getNextFreeWorkVariableName(contextMap);
        String freeWorkVariableName1 = ProgramUtils.getNextFreeWorkVariableName(contextMap);
        String freeWorkVariableName2 = ProgramUtils.getNextFreeWorkVariableName(contextMap);
        contextMap.put(freeLabelName1, expandedInstructionIndex + 8);
        contextMap.put(freeLabelName2, expandedInstructionIndex + 2);
        contextMap.put(freeLabelName3, expandedInstructionIndex + 4);

        instructions.add(new Assignment(freeWorkVariableName1,
                Map.of(Assignment.sourceArgumentName, mainVarName), label, this));
        instructions.add(new Assignment(freeWorkVariableName2, Map.of(Assignment.sourceArgumentName,
                args.get(variableArgumentName)), null, this));
        instructions.add(new JumpZero(freeWorkVariableName1,
                Map.of(JumpZero.labelArgumentName, freeLabelName3), freeLabelName2, this));
        instructions.add(new JumpZero(freeWorkVariableName2,
                Map.of(JumpZero.labelArgumentName, freeLabelName1), null, this));
        instructions.add(new Decrease(freeWorkVariableName1, null, null, this));
        instructions.add(new Decrease(freeWorkVariableName2, null, null, this));
        instructions.add(new GOTOLabel("",
                Map.of(GOTOLabel.labelArgumentName, freeLabelName2), null, this));
        instructions.add(new JumpZero(freeWorkVariableName2,
                Map.of(JumpZero.labelArgumentName, originalLabelName), freeLabelName3, this));
        instructions.add(new Neutral(ProgramEngine.outputName, null, freeLabelName1, this));
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
        String labelName = args.get(labelArgumentName);
        String checkConstant = args.get(variableArgumentName);
        String commandPart = String.format("IF %s == %s GOTO %s", mainVarName, checkConstant, labelName);
        return formatDisplay(instructionIndex, commandPart);
    }
}
