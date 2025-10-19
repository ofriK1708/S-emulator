package engine.core.syntheticCommand;

import engine.core.FunctionManager;
import engine.core.Instruction;
import engine.core.basicCommand.Neutral;
import engine.utils.ArchitectureType;
import engine.utils.CommandType;
import engine.utils.ProgramUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JumpEqualFunction extends Instruction {
    // region Fields
    private static final @NotNull ArchitectureType ARCHITECTURE_TYPE = ArchitectureType.ARCHITECTURE_IV;
    private static final int ARCHITECTURE_CREDITS_COST = ARCHITECTURE_TYPE.getCreditsCost();
    private final static String labelArgumentName = "JEFunctionLabel";
    private final @NotNull FunctionManager functionManager;
    private final @NotNull Quote functionQuoteToCheck;
    // endregion

    // region Constructors
    public JumpEqualFunction(String mainVarName, Map<String, String> args,
                             String label, @NotNull FunctionManager functionManager,
                             int quoteIndex, @NotNull String enclosingFunctionName) {
        super(mainVarName, args, label);
        this.functionManager = functionManager;
        functionQuoteToCheck = Quote.createInitialQuote("", this.args, "", functionManager, quoteIndex,
                enclosingFunctionName);
    }
    // endregion

    // region Architecture
    @Override
    public int getArchitectureCreditsCost() {
        return ARCHITECTURE_CREDITS_COST;
    }

    @Override
    public @NotNull ArchitectureType getArchitectureType() {
        return ARCHITECTURE_TYPE;
    }
    // endregion

    // region Execution
    @Override
    public void execute(@NotNull Map<String, Integer> contextMap) throws IllegalArgumentException {
        String labelName = args.get(labelArgumentName);
        if (contextMap.containsKey(labelName)) {
            int mainVarValue = contextMap.get(mainVarName);
            int labelLineNumber = contextMap.get(labelName);
            if (mainVarValue != functionQuoteToCheck.executeAndGetResult(contextMap)) {
                incrementProgramCounter(contextMap);
            } else {
                contextMap.put(ProgramCounterName, labelLineNumber);
            }
        } else {
            throw new IllegalArgumentException("No such label : " + labelName);
        }
    }
    // endregion

    // region Expansion
    @Override
    public @NotNull List<Instruction> expand(@NotNull Map<String, Integer> contextMap, int originalInstructionIndex) {
        List<Instruction> expandedInstructions = new ArrayList<>();
        String freeWorkVar = ProgramUtils.getNextFreeWorkVariableName(contextMap);
        if (!label.isBlank()) {
            expandedInstructions.add(new Neutral(ProgramUtils.OUTPUT_NAME, Map.of(), label,
                    this, originalInstructionIndex));
        }
        expandedInstructions.add(new Quote(freeWorkVar, label, functionQuoteToCheck, this,
                originalInstructionIndex));
        expandedInstructions.add(new JumpEqualVariable(mainVarName, Map.of(
                JumpEqualVariable.labelArgumentName, args.get(labelArgumentName),
                JumpEqualVariable.variableArgumentName, freeWorkVar),
                "", this, originalInstructionIndex));
        return expandedInstructions;
    }
    // endregion

    // region Info
    @Override
    public int getCycles() {
        final int JEFunctionOverhead = 6;
        return JEFunctionOverhead + functionQuoteToCheck.getFunctionCycles();
    }

    @Override
    public @NotNull CommandType getType() {
        return CommandType.SYNTHETIC;
    }

    @Override
    public int getExpandLevel() {
        return ProgramUtils.calculateExpandedLevel(this);
    }

    @Override
    public @NotNull String getStringRepresentation() {
        String labelName = args.get(labelArgumentName);
        return String.format("if %s == %s GOTO %s", mainVarName, functionQuoteToCheck.getFunctionStringRepresentation(),
                labelName);
    }
    // endregion
}
