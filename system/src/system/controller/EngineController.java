package system.controller;

import dto.engine.FunctionMetadata;
import dto.engine.ProgramDTO;
import dto.engine.ProgramMetadata;
import dto.server.SystemResponse;
import dto.server.UserDTO;
import engine.utils.ArchitectureType;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface EngineController {

    ProgramDTO loadProgram(String programName);

    void loadProgramFromFile(@NotNull Path xmlFilePath) throws IOException;

    List<ProgramMetadata> getProgramsMetadata() throws IOException;

    List<FunctionMetadata> getFunctionsMetadata() throws IOException;

    void getBasicProgramAsync(@NotNull Consumer<SystemResponse> onResponse);

    ProgramDTO getBasicProgram();

    void getProgramByExpandLevelAsync(int expandLevel, @NotNull Consumer<SystemResponse> onResponse);

    ProgramDTO getProgramByExpandLevel(int expandLevel);

    void runLoadedProgram(int expandLevel, @NotNull Map<String, Integer> arguments, ArchitectureType architectureType,
                          @NotNull Consumer<SystemResponse> onResponse);

    void startDebugSession(int expandLevel, @NotNull Map<String, Integer> arguments, ArchitectureType architectureType,
                           @NotNull Consumer<SystemResponse> onResponse);

    void debugStepOver(@NotNull Consumer<SystemResponse> onResponse);

    void debugStepBack(@NotNull Consumer<SystemResponse> onResponse);

    void debugResume(@NotNull Consumer<SystemResponse> onResponse);

    void debugStop(@NotNull Consumer<SystemResponse> onResponse);

    void FetchUserExecutionHistoryAsync(@NotNull String username, @NotNull Consumer<SystemResponse> onResponse);

    List<UserDTO> getAllUsersDTO() throws IOException;

    void registerUserAsync(@NotNull String username, @NotNull Consumer<SystemResponse> onResponse);

    void setUserCreditsAsync(@NotNull String username, int credits, @NotNull Consumer<SystemResponse> onResponse);
}
