package engine.core;


import dto.engine.ExecutionResultInfoDTO;
import engine.exception.InsufficientCredits;
import engine.utils.ArchitectureType;
import engine.utils.ProgramUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
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
    private final @Nullable ExecutionResultInfoDTO executionResultInfo;
    private final @NotNull Map<String, Integer> arguments;
    private boolean isFinished = false;
    private final int initialUserCredits;
    private int runningUserCredits;
    private int cyclesCount = 0;

    private ProgramRunner(@NotNull List<Instruction> executedInstructions,
                          @NotNull Map<String, Integer> executedContextMap,
                          @Nullable ExecutionResultInfoDTO executionResultInfo,
                          @NotNull Map<String, Integer> arguments,
                          int userCredits) {
        this.executedInstructions = executedInstructions;
        this.executionResultInfo = executionResultInfo;
        this.executedContextMap = executedContextMap;
        this.arguments = arguments;
        initialUserCredits = runningUserCredits = userCredits;
        executedContextMap.putAll(arguments);

    }

    static @NotNull ProgramRunner createInnerRunner(@NotNull ProgramExecutable executable,
                                                    @NotNull Map<String, Integer> arguments) {
        return new ProgramRunner(
                executable.instructions(),
                executable.contextMap(),
                null,
                arguments,
                Integer.MAX_VALUE);  // no credit limit for inner runs
    }

    static @NotNull ProgramRunner createMainRunner(@NotNull ProgramExecutable executable,
                                                   @NotNull ExecutionMetadata executionMetadata,
                                                   @NotNull Map<String, Integer> arguments,
                                                   int userCredits) {

        ExecutionResultValues executionResultValues = new ExecutionResultValues(arguments);
        ExecutionStatistics executionStatistics = new ExecutionStatistics();
        ExecutionResultInfoDTO executionResultInfo = new ExecutionResultInfoDTO(
                executionMetadata,
                executionResultValues,
                executionStatistics
        );
        return new ProgramRunner(
                executable.instructions(),
                executable.contextMap(),
                executionResultInfo,
                arguments,
                userCredits);

    }

    @Contract(pure = true)
    private void run(boolean isMainRun) {
        while (executedContextMap.get(PC_NAME) < executedInstructions.size()) {
            int currentPC = executedContextMap.get(PC_NAME);
            Instruction instruction = executedInstructions.get(currentPC);
            try {
                int instructionCreditsCost = calcCreditCost(instruction, executedContextMap);
                if (runningUserCredits < instructionCreditsCost) {
                    throw new InsufficientCredits("Insufficient credits to execute instruction" +
                            instruction.getStringRepresentation() + " at PC=" + currentPC, runningUserCredits,
                            instructionCreditsCost);
                }
                runningUserCredits -= instructionCreditsCost;
                cyclesCount += instructionCreditsCost;
                if (isMainRun) {
                    saveState(instruction.getArchitectureType());
                }
                instruction.execute(executedContextMap);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Error executing instruction at PC=" + currentPC + ": " + e.getMessage(), e);
            }
        }
        if (isMainRun) {
            saveState(null); // end of execution, no instruction executed
        }
        isFinished = true;
    }

    private void saveState(@Nullable ArchitectureType instructionExecutedArchType) {
        if (executionResultInfo != null) {
            executionResultInfo.executionStatistics().setCycleCount(cyclesCount);
            executionResultInfo.executionStatistics().setCreditCost(initialUserCredits - runningUserCredits);
            executionResultInfo.executionResultValues().setOutput(
                    executedContextMap.get(ProgramUtils.OUTPUT_NAME));
            executionResultInfo.executionResultValues().setArguments(
                    ProgramUtils.extractSortedArguments(executedContextMap));
            executionResultInfo.executionResultValues().setWorkVariables(
                    ProgramUtils.extractSortedWorkVars(executedContextMap));
            if (instructionExecutedArchType != null) {
                executionResultInfo.executionStatistics().incrementNumberOfInstructionsExecuted();
                executionResultInfo.executionStatistics()
                        .incrementInstructionsForArchitecture(instructionExecutedArchType);
            }
        } else {
            throw new IllegalStateException("Cannot save state: executionResultInfo is null.");
        }
    }

    public @NotNull ExecutionResultInfoDTO getExecutionResultInfo() {
        if (executionResultInfo != null) {
            return executionResultInfo;
        } else {
            throw new IllegalStateException("Cannot get ExecutionResultInfoDTO: executionResultInfo is null.");
        }
    }

    public InnerRunResult innerRun() {
        run(false);
        return new InnerRunResult(
                executedContextMap.get(ProgramUtils.OUTPUT_NAME),
                cyclesCount
        );
    }

    public void start() {
        run(true);
    }

    public int getEndOfRunCyclesCount() {
        if (isFinished) {
            return cyclesCount;
        } else {
            throw new IllegalStateException("Cannot get cycles count: program has not finished running.");
        }
    }


    private int calcCreditCost(Instruction instruction, Map<String, Integer> contextMap) {
        Map<String, Integer> tempContext = new HashMap<>(contextMap);
        instruction.execute(tempContext); // preform a dry run to calc dynamic instructions cost
        return instruction.getCycles();
    }
}

