package engine.core.syntheticCommand;

import engine.core.Instruction;
import engine.core.ProgramEngine;
import engine.core.basicCommand.Neutral;
import engine.utils.CommandType;
import engine.utils.ProgramUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JumpEqualFunction extends Instruction {
    private final static String labelArgumentName = "JEFunctionLabel";
    private final @NotNull ProgramEngine mainEngine;
    private final @NotNull Quote functionQuoteToCheck;

    public JumpEqualFunction(String mainVarName, Map<String, String> args, String label, Instruction derivedFrom, int derivedFromIndex, @NotNull ProgramEngine mainFunction) {
        super(mainVarName, args, label, derivedFrom, derivedFromIndex);
        this.mainEngine = mainFunction;
        functionQuoteToCheck = new Quote("", this.args, "", mainEngine);
    }

    public JumpEqualFunction(String mainVarName, Map<String, String> args, String label, @NotNull ProgramEngine mainFunction) {
        super(mainVarName, args, label);
        this.mainEngine = mainFunction;
        functionQuoteToCheck = new Quote("", this.args, "", mainEngine);
    }

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
    public @NotNull List<Instruction> expand(Map<String, Integer> contextMap, int originalInstructionIndex) {
        List<Instruction> expandedInstructions = new ArrayList<>();
        String freeWorkVar = ProgramUtils.getNextFreeWorkVariableName(contextMap);
        if (!label.isBlank()) {
            expandedInstructions.add(new Neutral(ProgramUtils.OUTPUT_NAME, Map.of(), label, this, originalInstructionIndex));
        }
        expandedInstructions.add(new Quote(freeWorkVar, args, label, this, originalInstructionIndex, mainEngine));
        expandedInstructions.add(new JumpEqualVariable(mainVarName, Map.of(JumpEqualVariable.labelArgumentName,
                args.get(labelArgumentName), JumpEqualVariable.variableArgumentName, freeWorkVar), "", this, originalInstructionIndex));
        return expandedInstructions;
    }

    @Override
    public @NotNull String getStringRepresentation() {
        String labelName = args.get(labelArgumentName);
        return String.format("if %s == %s GOTO %s", mainVarName, functionQuoteToCheck.getFunctionStringRepresentation(),
                labelName);
    }
}
