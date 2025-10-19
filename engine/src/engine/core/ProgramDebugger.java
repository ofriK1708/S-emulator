package engine.core;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static engine.utils.ProgramUtils.PC_NAME;

public class ProgramDebugger {
    // Enhanced debugger fields with proper state management:

    private final int debugExpandLevel;
    private final List<Map<String, Integer>> debugStateHistory = new ArrayList<>();
    private final List<Integer> debugCyclesHistory = new ArrayList<>(); // Track cycles per step
    private @NotNull
    final List<Instruction> currentDebugInstructions;
    private @NotNull
    final Map<String, Integer> currentDebugContext;
    private @NotNull Map<String, Integer> debugArguments = new HashMap<>();
    private boolean debugMode = false;
    private int currentDebugPC = 0;
    private int totalDebugCycles = 0; // Monotonic cycle counter

    private ProgramDebugger(@NotNull List<Instruction> currentDebugInstructions,
                            @NotNull Map<String, Integer> currentDebugContext,
                            int debugExpandLevel) {
        this.currentDebugInstructions = currentDebugInstructions;
        this.currentDebugContext = currentDebugContext;
        this.debugExpandLevel = debugExpandLevel;

    }

    public static @NotNull ProgramDebugger create(@NotNull InstructionSequence instructionSequence,
                                                  int expandLevel) {
        Map<String, Integer> currentDebugContext = instructionSequence.getContextMapCopy(expandLevel);
        List<Instruction> currentDebugInstructions = instructionSequence.getInstructionsCopy(expandLevel);
        return new ProgramDebugger(
                currentDebugInstructions,
                currentDebugContext,
                expandLevel);

    }

    public void startDebugSession(int expandLevel, @NotNull Map<String, Integer> arguments) {
        // Apply arguments to context map at the specified expand level
        currentDebugContext.putAll(arguments);
        debugArguments = new HashMap<>(arguments);
        // Save initial state with zero cycles
        debugStateHistory.add(new HashMap<>(currentDebugContext));
        debugCyclesHistory.add(0);

    }

    public void debugStep() {
        if (!debugMode) {
            throw new IllegalStateException("Debug session not started");
        }

        if (currentDebugPC >= currentDebugInstructions.size()) {
            System.out.println("Debug session completed - reached end of program");
            return;
        }

        // Execute current instruction
        executeStep();
    }

    public void debugStepBackward() {
        if (!debugMode) {
            throw new IllegalStateException("Debug session not started");
        }

        if (debugStateHistory.size() <= 1) {
            // Can't go back further than initial state
            return;
        }

        // Remove current state and restore previous
        debugStateHistory.removeLast();
        debugCyclesHistory.removeLast();

        // Restore previous state
        currentDebugContext.putAll(debugStateHistory.getLast());
        currentDebugPC = currentDebugContext.get(Instruction.ProgramCounterName);

        // Update totalDebugCycles to match the current state
        totalDebugCycles = debugCyclesHistory.getLast();
    }

    public void debugResume() {
        if (!debugMode) {
            throw new IllegalStateException("Debug session not started");
        }

        // Reset breakpoint statuses at start of resume

        // Execute remaining instructions, stopping at breakpoints
        while (currentDebugPC < currentDebugInstructions.size()) {
            executeStep();
        }
        System.out.println("Resume completed - reached end of program");
    }

    private void executeStep() {
        Instruction currentInstruction = currentDebugInstructions.get(currentDebugPC);

        try {
            // Execute instruction
            currentInstruction.execute(currentDebugContext);

            // Add cycles
            totalDebugCycles += currentInstruction.getCycles();

            // Update PC from context (instruction may have modified it)
            currentDebugPC = currentDebugContext.get(PC_NAME);

            // Save state
            debugStateHistory.add(new HashMap<>(currentDebugContext));
            debugCyclesHistory.add(totalDebugCycles);

        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Error executing debug instruction at PC=" +
                    currentDebugPC + ": " + e.getMessage(), e);
        }
    }

    public void stopDebugSession() {
        if (debugMode) {
            // Finalize statistics even if session was stopped manually
        }

        debugMode = false;
    }


    public int getCurrentDebugPC() {
        return debugMode ? currentDebugPC : -1;
    }

    public boolean isInDebugMode() {
        return debugMode;
    }

    public boolean isDebugFinished() {
        return debugMode && currentDebugPC >= currentDebugInstructions.size();
    }

    public int getCurrentDebugCycles() {
        if (!debugMode) {
            throw new IllegalStateException("Debug session not active");
        }

        return totalDebugCycles;
    }
}
