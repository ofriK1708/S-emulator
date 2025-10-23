package system.controller;

import dto.engine.FunctionMetadata;
import dto.engine.ProgramMetadata;
import dto.server.SystemResponse;
import dto.server.UserDTO;
import engine.exception.FunctionNotFound;
import engine.exception.LabelNotExist;
import jakarta.xml.bind.JAXBException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public interface EngineController {
    void LoadProgramFromFile(@NotNull Path xmlFilePath) throws LabelNotExist, JAXBException, IOException,
            FunctionNotFound;

    void loadProgram(String programName, @NotNull Consumer<SystemResponse> onResponse) throws IOException;

    Set<ProgramMetadata> getProgramsMetadata() throws IOException;

    Set<FunctionMetadata> getFunctionsMetadata() throws IOException;

    void clearLoadedProgram();

    void getBasicProgram(@NotNull Consumer<SystemResponse> onResponse) throws IOException;

    void getProgramByExpandLevel(int expandLevel, @NotNull Consumer<SystemResponse> onResponse) throws IOException;

    void runLoadedProgram(int expandLevel, @NotNull Map<String, Integer> arguments,
                          @NotNull Consumer<SystemResponse> onResponse) throws IOException;

    void startDebugSession(int expandLevel, @NotNull Map<String, Integer> arguments,
                           @NotNull Consumer<SystemResponse> onResponse) throws IOException;

    void debugStepOver(@NotNull Consumer<SystemResponse> onResponse) throws IOException;

    void debugStepBack(@NotNull Consumer<SystemResponse> onResponse) throws IOException;

    void debugResume(@NotNull Consumer<SystemResponse> onResponse) throws IOException;

    void debugStop(@NotNull Consumer<SystemResponse> onResponse) throws IOException;

    void getUserStatistics(@NotNull String username, @NotNull Consumer<SystemResponse> onResponse) throws IOException;

    Set<UserDTO> getAllUsersDTO() throws IOException;

    void registerUser(@NotNull String username, @NotNull Consumer<SystemResponse> onResponse) throws IOException;
}
