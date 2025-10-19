package engine.core.syntheticCommand;

import engine.core.Instruction;
import engine.core.basicCommand.Decrease;
import engine.core.basicCommand.Increase;
import engine.core.basicCommand.JumpNotZero;
import engine.core.basicCommand.Neutral;
import engine.utils.ArchitectureType;
import engine.utils.CommandType;
import engine.utils.ProgramUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Assignment extends Instruction
{
    // region Fields
    private static final @NotNull ArchitectureType ARCHITECTURE_TYPE = ArchitectureType.ARCHITECTURE_III;

    public static final String sourceArgumentName = "assignedVariable";
    private static int expandLevel = -1;
    // endregion

    // region Constructors
    public Assignment(String mainVarName, Map<String, String> args, String labelName)
    {
        super(mainVarName, args, labelName);
        expandLevel = ProgramUtils.calculateExpandedLevel(this, expandLevel);
    }

    public Assignment(String mainVarName, Map<String, String> args, String label, @NotNull Instruction derivedFrom,
                      int derivedFromIndex)
    {
        super(mainVarName, args, label, derivedFrom, derivedFromIndex);
        expandLevel = ProgramUtils.calculateExpandedLevel(this, expandLevel);
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
        String sourceName = args.get(sourceArgumentName);
        if (contextMap.containsKey(sourceName))
        {
            int sourceValue = contextMap.get(sourceName);
            contextMap.put(mainVarName, sourceValue);
            incrementProgramCounter(contextMap);
        } else
        {
            throw new IllegalArgumentException("No such variable: " + sourceName);
        }
    }
    // endregion

    // region Expansion
    @Override
    public @NotNull List<Instruction> expand(@NotNull Map<String, Integer> contextMap, int originalInstructionIndex)
    {
        List<Instruction> instructions = new ArrayList<>();
        String freeLabel1 = ProgramUtils.getNextFreeLabelName(contextMap);
        String freeLabel2 = ProgramUtils.getNextFreeLabelName(contextMap);
        String freeLabel3 = ProgramUtils.getNextFreeLabelName(contextMap);
        String freeWorkVar = ProgramUtils.getNextFreeWorkVariableName(contextMap);
        String srcVarName = args.get(sourceArgumentName);

        instructions.add(new ZeroVariable(mainVarName, null, label, this, originalInstructionIndex));
        instructions.add(new JumpNotZero(srcVarName, Map.of(JumpNotZero.labelArgumentName, freeLabel1), null, this, originalInstructionIndex));
        instructions.add(new GOTOLabel("", Map.of(GOTOLabel.labelArgumentName, freeLabel3), null, this, originalInstructionIndex));
        instructions.add(new Decrease(srcVarName, null, freeLabel1, this, originalInstructionIndex));
        instructions.add(new Increase(freeWorkVar, null, null, this, originalInstructionIndex));
        instructions.add(new JumpNotZero(srcVarName, Map.of(JumpNotZero.labelArgumentName, freeLabel1), null, this, originalInstructionIndex));
        instructions.add(new Decrease(freeWorkVar, null, freeLabel2, this, originalInstructionIndex));
        instructions.add(new Increase(mainVarName, null, null, this, originalInstructionIndex));
        instructions.add(new Increase(srcVarName, null, null, this, originalInstructionIndex));
        instructions.add(new JumpNotZero(freeWorkVar, Map.of(JumpNotZero.labelArgumentName, freeLabel2), null, this, originalInstructionIndex));
        instructions.add(new Neutral(mainVarName, null, freeLabel3, this, originalInstructionIndex));

        return instructions;
    }
    // endregion

    // region Info
    @Override
    public int getCycles() {
        return 4;
    }

    @Override
    public @NotNull CommandType getType() {
        return CommandType.SYNTHETIC;
    }

    @Override
    public int getExpandLevel() {
        if (expandLevel == -1) {
            expandLevel = ProgramUtils.calculateExpandedLevel(this, expandLevel);
        }
        return expandLevel;
    }

    @Override
    public @NotNull String getStringRepresentation()
    {
        String sourceName = args.get(sourceArgumentName);
        return String.format("%s <- %s", mainVarName, sourceName);
    }
    // endregion
}
