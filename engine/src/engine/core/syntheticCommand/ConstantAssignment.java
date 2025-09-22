package engine.core.syntheticCommand;

import engine.core.Instruction;
import engine.core.basicCommand.Increase;
import engine.utils.CommandType;
import engine.utils.ProgramUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConstantAssignment extends Instruction
{
    public final static String valueArgumentName = "constantValue";
    public static int expandLevel = -1;

    protected ConstantAssignment(String mainVarName, Map<String, String> args, @Nullable String label, @NotNull Instruction derivedFrom, int derivedFromIndex) {
        super(mainVarName, args, label, derivedFrom, derivedFromIndex);
    }

    public ConstantAssignment(String mainVarName, Map<String, String> args, String labelName)
    {
        super(mainVarName, args, labelName);
        expandLevel = ProgramUtils.calculateExpandedLevel(this, expandLevel);
    }

    @Override
    public void execute(@NotNull Map<String, Integer> contextMap) throws IllegalArgumentException
    {
        try
        {
            int constantValue = Integer.parseInt(args.get(valueArgumentName));
            contextMap.put(mainVarName, constantValue);
            incrementProgramCounter(contextMap);
        } catch(NumberFormatException e)
        {
            throw new IllegalArgumentException("Invalid value for constant assignment: " + args.get(valueArgumentName));
        }
    }

    @Override
    public int getCycles() {
        return 2;
    }

    @Override
    public @NotNull CommandType getType() {
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
    public @NotNull List<Instruction> expand(Map<String, Integer> contextMap, int originalInstructionIndex)
    {

        List<Instruction> instructions = new ArrayList<>();
        instructions.add(new ZeroVariable(mainVarName, null, label, this, originalInstructionIndex));
        try
        {
            int constantValue = Integer.parseInt(args.get(valueArgumentName));
            for (int i = 0; i < constantValue; i++)
            {
                instructions.add(new Increase(mainVarName, null, null, this, originalInstructionIndex));
            }
        } catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Invalid value for constant assignment: " + args.get(valueArgumentName));
        }
        return instructions;
    }

    @Override
    public @NotNull String getStringRepresentation()
    {
        int constantValue = 0;
        try
        {
            constantValue = Integer.parseInt(args.get(valueArgumentName));
        } catch (NumberFormatException ignored)
        {
        }
        return String.format("%s <- %d", mainVarName, constantValue);
    }
}
