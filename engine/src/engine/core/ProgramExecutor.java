package engine.core;

import engine.exception.InsufficientCredits;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static engine.utils.ProgramUtils.PC_NAME;

public class ProgramExecutor {
    protected final @NotNull Map<String, Integer> executedContextMap;
    protected final @NotNull List<Instruction> executedInstructions;
    protected final int initialUserCredits;
    protected int runningUserCredits;
    protected int cyclesCount = 0;

    protected ProgramExecutor(@NotNull List<Instruction> executedInstructions,
                              @NotNull Map<String, Integer> executedContextMap,
                              int userCredits) {
        this.executedInstructions = executedInstructions;
        this.executedContextMap = executedContextMap;
        initialUserCredits = runningUserCredits = userCredits;
    }

    protected ProgramExecutor(@NotNull List<Instruction> executedInstructions,
                              @NotNull Map<String, Integer> executedContextMap,
                              @NotNull Map<String, Integer> arguments,
                              int userCredits) {
        this.executedInstructions = executedInstructions;
        this.executedContextMap = executedContextMap;
        initialUserCredits = runningUserCredits = userCredits;
        executedContextMap.putAll(arguments);
    }

    protected int executeInstruction(Instruction instruction) {
        try {
            int currentPC = executedContextMap.get(PC_NAME);
            int creditCost = calcCreditCost(instruction, executedContextMap);
            if (runningUserCredits < creditCost) {
                throw new InsufficientCredits("Insufficient credits to execute instruction" +
                        instruction.getStringRepresentation() + " at PC=" + currentPC,
                        runningUserCredits, creditCost);
            }
            runningUserCredits -= creditCost;
            cyclesCount += creditCost; // credit = cycles
            instruction.execute(executedContextMap);
            return creditCost;

        } catch (Exception e) {
            throw new RuntimeException("Error executing instruction at PC=" +
                    executedContextMap.get(PC_NAME) + ": " + e.getMessage(), e);
        }
    }

    protected int executeInstruction() {
        int currentPC = executedContextMap.get(PC_NAME);
        Instruction instruction = executedInstructions.get(currentPC);
        return executeInstruction(instruction);
    }

    private int calcCreditCost(@NotNull Instruction instruction, @NotNull Map<String, Integer> contextMap) {
        Map<String, Integer> tempContext = new HashMap<>(contextMap);
        instruction.execute(tempContext); // preform a dry run to calc dynamic instructions cost
        return instruction.getCycles();
    }
}
