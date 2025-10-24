package system.controller;

import dto.engine.FunctionMetadata;
import dto.engine.ProgramDTO;
import dto.engine.ProgramMetadata;
import dto.server.SystemResponse;
import dto.server.UserDTO;
import engine.exception.FunctionNotFound;
import engine.exception.LabelNotExist;
import jakarta.xml.bind.JAXBException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface EngineController {
    void LoadProgramFromFileAsync(@NotNull Path xmlFilePath, Consumer<SystemResponse> onResponse) throws LabelNotExist, JAXBException, IOException,
            FunctionNotFound;

    void loadProgramAsync(String programName, @NotNull Consumer<SystemResponse> onResponse) throws IOException;

    ProgramDTO loadProgram(String programName) throws IOException;

    void loadProgramFromFile(@NotNull Path xmlFilePath) throws JAXBException, IOException;

    List<ProgramMetadata> getProgramsMetadata() throws IOException;

    List<FunctionMetadata> getFunctionsMetadata() throws IOException;

    void clearLoadedProgram();

    void getBasicProgramAsync(@NotNull Consumer<SystemResponse> onResponse) throws IOException;

    ProgramDTO getBasicProgram();

    void getProgramByExpandLevelAsync(int expandLevel, @NotNull Consumer<SystemResponse> onResponse) throws IOException;

    ProgramDTO getProgramByExpandLevel(int expandLevel);

    void runLoadedProgram(int expandLevel, @NotNull Map<String, Integer> arguments,
                          @NotNull Consumer<SystemResponse> onResponse) throws IOException;

    void startDebugSession(int expandLevel, @NotNull Map<String, Integer> arguments,
                           @NotNull Consumer<SystemResponse> onResponse) throws IOException;

    void debugStepOver(@NotNull Consumer<SystemResponse> onResponse) throws IOException;

    void debugStepBack(@NotNull Consumer<SystemResponse> onResponse) throws IOException;

    void debugResume(@NotNull Consumer<SystemResponse> onResponse) throws IOException;

    void debugStop(@NotNull Consumer<SystemResponse> onResponse) throws IOException;

    void getUserStatistics(@NotNull String username, @NotNull Consumer<SystemResponse> onResponse) throws IOException;

    List<UserDTO> getAllUsersDTO() throws IOException;

    void registerUser(@NotNull String username, @NotNull Consumer<SystemResponse> onResponse) throws IOException;
}
