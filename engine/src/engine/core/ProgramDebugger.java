package engine.core;

import dto.engine.DebugStateChangeResultDTO;
import dto.engine.ExecutionResultInfoDTO;
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
public class ProgramDebugger {
    // region Final fields set via Builder
    private final boolean isMainProgram;
    private final @NotNull String programName;
    private final @NotNull ArchitectureType architectureType;
    private final @NotNull List<Instruction> currentDebugInstructions;
    private final @NotNull Map<String, Integer> currentDebugContext;
    private final int initialUserCredits;
    private final int expandLevel;
    // endregion

    // region State-related fields
    private final @NotNull List<Map<String, Integer>> debugStateHistory = new ArrayList<>();
    private final @NotNull List<Integer> debugCyclesHistory = new ArrayList<>();
    private @NotNull Map<String, Integer> debugArguments = new HashMap<>();
    private boolean debugMode = false;
    private int currentDebugPC = 0;
    private int runningUserCredits;
    // endregion

    // region class ctor and builder

    /**
     * Private constructor to enforce usage of Builder.
     *
     * @param builder The Builder instance
     */
    private ProgramDebugger(Builder builder) {
        this.currentDebugInstructions = builder.instructions;
        this.currentDebugContext = builder.contextMap;
        this.expandLevel = builder.expandLevel;
        this.initialUserCredits = this.runningUserCredits = builder.userCredits;
        this.isMainProgram = builder.isMainProgram;
        this.programName = builder.programName;
        this.architectureType = builder.architectureType;
    }

    // Static factory method to get a new builder instance
    static Builder builder(@NotNull ProgramExecutable executable, int userCredits, int expandLevel) {
        return new Builder(executable, userCredits, expandLevel);
    }

    /**
     * Constructs an ExecutionResultDTO representing the current debug state.
     * used when debug is finished or stopped.
     *
     * @return ExecutionResultDTO with current debug information.
     */
    public ExecutionResultInfoDTO getDebugFinishedExecutionResult() {
        return new ExecutionResultInfoDTO(
                isMainProgram,
                programName,
                architectureType,
                debugArguments,
                ProgramUtils.extractSortedVariables(currentDebugContext),
                currentDebugContext.get(OUTPUT_NAME),
                expandLevel,
                initialUserCredits - runningUserCredits,
                initialUserCredits - runningUserCredits
        );
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
        currentDebugContext.putAll(arguments);
        debugArguments = new HashMap<>(arguments);
        // Save initial state with zero cycles
        debugStateHistory.add(new HashMap<>(currentDebugContext));
        debugCyclesHistory.add(0);
        debugMode = true;
        currentDebugPC = currentDebugContext.get(PC_NAME);
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
                ProgramUtils.extractSortedVariables(currentDebugContext),
                isDebugFinished()
        );
    }

    public DebugStateChangeResultDTO stepBack() {
        if (!debugMode) {
            throw new IllegalStateException("Debug session not started");
        }

        if (currentDebugPC == 0) {
            // Can't go back further than the first instruction
            throw new IllegalStateException("Already at the beginning of the program");
        }
        // make sure we have enough credits to step back
        int lastCycleCreditCost = debugCyclesHistory.getLast();
        int lastPcValue = debugStateHistory.getLast().get(PC_NAME);
        String lastInstructionStr = currentDebugInstructions.get(lastPcValue).getStringRepresentation();
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
        currentDebugContext.putAll(debugStateHistory.getLast());
        currentDebugPC = currentDebugContext.get(PC_NAME);

        String instructionStr = currentDebugInstructions.get(currentDebugPC).getStringRepresentation();

        // Prepare result DTO
        return new DebugStateChangeResultDTO(
                ProgramUtils.extractSortedVariables(currentDebugContext),
                false // stepping back can never finish the program
        );
    }

    public DebugStateChangeResultDTO resume() {
        if (!debugMode) {
            throw new IllegalStateException("Debug session not started");
        }

        // Reset breakpoint statuses at start of resume

        // Execute remaining instructions, stopping at breakpoints
        while (currentDebugPC < currentDebugInstructions.size()) {
            executeStep();
        }

        // Prepare result DTO
        return new DebugStateChangeResultDTO(
                ProgramUtils.extractSortedVariables(currentDebugContext),
                true // resume always finishes the program
        );
    }

    public DebugStateChangeResultDTO stop() {
        if (!debugMode) {
            throw new IllegalStateException("Debug session not started");
        }
        // prepare result DTO
        return new DebugStateChangeResultDTO(
                ProgramUtils.extractSortedVariables(currentDebugContext),
                true // stopping the debug session marks it as finished
        );
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

        public Builder mainProgram(boolean isMainProgram) {
            this.isMainProgram = isMainProgram;
            return this;
        }

        public Builder programName(@NotNull String programName) {
            this.programName = programName;
            return this;
        }

        public Builder setArchitectureType(@NotNull ArchitectureType architectureType) {
            this.architectureType = architectureType;
            return this;
        }

        /**
         * Sets all metadata fields at once.
         *
         * @param executionMetadata The ExecutionMetadata instance
         * @return Builder instance for chaining
         */
        public Builder executionMetadata(ExecutionMetadata executionMetadata) {
            this.isMainProgram = executionMetadata.isMainProgram();
            this.programName = executionMetadata.programName();
            this.architectureType = executionMetadata.architectureType();
            return this;
        }

        public ProgramDebugger build() {
            return new ProgramDebugger(this);
        }
    }

    // endregion

    // region public getters

    public @NotNull String getProgramName() {
        return programName;
    }
    // region private helpers
    private void executeStep() {
        Instruction instruction = currentDebugInstructions.get(currentDebugPC);

        try {
            int creditCost = instruction.getCycles();
            if (runningUserCredits < creditCost) {
                throw new InsufficientCredits("Insufficient credits to execute instruction" +
                        instruction.getStringRepresentation() + " at PC=" + currentDebugPC, runningUserCredits,
                        creditCost);
            }
            runningUserCredits -= creditCost;
            instruction.execute(currentDebugContext);
            currentDebugPC = currentDebugContext.get(PC_NAME);
            // Save state
            debugStateHistory.add(new HashMap<>(currentDebugContext));
            debugCyclesHistory.add(creditCost); // cycles = credit cost for this instruction;

        } catch (Exception e) {
            throw new RuntimeException("Error executing debug instruction at PC=" +
                    currentDebugPC + ": " + e.getMessage(), e);
        }
    }

    // endregion

    private boolean isDebugFinished() {
        return debugMode && currentDebugPC >= currentDebugInstructions.size();
    }

    // endregion
}
