package engine.core;


import dto.engine.ExecutionResultValuesDTO;
import engine.exception.InstructionExecutionException;
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
public class ProgramRunner extends ProgramExecutor {

    private ProgramRunner(@NotNull List<Instruction> executedInstructions,
                          @NotNull Map<String, Integer> executedContextMap,
                          @NotNull Map<String, Integer> arguments,
                          int userCredits) {
        super(executedInstructions, executedContextMap, userCredits);
        executedContextMap.putAll(arguments);
    }

    static @NotNull ProgramRunner createInnerRunner(@NotNull ProgramExecutable executable,
                                                    @NotNull Map<String, Integer> arguments) {
        return new ProgramRunner(
                executable.instructions(),
                executable.contextMap(),
                arguments,
                Integer.MAX_VALUE);  // no credit limit for inner runs
    }

    static @NotNull ProgramRunner createMainRunner(@NotNull ProgramExecutable executable,
                                                   @NotNull Map<String, Integer> arguments,
                                                   int userCredits) {

        return new ProgramRunner(
                executable.instructions(),
                executable.contextMap(),
                arguments,
                userCredits);
    }

    @Contract(pure = true)
    public @NotNull ExecutionResultValuesDTO run() throws InstructionExecutionException, InsufficientCredits {
        while (executedContextMap.get(PC_NAME) < executedInstructions.size()) {
            executeInstruction(executedInstructions.get(executedContextMap.get(PC_NAME)));
        }
        return new ExecutionResultValuesDTO(
                executedContextMap.get(ProgramUtils.OUTPUT_NAME),
                cyclesCount,
                initialUserCredits - runningUserCredits,
                ProgramUtils.extractSortedArguments(executedContextMap),
                ProgramUtils.extractSortedWorkVars(executedContextMap)
        );
    }

}

