package engine.core;


import dto.engine.ExecutionResultValuesDTO;
import engine.utils.ProgramUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Class responsible for running a program represented by a list of instructions
 * and maintaining the execution context. doesn't affect's the original instructions or context.
 */
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
    public ExecutionResultValuesDTO run(int expandLevel,
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
        return new ExecutionResultValuesDTO(
                executedContextMap.get(ProgramUtils.OUTPUT_NAME),
                cyclesCounter,
                expandLevel,
                ProgramUtils.extractSortedArguments(executedContextMap),
                ProgramUtils.extractSortedWorkVars(executedContextMap)
        );
    }

    @Contract(pure = true)
    public ExecutionResultValuesDTO run(@NotNull Map<String, Integer> arguments) {
        return run(0, arguments);
    }
}
