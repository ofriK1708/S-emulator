package system.controller;

import dto.engine.ExecutionStatisticsDTO;
import dto.engine.ProgramDTO;
import engine.exception.FunctionNotFound;
import engine.exception.LabelNotExist;
import jakarta.xml.bind.JAXBException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface EngineController {
    void LoadProgramFromFile(@NotNull Path xmlFilePath) throws LabelNotExist, JAXBException, IOException,
            FunctionNotFound;

    ProgramDTO getBasicProgram() throws IOException;

    int getMaxExpandLevel();

    void runLoadedProgram(int expandLevel, @NotNull Map<String, Integer> arguments);

    int getLastExecutionNumberOfCycles();

    @NotNull ProgramDTO getProgramByExpandLevel(int expandLevel);

    ExecutionStatisticsDTO getLastExecutionStatistics();

    @NotNull Set<String> getAllVariablesAndLabelsNames(int expandLevel, boolean includeLabels);

    @NotNull Map<String, Integer> getSortedArguments(int expandLevel);

    @NotNull Map<String, Integer> getSortedArguments();

    // @NotNull Map<String, String> getFunctionsSet(); not sure if needed
    @NotNull Integer getProgramResult(int expandLevel);

    @NotNull Map<String, Integer> getWorkVars(int expandLevel);

    List<ExecutionStatisticsDTO> getAllExecutionStatistics();

    void startDebugSession(int expandLevel, @NotNull Map<String, Integer> arguments);

    void debugStep();

    void debugStepBackward();

    void debugResume();

    void stopDebugSession();

    int getCurrentDebugPC();

    boolean isDebugFinished();

    @NotNull Map<String, Integer> getFinalVariableStates(int expandLevel, @NotNull Map<String, Integer> arguments);

    int getCurrentDebugCycles();

    void clearLoadedProgram();
}
