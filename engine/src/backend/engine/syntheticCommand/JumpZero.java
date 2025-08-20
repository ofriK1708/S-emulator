package backend.engine.syntheticCommand;

import backend.engine.CommandType;
import backend.engine.Instruction;
import backend.engine.ProgramEngine;
import backend.engine.ProgramUtils;
import backend.engine.basicCommand.JumpNotZero;
import backend.engine.basicCommand.Neutral;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JumpZero extends Instruction
{
    public static final String labelArgumentName = "JZLabel";

    public JumpZero(String mainVarName, Map<String, String> args, String labelName)
    {
        super(mainVarName, args, labelName);
    }

    public JumpZero(String mainVarName, Map<String, String> args, String label, Instruction derivedFrom)
    {
        super(mainVarName, args, label, derivedFrom);
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
    public List<Instruction> expand(Map<String, Integer> contextMap, int originalInstructionIndex, int expandedInstructionIndex)
    {
        derivedFromIndex = originalInstructionIndex;
        List<Instruction> instructions = new ArrayList<Instruction>();
        String freeLabelName = ProgramUtils.getNextFreeWorkVariableName(contextMap);
        contextMap.put(freeLabelName, expandedInstructionIndex + 2);
        String labelName = args.get(labelArgumentName);

        instructions.add(new JumpNotZero(mainVarName,
                Map.of(JumpNotZero.labelArgumentName, freeLabelName), label, this));
        instructions.add(new GOTOLabel("",
                Map.of(GOTOLabel.labelArgumentName, labelName), null, this));
        instructions.add(new Neutral(ProgramEngine.outputName, null, freeLabelName, this));

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
    public String getDisplayFormat(int instructionNumber) {
        String labelName = args.get(labelArgumentName);
        String commandPart = String.format("if %s == 0 GOTO %s", mainVarName, labelName);
        return formatDisplay(instructionNumber, commandPart);
    }
}
