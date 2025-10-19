package system.controller;

import dto.engine.ProgramDTO;
import engine.exception.FunctionNotFound;
import engine.exception.LabelNotExist;
import jakarta.xml.bind.JAXBException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

public interface EngineController {
    void LoadProgramFromFile(@NotNull Path xmlFilePath) throws LabelNotExist, JAXBException, IOException,
            FunctionNotFound;

    void loadProgram(String programName);

    ProgramDTO getBasicProgram() throws IOException;

    int getMaxExpandLevel();

    void runLoadedProgram(int expandLevel, @NotNull Map<String, Integer> arguments);

    @NotNull ProgramDTO getProgramByExpandLevel(int expandLevel);

    @NotNull Set<String> getAllVariablesAndLabelsNames(int expandLevel, boolean includeLabels);

    @NotNull Map<String, Integer> getSortedArguments(int expandLevel);

    @NotNull Map<String, Integer> getSortedArguments();

    // @NotNull Map<String, String> getFunctionsSet(); not sure if needed
    @NotNull Integer getProgramResult(int expandLevel);

    @NotNull Map<String, Integer> getWorkVars(int expandLevel);

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
