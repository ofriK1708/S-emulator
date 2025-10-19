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
    private final @NotNull Map<String, Integer> executedContextMap;
    private final @NotNull List<Instruction> executedInstructions;
    private final int initialUserCredits;
    private int runningUserCredits;

    private ProgramRunner(@NotNull List<Instruction> executedInstructions,
                          @NotNull Map<String, Integer> executedContextMap,
                          int userCredits) {
        this.executedInstructions = executedInstructions;
        this.executedContextMap = executedContextMap;
        initialUserCredits = runningUserCredits = userCredits;
    }


    public static @NotNull ProgramRunner createFrom(@NotNull List<Instruction> executedInstructions,
                                                    @NotNull Map<String, Integer> executedContextMap,
                                                    int userCredits) {
        return new ProgramRunner(
                executedInstructions,
                executedContextMap,
                userCredits);
    }

    @Contract(pure = true)
    public @NotNull ExecutionResultValuesDTO run(int expandLevel,
                                                 @NotNull Map<String, Integer> arguments) {
        int cyclesCounter = 0;
        executedContextMap.putAll(arguments);
        while (executedContextMap.get(Instruction.ProgramCounterName) < executedInstructions.size()) {
            int currentPC = executedContextMap.get(Instruction.ProgramCounterName);
            Instruction instruction = executedInstructions.get(currentPC);
            try {
                instruction.execute(executedContextMap);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Error executing instruction at PC=" + currentPC + ": " + e.getMessage(), e);
            }
            cyclesCounter += instruction.getCycles();
            runningUserCredits -= instruction.getArchitectureCreditsCost();
            if (runningUserCredits < 0) {
                throw new RuntimeException("Insufficient user credits to continue execution at PC=" + currentPC);
            }
        }
        return new ExecutionResultValuesDTO(
                executedContextMap.get(ProgramUtils.OUTPUT_NAME),
                expandLevel,
                cyclesCounter,
                initialUserCredits - runningUserCredits,
                ProgramUtils.extractSortedArguments(executedContextMap),
                ProgramUtils.extractSortedWorkVars(executedContextMap)
        );
    }

    @Contract(pure = true)
    public ExecutionResultValuesDTO run(@NotNull Map<String, Integer> arguments) {
        return run(0, arguments);
    }
}
