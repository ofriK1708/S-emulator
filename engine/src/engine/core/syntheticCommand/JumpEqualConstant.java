package engine.core.syntheticCommand;

import engine.core.Instruction;
import engine.core.basicCommand.Decrease;
import engine.core.basicCommand.JumpNotZero;
import engine.core.basicCommand.Neutral;
import engine.utils.ArchitectureType;
import engine.utils.CommandType;
import engine.utils.ProgramUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JumpEqualConstant extends Instruction
{
    private static final @NotNull ArchitectureType ARCHITECTURE_TYPE = ArchitectureType.ARCHITECTURE_III;
    private static final int ARCHITECTURE_CREDITS_COST = ARCHITECTURE_TYPE.getCreditsCost();
    public final String labelArgumentName = "JEConstantLabel";
    public final String constantArgumentName = "constantValue";
    private static int expandLevel = -1;

    public JumpEqualConstant(String mainVarName, Map<String, String> args, String labelName)
    {
        super(mainVarName, args, labelName);
        expandLevel = ProgramUtils.calculateExpandedLevel(this, expandLevel);
    }

    @Override
    public int getArchitectureCreditsCost() {
        return 0;
    }

    @Override
    public @NotNull ArchitectureType getArchitectureType() {
        return null;
    }

    @Override
    public void execute(@NotNull Map<String, Integer> contextMap) throws IllegalArgumentException
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
    public @NotNull CommandType getType()
    {
        return CommandType.SYNTHETIC;
    }

    @Override
    public @NotNull List<Instruction> expand(@NotNull Map<String, Integer> contextMap, int originalInstructionIndex)
    {
        List<Instruction> instructions = new ArrayList<>();
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
            instructions.add(new Neutral(ProgramUtils.OUTPUT_NAME, null, freeLabelName, this, originalInstructionIndex));
        } catch (NumberFormatException e)
        {
            throw new RuntimeException("Invalid constant value: " + args.get(constantArgumentName));
        }
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
        int checkConstant = 0;
        try
        {
            checkConstant = Integer.parseInt(args.get(constantArgumentName));
        } catch (NumberFormatException ignored)
        {
        }
        return String.format("IF %s == %d GOTO %s", mainVarName, checkConstant, labelName);
    }

}
