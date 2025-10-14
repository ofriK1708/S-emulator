package system.controller;

import dto.engine.ExecutionStatisticsDTO;
import dto.engine.ProgramDTO;
import engine.core.ProgramEngine;
import engine.exception.FunctionNotFound;
import engine.exception.LabelNotExist;
import engine.generated_2.SProgram;
import engine.utils.ProgramUtils;
import jakarta.xml.bind.JAXBException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import system.file.processing.XMLHandler;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import static system.utils.systemUtils.validateFile;

public class LocalEngineController implements EngineController {
    public static @NotNull Predicate<String> validArgumentValueCheck = ProgramUtils.validArgumentCheck;
    private final @NotNull XMLHandler xmlHandler;
    private @Nullable ProgramEngine engine;
    private ProgramEngine mainEngine;
    private String mainEngineName;
    private int maxExpandLevel = 0;

    public LocalEngineController() {
        try {
            this.xmlHandler = new XMLHandler();
        } catch (JAXBException e) {
            throw new RuntimeException("Failed to initialize XMLHandler " + e.getMessage());
        }
    }
    // Add these fields to Debugger:
    private boolean inDebugSession = false;

    private void createEngine(@NotNull SProgram program) throws LabelNotExist, FunctionNotFound {
        engine = new ProgramEngine(program);
        mainEngine = engine;
        mainEngineName = engine.getProgramName();
        maxExpandLevel = engine.getMaxExpandLevel();
    }

    public @NotNull ProgramDTO setLoadedProgram(@NotNull String functionName) {
        if (engine == null) {
            throw new IllegalStateException("Program has not been set");
        }
        engine = functionName.equals(mainEngineName) ? mainEngine : engine.getFunctionByName(functionName);
        maxExpandLevel = engine.getMaxExpandLevel();
        return engine.getProgramByExpandLevelDTO(0);
    }

    public int getMaxExpandLevel() {
        if (engine == null) {
            throw new IllegalStateException("Program has not been set");
        }
        return maxExpandLevel;
    }

    @Override
    public void LoadProgramFromFile(@NotNull Path xmlFilePath)
            throws LabelNotExist, JAXBException, IOException, FunctionNotFound {
        if (!xmlFilePath.getFileName().toString().endsWith(".xml")) {
            throw new IllegalArgumentException("File must be an XML file");
        }
        validateFile(xmlFilePath);
        SProgram program = xmlHandler.unmarshallFile(xmlFilePath);
        createEngine(program);

    }


    public void runLoadedProgram(int expandLevel, @NotNull Map<String, Integer> arguments) {
        if (engine == null) {
            throw new IllegalStateException("Program has not been set");
        }
        if (expandLevel < 0 || expandLevel > maxExpandLevel) {
            throw new IllegalArgumentException("Expand level must be between 0 and " + maxExpandLevel);
        }
        engine.run(expandLevel, arguments, true);
    }


    public int getLastExecutionNumberOfCycles() {
        if (engine == null) {
            throw new IllegalStateException("Program has not been set");
        }
        return engine.getLastExecutionCycles();
    }

    public @NotNull ProgramDTO getProgramByExpandLevel(int expandLevel) {
        if (engine == null) {
            throw new IllegalStateException("Program has not been set");
        }
        if (expandLevel < 0 || expandLevel > maxExpandLevel) {
            throw new IllegalArgumentException("Expand level must be between 0 and " + maxExpandLevel);
        }
        return engine.getProgramByExpandLevelDTO(expandLevel);
    }

    @Override
    public ProgramDTO getBasicProgram() {
        return getProgramByExpandLevel(0);
    }

    @Override
    public void loadProgram(String programName) {
        throw new UnsupportedOperationException("local controller does not support loading multiple programs");
    }

    public void clearLoadedProgram() {
        engine = null;
        maxExpandLevel = 0;
    }

    public ExecutionStatisticsDTO getLastExecutionStatistics() {
        if (engine == null) {
            throw new IllegalStateException("Program has not been set");
        }
        return engine.getAllExecutionStatistics().getLast();
    }

    public @NotNull Set<String> getAllVariablesAndLabelsNames(int expandLevel, boolean includeLabels) {
        if (engine == null) {
            throw new IllegalStateException("Program has not been set");
        }
        return engine.getAllVariablesNames(expandLevel, includeLabels);
    }

    public @NotNull Map<String, Integer> getSortedArguments(int expandLevel) {
        if (engine == null) {
            throw new IllegalStateException("Program has not been set");
        }
        return engine.getSortedArguments(expandLevel);
    }

    public @NotNull Map<String, Integer> getSortedArguments() {
        if (engine == null) {
            throw new IllegalStateException("Program has not been set");
        }
        return engine.getSortedArguments();
    }

    public @NotNull Map<String, String> getFunctionsSet() {
        if (engine == null) {
            throw new IllegalStateException("Program has not been set");
        }
        return engine.getAllFunctionNamesAndStrName();
    }


    public @NotNull Integer getProgramResult(int expandLevel) {
        if (engine == null) {
            throw new IllegalStateException("No program loaded");
        }
        return engine.getOutput(expandLevel);
    }

    public @NotNull Map<String, Integer> getWorkVars(int expandLevel) {
        if (engine == null) {
            throw new IllegalStateException("Program has not been set");
        }
        return engine.getSortedWorkVars(expandLevel);
    }

    public List<ExecutionStatisticsDTO> getAllExecutionStatistics() {
        if (engine == null) {
            throw new IllegalStateException("Program has not been set");
        }
        return engine.getAllExecutionStatistics();
    }

    public void startDebugSession(int expandLevel, @NotNull Map<String, Integer> arguments) {
        if (engine == null) {
            throw new IllegalStateException("Program has not been set");
        }
        if (expandLevel < 0 || expandLevel > maxExpandLevel) {
            throw new IllegalArgumentException("Expand level must be between 0 and " + maxExpandLevel);
        }

        engine.startDebugSession(expandLevel, arguments);
        inDebugSession = true;
    }

    public void debugStep() {
        if (engine == null || !inDebugSession) {
            throw new IllegalStateException("Debug session not active");
        }
        engine.debugStep();
        if (engine.isDebugFinished()) {
            engine.finalizeDebugExecution();
        }
    }

    public void debugStepBackward() {
        if (engine == null || !inDebugSession) {
            throw new IllegalStateException("Debug session not active");
        }
        engine.debugStepBackward();
    }

    public void debugResume() {
        if (engine == null || !inDebugSession) {
            throw new IllegalStateException("Debug session not active");
        }
        engine.debugResume();
        // Finalize after resume completes
        if (engine.isDebugFinished()) {
            engine.finalizeDebugExecution();
        }
    }

    public void stopDebugSession() {
        if (engine != null && inDebugSession) {
            engine.stopDebugSession();
            inDebugSession = false;
        }
    }

    public int getCurrentDebugPC() {
        if (engine == null || !inDebugSession) {
            return -1;
        }
        return engine.getCurrentDebugPC();
    }

    public boolean isInDebugSession() {
        return inDebugSession && engine != null && engine.isInDebugMode();
    }

    public boolean isDebugFinished() {
        return engine != null && engine.isDebugFinished();
    }

    public int getCurrentDebugCycles() {
        if (engine == null || !inDebugSession) {
            throw new IllegalStateException("Debug session not active");
        }
        return engine.getCurrentDebugCycles();
    }

    // NEW DEBUG FUNCTIONALITY - Added at the end as requested

    public int getLastDebugCycles() {
        if (engine == null) {
            throw new IllegalStateException("Program has not been set");
        }
        return engine.getLastDebugCycles();
    }

    /**
     * Retrieves the final variable states for a given execution configuration.
     * This method temporarily executes the program to get the final variable states,
     * then restores the original state. Used by the Show functionality to display
     * variable values from past executions.
     *
     * @param expandLevel The expansion level of the execution
     * @param arguments   The arguments used in the execution
     * @return Map of variable names to their final values after execution
     * @throws IllegalStateException    if no program is loaded
     * @throws IllegalArgumentException if expand level is invalid
     */
    public @NotNull Map<String, Integer> getFinalVariableStates(int expandLevel,
                                                                @NotNull Map<String, Integer> arguments) {
        if (engine == null) {
            throw new IllegalStateException("Program has not been set");
        }
        if (expandLevel < 0 || expandLevel > maxExpandLevel) {
            throw new IllegalArgumentException("Expand level must be between 0 and " + maxExpandLevel);
        }

        try {
            // Store current engine state to restore later
            boolean wasInDebugSession = inDebugSession;

            // Temporarily execute the program to get final states
            // Note: This creates a temporary execution that doesn't interfere with current state
            engine.run(expandLevel, arguments, false);

            // Get both work variables and arguments from the final state
            Map<String, Integer> finalStates = new java.util.HashMap<>();

            // Add work variables (z1, z2, etc.)
            finalStates.putAll(engine.getSortedWorkVars(expandLevel));

            // Add arguments (x1, x2, etc.) - these might have changed during execution
            finalStates.putAll(engine.getSortedArguments(expandLevel));

            // Add output variable (y)
            finalStates.put(ProgramUtils.OUTPUT_NAME, engine.getOutput(expandLevel));

            // Restore original debug session state
            inDebugSession = wasInDebugSession;

            System.out.println("Retrieved " + finalStates.size() + " final variable states for expand level " +
                    expandLevel + " with arguments: " + arguments);

            return finalStates;

        } catch (Exception e) {
            System.err.println("Error retrieving final variable states: " + e.getMessage());
            // Return empty map as fallback
            return new java.util.HashMap<>();
        }
    }
}