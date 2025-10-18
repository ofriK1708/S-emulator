package engine.core;


import dto.engine.ExecutionResultDTO;
import engine.utils.ProgramUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class ProgramRunner {
    private final Map<String, Integer> executedContextMap;
    private final List<Instruction> executedInstructions;

    private ProgramRunner(@NotNull List<Instruction> executedInstructions,
                          @NotNull Map<String, Integer> executedContextMap) {
        this.executedInstructions = executedInstructions;
        this.executedContextMap = executedContextMap;
    }


    public static ProgramRunner createFrom(@NotNull List<Instruction> executedInstructions,
                                           @NotNull Map<String, Integer> executedContextMap) {
        return new ProgramRunner(
                executedInstructions,
                executedContextMap);
    }

    @Contract(pure = true)
    public ExecutionResultDTO run(int expandLevel,
                                  @NotNull Map<String, Integer> arguments) {
        int cyclesCounter = 0;
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
        return new ExecutionResultDTO(
                ProgramUtils.extractSortedArguments(executedContextMap),
                ProgramUtils.extractSortedWorkVars(executedContextMap),
                executedContextMap.get(ProgramUtils.OUTPUT_NAME),
                expandLevel,
                cyclesCounter
        );
    }

    @Contract(pure = true)
    public ExecutionResultDTO run(@NotNull Map<String, Integer> arguments) {
        return run(0, arguments);
    }
}
