package engine.core;

import dto.engine.DebugStateChangeResultDTO;
import dto.engine.FullExecutionResultDTO;
import engine.exception.InstructionExecutionException;
import engine.exception.InsufficientCredits;
import engine.utils.ArchitectureType;
import engine.utils.ProgramUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static engine.utils.ProgramUtils.OUTPUT_NAME;
import static engine.utils.ProgramUtils.PC_NAME;

/**
 * Enhanced ProgramDebugger with proper state management for stepping
 * both forward and backward through program execution.
 * <p>
 * Tracks execution state history and cycle counts.
 * Supports starting, stepping, resuming, and stopping debug sessions.
 * </p>
 */
public class ProgramDebugger extends ProgramExecutor {
    // region Final fields set via Builder
    private final boolean isMainProgram;
    private final @NotNull String programName;
    private final @NotNull ArchitectureType architectureType;
    private final int expandLevel;
    // endregion

    // region State-related fields
    private final @NotNull List<Map<String, Integer>> debugStateHistory = new ArrayList<>();
    private final @NotNull List<Integer> debugCyclesHistory = new ArrayList<>();
    private @NotNull Map<String, Integer> debugArguments = new HashMap<>();
    private boolean debugMode = false;
    // endregion

    // region class ctor and builder

    /**
     * Private constructor to enforce usage of Builder.
     *
     * @param builder The Builder instance
     */
    private ProgramDebugger(Builder builder) {
        super(builder.instructions, builder.contextMap, builder.userCredits);
        this.expandLevel = builder.expandLevel;
        this.isMainProgram = builder.isMainProgram;
        this.programName = builder.programName;
        this.architectureType = builder.architectureType;
    }

    // Static factory method to get a new builder instance
    static Builder builder(@NotNull ProgramExecutable executable, int userCredits, int expandLevel) {
        return new Builder(executable, userCredits, expandLevel);
    }

    /**
     * Starts a debug session by initializing the context with provided arguments.
     *
     * @param arguments Map of argument names to their integer values.
     * @return ProgramDebugger instance for method chaining.
     * @throws IllegalStateException if a debug session is already active.
     */
    public ProgramDebugger start(@NotNull Map<String, Integer> arguments) {
        // Apply arguments to context map at the specified expand level
        if (debugMode) {
            throw new IllegalStateException("Debug session already started");
        }
        executedContextMap.putAll(arguments);
        debugArguments = new HashMap<>(arguments);
        // Save initial state with zero cycles
        debugStateHistory.add(new HashMap<>(executedContextMap));
        debugMode = true;
        return this;
    }
    // endregion

    // region debug actions

    public DebugStateChangeResultDTO stepOver() {
        if (!debugMode) {
            throw new IllegalStateException("Debug session not started");
        }
        // Execute current instruction
        executeStep();
        // Prepare result DTO
        return new DebugStateChangeResultDTO(
                ProgramUtils.extractSortedVariables(executedContextMap),
                getPC(),
                cyclesCount,
                runningUserCredits,
                isDebugFinished()
        );
    }

    public DebugStateChangeResultDTO stepBack() {
        if (!debugMode) {
            throw new IllegalStateException("Debug session not started");
        }

        if (getPC() == 0) {
            // Can't go back further than the first instruction
            throw new IllegalStateException("Already at the beginning of the program");
        }
        // make sure we have enough credits to step back
        int lastCycleCreditCost = debugCyclesHistory.getLast();
        int lastPcValue = debugStateHistory.getLast().get(PC_NAME);
        String lastInstructionStr = executedInstructions.get(lastPcValue).getStringRepresentation();
        if (runningUserCredits < lastCycleCreditCost) {
            throw new InsufficientCredits("Not enough credits to step backward and execute instruction "
                    + lastInstructionStr + " at pc=" + lastPcValue,
                    runningUserCredits,
                    lastCycleCreditCost);
        }
        // charge credits for stepping back, and increase total debug cycles
        runningUserCredits -= lastCycleCreditCost;

        // Remove current state and restore previous
        debugCyclesHistory.removeLast();
        debugStateHistory.removeLast();

        // Restore previous state
        executedContextMap.putAll(debugStateHistory.getLast());

        // Prepare result DTO
        return new DebugStateChangeResultDTO(
                ProgramUtils.extractSortedVariables(executedContextMap),
                getPC(),
                cyclesCount,
                runningUserCredits,
                false // stepping back can never finish the program
        );
    }

    public DebugStateChangeResultDTO resume() {
        if (!debugMode) {
            throw new IllegalStateException("Debug session not started");
        }

        // Reset breakpoint statuses at start of resume

        // Execute remaining instructions, stopping at breakpoints
        while (getPC() < executedInstructions.size()) {
            executeStep();
        }

        // Prepare result DTO
        return new DebugStateChangeResultDTO(
                ProgramUtils.extractSortedVariables(executedContextMap),
                getPC(),
                cyclesCount,
                runningUserCredits,
                true // resume always finishes the program
        );
    }

    public DebugStateChangeResultDTO stop() {
        if (!debugMode) {
            throw new IllegalStateException("Debug session not started");
        }
        // prepare result DTO
        return new DebugStateChangeResultDTO(
                ProgramUtils.extractSortedVariables(executedContextMap),
                getPC(),
                cyclesCount,
                runningUserCredits,
                true // stopping the debug session marks it as finished
        );
    }

    /**
     * Constructs an ExecutionResultDTO representing the current debug state.
     * used when debug is finished or stopped.
     *
     * @return ExecutionResultDTO with current debug information.
     */
    public FullExecutionResultDTO getDebugFinishedExecutionResult() {
        return new FullExecutionResultDTO(
                isMainProgram,
                programName,
                architectureType,
                debugArguments,
                ProgramUtils.extractSortedVariables(executedContextMap),
                executedContextMap.get(OUTPUT_NAME),
                expandLevel,
                initialUserCredits - runningUserCredits,
                initialUserCredits - runningUserCredits
        );
    }
    // endregion

    // region public getters

    public @NotNull String getProgramName() {
        return programName;
    }

    // region private helpers
    private void executeStep() throws InsufficientCredits, InstructionExecutionException {
        Instruction currentInstruction = executedInstructions.get(executedContextMap.get(PC_NAME));
        int creditCost = executeInstruction(currentInstruction);
        // Save state
        debugStateHistory.add(new HashMap<>(executedContextMap));
        debugCyclesHistory.add(creditCost); // cycles = credit cost for this instruction;

    }

    // endregion

    private boolean isDebugFinished() {
        return debugMode && getPC() >= executedInstructions.size();
    }

    /**
     * Builder for ProgramDebugger.
     * Allows setting required and optional parameters.
     *
     * @see ProgramDebugger
     */
    static class Builder {
        // Required parameters
        private final List<Instruction> instructions;
        private final Map<String, Integer> contextMap;
        private final int userCredits;
        private final int expandLevel;

        // Optional parameters with default values
        private boolean isMainProgram = false;
        private String programName = " ";
        private ArchitectureType architectureType = ArchitectureType.ARCHITECTURE_I; // the default architecture

        private Builder(@NotNull ProgramExecutable executable, int userCredits, int expandLevel) {
            this.instructions = executable.instructions();
            this.contextMap = executable.contextMap();
            this.userCredits = userCredits;
            this.expandLevel = expandLevel;
        }

        public Builder isMainProgram(boolean isMainProgram) {
            this.isMainProgram = isMainProgram;
            return this;
        }

        public Builder programName(@NotNull String programName) {
            this.programName = programName;
            return this;
        }

        public Builder architectureType(@NotNull ArchitectureType architectureType) {
            this.architectureType = architectureType;
            return this;
        }

        public ProgramDebugger build() {
            return new ProgramDebugger(this);
        }
    }

    // endregion
}
