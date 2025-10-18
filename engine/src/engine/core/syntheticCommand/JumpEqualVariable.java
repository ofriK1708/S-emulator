package engine.core.syntheticCommand;

import engine.core.Instruction;
import engine.core.basicCommand.Decrease;
import engine.core.basicCommand.Neutral;
import engine.utils.ArchitectureType;
import engine.utils.CommandType;
import engine.utils.ProgramUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JumpEqualVariable extends Instruction
{
    public static final ArchitectureType ARCHITECTURE_TYPE = ArchitectureType.ARCHITECTURE_III;
    public static final int ARCHITECTURE_CREDITS_COST = ARCHITECTURE_TYPE.getCreditsCost();
    public static final String labelArgumentName = "JEVariableLabel";
    public static final String variableArgumentName = "variableName";
    private static int expandLevel;

    public JumpEqualVariable(String mainVarName, Map<String, String> args, String labelName)
    {
        super(mainVarName, args, labelName);
        expandLevel = ProgramUtils.calculateExpandedLevel(this, expandLevel);
    }

    @SuppressWarnings("SameParameterValue")
    protected JumpEqualVariable(String mainVarName, Map<String, String> args, @Nullable String label, @NotNull Instruction derivedFrom, int derivedFromIndex) {
        super(mainVarName, args, label, derivedFrom, derivedFromIndex);
    }

    @Override
    public void execute(@NotNull Map<String, Integer> contextMap) throws IllegalArgumentException
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
    public @NotNull CommandType getType()
    {
        return CommandType.SYNTHETIC;
    }

    @Override
    public @NotNull List<Instruction> expand(@NotNull Map<String, Integer> contextMap, int originalInstructionIndex)
    {

        List<Instruction> instructions = new ArrayList<>();
        String originalLabelName = args.get(labelArgumentName);
        String freeLabelName1 = ProgramUtils.getNextFreeLabelName(contextMap);
        String freeLabelName2 = ProgramUtils.getNextFreeLabelName(contextMap);
        String freeLabelName3 = ProgramUtils.getNextFreeLabelName(contextMap);
        String freeWorkVariableName1 = ProgramUtils.getNextFreeWorkVariableName(contextMap);
        String freeWorkVariableName2 = ProgramUtils.getNextFreeWorkVariableName(contextMap);

        instructions.add(new Assignment(freeWorkVariableName1,
                Map.of(Assignment.sourceArgumentName, mainVarName), label, this, originalInstructionIndex));
        instructions.add(new Assignment(freeWorkVariableName2, Map.of(Assignment.sourceArgumentName,
                args.get(variableArgumentName)), null, this, originalInstructionIndex));
        instructions.add(new JumpZero(freeWorkVariableName1,
                Map.of(JumpZero.labelArgumentName, freeLabelName3), freeLabelName2, this, originalInstructionIndex));
        instructions.add(new JumpZero(freeWorkVariableName2,
                Map.of(JumpZero.labelArgumentName, freeLabelName1), null, this, originalInstructionIndex));
        instructions.add(new Decrease(freeWorkVariableName1, null, null, this, originalInstructionIndex));
        instructions.add(new Decrease(freeWorkVariableName2, null, null, this, originalInstructionIndex));
        instructions.add(new GOTOLabel("",
                Map.of(GOTOLabel.labelArgumentName, freeLabelName2), null, this, originalInstructionIndex));
        instructions.add(new JumpZero(freeWorkVariableName2,
                Map.of(JumpZero.labelArgumentName, originalLabelName), freeLabelName3, this, originalInstructionIndex));
        instructions.add(new Neutral(ProgramUtils.OUTPUT_NAME, null, freeLabelName1, this, originalInstructionIndex));
        return instructions;


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
    public @NotNull String getStringRepresentation()
    {
        String labelName = args.get(labelArgumentName);
        String checkConstant = args.get(variableArgumentName);
        return String.format("IF %s == %s GOTO %s", mainVarName, checkConstant, labelName);
    }
}
