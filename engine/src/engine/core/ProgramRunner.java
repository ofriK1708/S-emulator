package engine.core;


import dto.engine.ExecutionResult;
import engine.utils.ProgramUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ProgramRunner {
    private final List<Map<String, Integer>> executedContextMapsByExpandLevel;
    private final Set<Integer> levelsWhichProgramHasBeenRunAt = new HashSet<>();
    private final List<List<Instruction>> executedInstructionsByExpandLevel;

    private ProgramRunner(@NotNull List<Map<String, Integer>> executedContextMapsByExpandLevel,
                          @NotNull List<List<Instruction>> executedInstructionsByExpandLevel) {
        this.executedContextMapsByExpandLevel = executedContextMapsByExpandLevel;
        this.executedInstructionsByExpandLevel = executedInstructionsByExpandLevel;
    }

    public static ProgramRunner createFrom(@NotNull InstructionSequence instructionSequence) {
        List<Map<String, Integer>> executedContextMapsByExpandLevel = new ArrayList<>();
        for (Map<String, Integer> contextMap : instructionSequence.getContextMapsByExpandLevel()) {
            executedContextMapsByExpandLevel.add(new HashMap<>(contextMap));
        }
        List<List<Instruction>> executedInstructionsByExpandLevel = instructionSequence.getInstructionExpansionLevels();
        return new ProgramRunner(
                executedContextMapsByExpandLevel,
                executedInstructionsByExpandLevel);
    }

    @Contract(pure = true)
    public ExecutionResult run(int expandLevel,
                               @NotNull Map<String, Integer> arguments) {
        if (levelsWhichProgramHasBeenRunAt.contains(expandLevel)) {
            clearPreviousRunData(expandLevel);
        }
        int cyclesCounter = 0;
        List<Instruction> executedInstructions = executedInstructionsByExpandLevel.get(expandLevel);
        Map<String, Integer> executedContextMap = executedContextMapsByExpandLevel.get(expandLevel);
        executedContextMap.putAll(arguments);
        while (executedContextMap.get(Instruction.ProgramCounterName) < executedInstructions.size()) {
            int currentPC = executedContextMap.get(Instruction.ProgramCounterName);
            Instruction instruction = executedInstructions.get(currentPC);
            try {
                instruction.execute(executedContextMap);
                cyclesCounter += instruction.getCycles();
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Error executing instruction at PC=" + currentPC + ": " + e.getMessage(), e);
            }
        }
        levelsWhichProgramHasBeenRunAt.add(expandLevel);
        return new ExecutionResult(
                ProgramUtils.extractSortedArguments(executedContextMap),
                ProgramUtils.extractSortedWorkVars(executedContextMap),
                executedContextMap.get(ProgramUtils.OUTPUT_NAME),
                expandLevel,
                cyclesCounter
        );
    }

    @Contract(pure = true)
    public ExecutionResult run(@NotNull Map<String, Integer> arguments) {
        return run(0, arguments);
    }

    private void clearPreviousRunData(int expandLevel) {
        Map<String, Integer> currentContextMap = executedContextMapsByExpandLevel.get(expandLevel);
        for (Map.Entry<String, Integer> entry : currentContextMap.entrySet()) {
            if (ProgramUtils.isVariable(entry.getKey())) {
                entry.setValue(0);
            }
        }
        currentContextMap.put(Instruction.ProgramCounterName, 0); // Reset PC
    }
}
