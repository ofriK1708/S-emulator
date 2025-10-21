package engine.core;


import dto.engine.ExecutionResultValuesDTO;
import engine.exception.InsufficientCredits;
import engine.utils.ProgramUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import static engine.utils.ProgramUtils.PC_NAME;

/**
 * Class responsible for running a program represented by a list of instructions
 * and maintaining the execution context. doesn't affect's the original instructions or context.
 */
public class ProgramRunner {
    private final @NotNull Map<String, Integer> executedContextMap;
    private final @NotNull List<Instruction> executedInstructions;
    private final int initialUserCredits;
    private int runningUserCredits;
    private int cyclesCount = 0;

    private ProgramRunner(@NotNull List<Instruction> executedInstructions,
                          @NotNull Map<String, Integer> executedContextMap,
                          int userCredits) {
        this.executedInstructions = executedInstructions;
        this.executedContextMap = executedContextMap;
        initialUserCredits = runningUserCredits = userCredits;
    }

    static @NotNull ProgramRunner createFrom(@NotNull ProgramExecutable executable,
                                                    int userCredits) {
        return new ProgramRunner(
                executable.instructions(),
                executable.contextMap(),
                userCredits);
    }

    @Contract(pure = true)
    public @NotNull ExecutionResultValuesDTO run(int expandLevel,
                                                 @NotNull Map<String, Integer> arguments) {
        executedContextMap.putAll(arguments);
        while (executedContextMap.get(PC_NAME) < executedInstructions.size()) {
            int currentPC = executedContextMap.get(PC_NAME);
            Instruction instruction = executedInstructions.get(currentPC);
            try {
                int instructionCreditsCost = instruction.getCycles();
                if (runningUserCredits < instructionCreditsCost) {
                    throw new InsufficientCredits("Insufficient credits to execute instruction" +
                            instruction.getStringRepresentation() + " at PC=" + currentPC, runningUserCredits,
                            instructionCreditsCost);
                }
                runningUserCredits -= instructionCreditsCost;
                cyclesCount += instructionCreditsCost;
                instruction.execute(executedContextMap);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Error executing instruction at PC=" + currentPC + ": " + e.getMessage(), e);
            }
        }
        return new ExecutionResultValuesDTO(
                executedContextMap.get(ProgramUtils.OUTPUT_NAME),
                expandLevel,
                cyclesCount,
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
