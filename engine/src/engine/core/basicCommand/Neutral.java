package engine.core.basicCommand;

import engine.core.Instruction;
import engine.utils.ArchitectureType;
import engine.utils.CommandType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class Neutral extends Instruction
{
    public static final ArchitectureType ARCHITECTURE_TYPE = ArchitectureType.ARCHITECTURE_I;
    public static final int ARCHITECTURE_CREDITS_COST = ARCHITECTURE_TYPE.getCreditsCost();
    private static final int expandLevel = 0;
    public Neutral(String mainVarName, Map<String, String> args, String labelName)
    {
        super(mainVarName, args, labelName);
    }

    public Neutral(String mainVarName, Map<String, String> args, String label, @NotNull Instruction derivedFrom,
                   int derivedFromIndex)
    {
        super(mainVarName, args, label, derivedFrom, derivedFromIndex);
    }

    @Override
    public void execute(@NotNull Map<String, Integer> contextMap) throws IllegalArgumentException
    {
        incrementProgramCounter(contextMap);
    }

    @Override
    public @NotNull List<Instruction> expand(Map<String, Integer> contextMap, int originalInstructionIndex)
    {
        return List.of(this);
    }

    @Override
    public int getCycles()
    {
        return 0;
    }

    @Override
    public @NotNull CommandType getType()
    {
        return CommandType.BASIC;
    }

    @Override
    public int getExpandLevel()
    {
        return expandLevel;
    }

    @Override
    public @NotNull String getStringRepresentation()
    {
        return String.format("%s <- %s", mainVarName, mainVarName);
    }
}
