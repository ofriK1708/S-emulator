package engine.core.syntheticCommand;

import engine.core.Instruction;
import engine.core.basicCommand.JumpNotZero;
import engine.core.basicCommand.Neutral;
import engine.utils.ArchitectureType;
import engine.utils.CommandType;
import engine.utils.ProgramUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JumpZero extends Instruction
{
    // region Fields
    private static final @NotNull ArchitectureType ARCHITECTURE_TYPE = ArchitectureType.ARCHITECTURE_III;

    public static final String labelArgumentName = "JZLabel";
    private static int expandLevel;
    // endregion

    // region Constructors
    public JumpZero(String mainVarName, Map<String, String> args, String labelName)
    {
        super(mainVarName, args, labelName);
        expandLevel = ProgramUtils.calculateExpandedLevel(this, expandLevel);
    }

    public JumpZero(String mainVarName, Map<String, String> args, String label, @NotNull Instruction derivedFrom,
                    int derivedFromIndex)
    {
        super(mainVarName, args, label, derivedFrom, derivedFromIndex);
    }
    // endregion

    // region Architecture

    @Override
    public @NotNull ArchitectureType getArchitectureType() {
        return ARCHITECTURE_TYPE;
    }
    // endregion

    // region Execution
    @Override
    public void execute(@NotNull Map<String, Integer> contextMap) throws IllegalArgumentException
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
    // endregion

    // region Expansion
    @Override
    public @NotNull List<Instruction> expand(@NotNull Map<String, Integer> contextMap, int originalInstructionIndex)
    {
        List<Instruction> instructions = new ArrayList<>();
        String freeLabelName = ProgramUtils.getNextFreeLabelName(contextMap);
        String labelName = args.get(labelArgumentName);

        instructions.add(new JumpNotZero(mainVarName,
                Map.of(JumpNotZero.labelArgumentName, freeLabelName), label, this, originalInstructionIndex));
        instructions.add(new GOTOLabel("",
                Map.of(GOTOLabel.labelArgumentName, labelName), null, this, originalInstructionIndex));
        instructions.add(new Neutral(ProgramUtils.OUTPUT_NAME, null, freeLabelName, this, originalInstructionIndex));

        return instructions;
    }
    // endregion

    // region Info
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
    public @NotNull String getStringRepresentation()
    {
        String labelName = args.get(labelArgumentName);
        return String.format("IF %s == 0 GOTO %s", mainVarName, labelName);
    }
    // endregion
}
