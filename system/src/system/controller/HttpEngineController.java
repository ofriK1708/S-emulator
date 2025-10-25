package system.controller;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dto.engine.*;
import dto.server.SystemResponse;
import dto.server.UserDTO;
import engine.utils.DebugAction;
import jakarta.xml.bind.JAXBException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import system.http.utils.DebugActionCallback;
import system.http.utils.Endpoints;
import system.http.utils.Requests;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static utils.ServletConstants.*;

/**
 * An implementation of the EngineController interface that communicates with the engine
 * via HTTP requests.
 *
 * <p>Handles loading programs, retrieving metadata, running programs, and managing debug sessions.</p>
 *
 * <p>Uses the {@link SystemResponse}  to build a response and send it back using the consumer given</p>
 */
public class HttpEngineController implements EngineController {

    private final @NotNull Gson gson = new GsonBuilder().create();
    // region private fields and helpers
    @Nullable String loadedProgramName = null;

    /**
     * Validates and retrieves the body string from the response body.
     *
     * @param responseBody The response body to validate and retrieve the string from.
     * @return The body string.
     * @throws IOException If the response body is null or an I/O error occurs.
     */
    public static @NotNull String getAndValidateBodyString(@Nullable ResponseBody responseBody) throws IOException {
        if (responseBody == null) {
            throw new IOException("Response body is null");
        }
        return responseBody.string();
    }

    /**
     * Validates and retrieves the name of the loaded program.
     *
     * @return The name of the loaded program.
     * @throws IllegalStateException If no program is loaded.
     */
    private @NotNull String getAndValidateProgramLoaded() {
        if (loadedProgramName == null) {
            throw new IllegalStateException("No program loaded");
        }
        return loadedProgramName;
    }

    /**
     * Validates that a program is loaded.
     *
     * @throws IllegalStateException If no program is loaded.
     */
    private void validateProgramLoaded() {
        if (loadedProgramName == null) {
            throw new IllegalStateException("No program loaded");
        }
    }
    // endregion

    // region EngineController implementation

    /**
     * Loads a program from an XML file to the server <strong>asynchronously</strong>.
     *
     * @param xmlFilePath The path to the XML file containing the program.
     * @param onResponse
     */
    @Override
    public void LoadProgramFromFileAsync(@NotNull Path xmlFilePath, Consumer<SystemResponse> onResponse)
            throws JAXBException, IOException {

        File xmlFile = xmlFilePath.toFile();
        Requests.uploadFileAsync(Endpoints.UPLOAD_PROGRAM, xmlFile, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                SystemResponse systemResponse = SystemResponse.builder()
                        .isSuccess(false)
                        .message("Failed to upload program: " + e.getMessage())
                        .build();
                onResponse.accept(systemResponse);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (response.isSuccessful()) {
                        String successMessage = getAndValidateBodyString(responseBody);
                        SystemResponse systemResponse = SystemResponse.builder()
                                .isSuccess(true)
                                .message(successMessage)
                                .build();
                        onResponse.accept(systemResponse);

                    } else {
                        System.out.println("Failed to upload program: " + response.body());
                    }
                }
            }
        });
    }

    /**
     * Loads a program from an XML file to the server.
     * this happens <strong>synchronously</strong>.
     * <p>
     * call this with 'pulling' threads or async tasks. <strong>NOT THE JAT!</strong>
     * </p>
     *
     * @param xmlFilePath The path to the XML file containing the program.
     * @throws JAXBException If an error occurs while processing the XML.
     * @throws IOException   If an I/O error occurs.
     */
    @Override
    public void loadProgramFromFile(@NotNull Path xmlFilePath) throws JAXBException, IOException {
        File xmlFile = xmlFilePath.toFile();
        try (Response response = Requests.uploadFile(Endpoints.UPLOAD_PROGRAM, xmlFile)) {
            if (response.isSuccessful()) {
                String successMessage = getAndValidateBodyString(response.body());
                System.out.println("Program uploaded successfully: " + successMessage);
            } else {
                throw new IOException("Failed to upload program: " + response.body());
            }
        }
    }

    /**
     * Gets the programs metadata from the server.
     * this happens <strong>synchronously</strong>.
     * <p>
     * call this with 'pulling' threads or async tasks. <strong>NOT THE JAT!</strong>
     * </p>
     *
     * @return A set of ProgramMetadata objects representing the programs metadata.
     */
    @Override
    public List<ProgramMetadata> getProgramsMetadata() throws IOException {
        try (Response response = Requests
                .getSystemInfo(Endpoints.GET_SYSTEM_INFO, PROGRAMS_METADATA_INFO)) {

            if (response.isSuccessful()) {
                String jsonString = getAndValidateBodyString(response.body());
                Set<ProgramMetadata> programsLinkedSet = gson.fromJson(jsonString, PROGRAMS_METADATA_SET_TYPE_TOKEN);
                return List.copyOf(programsLinkedSet);
            } else {
                throw new IOException("Failed to get programs metadata: " + response.body());
            }
        }
    }

    /**
     * Gets the functions metadata from the server.
     * this happens <strong>synchronously</strong>.
     * <p>
     * call this with 'pulling' threads or async tasks. <strong>NOT THE JAT!</strong>
     * </p>
     *
     * @return A set of FunctionMetadata objects representing the functions metadata.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public List<FunctionMetadata> getFunctionsMetadata() throws IOException {
        try (Response response = Requests
                .getSystemInfo(Endpoints.GET_SYSTEM_INFO, FUNCTIONS_METADATA_INFO)) {

            if (response.isSuccessful()) {
                String jsonString = getAndValidateBodyString(response.body());
                Set<FunctionMetadata> functionLinkedSet = gson.fromJson(jsonString, FUNCTIONS_METADATA_SET_TYPE_TOKEN);
                return List.copyOf(functionLinkedSet);
            } else {
                throw new IOException("Failed to get functions metadata: " + response.body());
            }
        }
    }

    /**
     * Gets the programs and functions metadata from the server.
     * this happens <strong>synchronously</strong>.
     * <p>
     * call this with 'pulling' threads or async tasks. <strong>NOT THE JAT!</strong>
     * </p>
     *
     * @return A ProgramsAndFunctionsMetadata object representing the programs and functions metadata.
     * @throws IOException If an I/O error occurs.
     */
    public ProgramsAndFunctionsMetadata getProgramsAndFunctionsMetadata() throws IOException {
        try (Response response = Requests
                .getSystemInfo(Endpoints.GET_SYSTEM_INFO, PROGRAMS_AND_FUNCTIONS_METADATA)) {

            if (response.isSuccessful()) {
                String jsonString = getAndValidateBodyString(response.body());
                return gson.fromJson(jsonString, ProgramsAndFunctionsMetadata.class);
            } else {
                throw new IOException("Failed to get programs and functions metadata: " + response.body());
            }
        }
    }

    /**
     * Gets all users DTO from the server.
     * this happens <strong>synchronously</strong>.
     * <p>
     * call this with 'pulling' threads or async tasks. <strong>NOT THE JAT!</strong>
     * </p>
     *
     * @return A List of UserDTO objects representing all users. (sorted already by registration order)
     */
    @Override
    public List<UserDTO> getAllUsersDTO() throws IOException {
        try (Response response = Requests
                .getSystemInfo(Endpoints.GET_ALL_USERS, ALL_USERS_INFO)) {

            if (response.isSuccessful()) {
                String jsonString = getAndValidateBodyString(response.body());
                Set<UserDTO> usersLinkedSet = gson.fromJson(jsonString, USER_DTO_SET_TYPE_TOKEN);
                return List.copyOf(usersLinkedSet);
            } else {
                throw new IOException("Failed to get all users: " + response.body());
            }
        }
    }

    /**
     * Gets the user statistics from the server <strong>asynchronously</strong>.
     *
     * @param username   The username for which to get the statistics.
     * @param onResponse A consumer that will be called with the SystemResponse when the operation is complete.
     */
    @Override
    public void FetchUserExecutionHistoryAsync(@NotNull String username, @NotNull Consumer<SystemResponse> onResponse) {
        Requests.getUserStatisticsAsync(Endpoints.GET_USER_EXECUTION_HISTORY, username, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                SystemResponse systemResponse = SystemResponse.builder()
                        .isSuccess(false)
                        .message("Failed to get user statistics: " + e.getMessage())
                        .build();
                onResponse.accept(systemResponse);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (response.isSuccessful()) {
                        String jsonString = getAndValidateBodyString(responseBody);

                        List<ExecutionResultStatisticsDTO> statisticsDTOList = gson.fromJson(jsonString,
                                EXECUTION_RESULT_STATISTICS_DTO_LIST_TYPE_TOKEN);

                        SystemResponse systemResponse = SystemResponse.builder()
                                .isSuccess(true)
                                .userStatisticsDTOList(statisticsDTOList)
                                .build();

                        onResponse.accept(systemResponse);
                    } else {
                        SystemResponse systemResponse = SystemResponse.builder()
                                .isSuccess(false)
                                .message("Failed to get user statistics: " + response.body())
                                .build();
                        onResponse.accept(systemResponse);
                    }
                }
            }
        });

    }

    /**
     * Registers a new user on the server <strong>synchronously</strong>.
     * we cant proceed without registering. so this must be sync.
     *
     * @param username   The username of the user to register.
     * @return A SystemResponse indicating the success or failure of the registration.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public SystemResponse registerUser(@NotNull String username) throws IOException {
        try (Response response = Requests
                .postRegisterUser(Endpoints.REGISTER_USER, username)) {

            if (response.isSuccessful()) {
                String successMessage = getAndValidateBodyString(response.body());

                return SystemResponse.builder()
                        .isSuccess(true)
                        .message(successMessage)
                        .build();
            } else {
                return SystemResponse.builder()
                        .isSuccess(false)
                        .message("Failed to register user: " + response.body())
                        .build();
            }
        }

    }

    /**
     * Loads a program by name from the server to be executed.
     *
     * @param programName The name of the program to load.
     * @param onResponse  A consumer that will be called with the SystemResponse when the operation is complete.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void loadProgramAsync(@NotNull String programName, @NotNull Consumer<SystemResponse> onResponse) {
        this.loadedProgramName = programName;
        getBasicProgramAsync(onResponse);
    }

    /**
     * Loads a program by name from the server to be executed.
     * this happens <strong>synchronously</strong>.
     * <p>
     * call this with 'pulling' threads or async tasks. <strong>NOT THE JAT!</strong>
     * </p>
     *
     * @param programName The name of the program to load.
     * @return A ProgramDTO object representing the loaded program.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public ProgramDTO loadProgram(String programName) throws IOException {
        this.loadedProgramName = programName;
        return getBasicProgram();
    }

    /**
     * Clears the currently loaded program.
     */
    @Override
    public void clearLoadedProgram() {
        this.loadedProgramName = null;
    }

    /**
     * Gets the basic program information from the server <strong>asynchronously</strong>.
     *
     * @param onResponse A consumer that will be called with the SystemResponse when the operation is complete.
     */
    @Override
    public void getBasicProgramAsync(@NotNull Consumer<SystemResponse> onResponse) {
        getProgramByExpandLevelAsync(0, onResponse);
    }

    /**
     * Gets the program information by expand level from the server <strong>asynchronously</strong>.
     *
     * @param expandLevel The expand level for the program information.
     * @param onResponse  A consumer that will be called with the SystemResponse when the operation is complete.
     */
    @Override
    public void getProgramByExpandLevelAsync(int expandLevel,
                                             @NotNull Consumer<SystemResponse> onResponse) {
        String programName = getAndValidateProgramLoaded();
        Requests.getProgramInfoAsync(Endpoints.GET_PROGRAM_INFO, PROGRAM_BY_EXPAND_LEVEL_INFO, programName, expandLevel,
                new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {

                        SystemResponse systemResponse = SystemResponse.builder()
                                .isSuccess(false)
                                .message("Failed to get basic program: " + e.getMessage())
                                .build();

                        onResponse.accept(systemResponse);
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        try (ResponseBody responseBody = response.body()) {
                            if (response.isSuccessful()) {
                                String jsonString = getAndValidateBodyString(responseBody);
                                ProgramDTO programDTO = gson.fromJson(jsonString, ProgramDTO.class);

                                SystemResponse systemResponse = SystemResponse.builder()
                                        .isSuccess(true)
                                        .programDTO(programDTO)
                                        .build();
                                onResponse.accept(systemResponse);
                            } else {
                                SystemResponse systemResponse = SystemResponse.builder()
                                        .isSuccess(false)
                                        .message("Failed to get basic program: " + response.body())
                                        .build();
                                onResponse.accept(systemResponse);
                            }
                        }
                    }
                });
    }

    /**
     * Gets the program information by expand level from the server.
     * this happens <strong>synchronously</strong>.
     * <p>
     * call this with 'pulling' threads or async tasks. <strong>NOT THE JAT!</strong>
     * </p>
     *
     * @param expandLevel The expand level for the program information.
     * @return A ProgramDTO object representing the program information.
     */
    @Override
    public ProgramDTO getProgramByExpandLevel(int expandLevel) {
        String programName = getAndValidateProgramLoaded();
        try (Response response = Requests
                .getProgramInfo(Endpoints.GET_PROGRAM_INFO, PROGRAM_BY_EXPAND_LEVEL_INFO, programName, expandLevel)) {

            if (response.isSuccessful()) {
                String jsonString = getAndValidateBodyString(response.body());
                return gson.fromJson(jsonString, ProgramDTO.class);
            } else {
                throw new IOException("Failed to get program by expand level: " + response.body());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the basic program information from the server.
     * this happens <strong>synchronously</strong>.
     * <p>
     * call this with 'pulling' threads or async tasks. <strong>NOT THE JAT!</strong>
     * </p>
     *
     * @return A ProgramDTO object representing the basic program information.
     */
    @Override
    public ProgramDTO getBasicProgram() {
        return getProgramByExpandLevel(0);
    }

    /**
     * Runs the loaded program with the given arguments and expand level <strong>asynchronously</strong>.
     *
     * @param expandLevel The expand level for the program execution.
     * @param arguments   A map of argument names to their integer values.
     * @param onResponse  A consumer that will be called with the SystemResponse when the operation is complete.
     */
    @Override
    public void runLoadedProgram(int expandLevel, @NotNull Map<String, Integer> arguments,
                                 @NotNull Consumer<SystemResponse> onResponse) {
        String programName = getAndValidateProgramLoaded();
        String jsonBody = gson.toJson(arguments);
        Requests.postRunProgramAsync(Endpoints.RUN_PROGRAM, programName, expandLevel, jsonBody,
                new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        SystemResponse systemResponse = SystemResponse.builder()
                                .isSuccess(false)
                                .message("Failed to run program: " + e.getMessage())
                                .build();
                        onResponse.accept(systemResponse);
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        try (ResponseBody responseBody = response.body()) {
                            if (response.isSuccessful()) {
                                String jsonString = getAndValidateBodyString(responseBody);

                                FullExecutionResultDTO executionResultDTO = gson.fromJson(jsonString,
                                        FullExecutionResultDTO.class);

                                SystemResponse systemResponse = SystemResponse.builder()
                                        .isSuccess(true)
                                        .fullExecutionResultDTO(executionResultDTO)
                                        .build();

                                onResponse.accept(systemResponse);
                            } else {
                                System.out.println("Failed to run program: " + response.body());
                            }
                            try {
                                response.close();
                            } catch (Exception e) {
                                System.out.println("Failed to close response: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }

    /**
     * Starts a debug session for the loaded program with the given arguments and expand
     * level <strong>asynchronously</strong>.
     *
     * @param expandLevel The expand level for the debug session.
     * @param arguments   A map of argument names to their integer values.
     * @param onResponse  A consumer that will be called with the SystemResponse when the operation is complete.
     */
    @Override
    public void startDebugSession(int expandLevel,
                                  @NotNull Map<String, Integer> arguments,
                                  @NotNull Consumer<SystemResponse> onResponse) {
        String programName = getAndValidateProgramLoaded();
        String jsonBody = gson.toJson(arguments);
        Requests.postStartDebugAsync(Endpoints.START_DEBUG_PROGRAM, programName, jsonBody, expandLevel,
                new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        SystemResponse systemResponse = SystemResponse.builder()
                                .isSuccess(false)
                                .message("Failed to start debug session: " + e.getMessage())
                                .build();
                        onResponse.accept(systemResponse);
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        try (ResponseBody responseBody = response.body()) {
                            if (response.isSuccessful()) {

                                String message = getAndValidateBodyString(responseBody);

                                SystemResponse systemResponse = SystemResponse.builder()
                                        .isSuccess(true)
                                        .message(message)
                                        .build();

                                onResponse.accept(systemResponse);
                            } else {
                                System.out.println("Failed to start debug session: " + response.body());
                            }
                        }
                    }
                });
    }

    /**
     * Performs a debug step over action <strong>asynchronously</strong>.
     *
     * @param onResponse A consumer that will be called with the SystemResponse when the operation is complete.
     */
    @Override
    public void debugStepOver(@NotNull Consumer<SystemResponse> onResponse) {
        validateProgramLoaded();
        Requests.postDebugActionAsync(Endpoints.DEBUG_ACTION, DebugAction.STEP_OVER.toString(),
                new DebugActionCallback(onResponse));

    }

    /**
     * Performs a debug step back action <strong>asynchronously</strong>.
     *
     * @param onResponse A consumer that will be called with the SystemResponse when the operation is complete.
     */
    @Override
    public void debugStepBack(@NotNull Consumer<SystemResponse> onResponse) {
        validateProgramLoaded();
        Requests.postDebugActionAsync(Endpoints.DEBUG_ACTION, DebugAction.STEP_BACK.toString(),
                new DebugActionCallback(onResponse));

    }

    /**
     * Performs a debug resume action <strong>asynchronously</strong>.
     *
     * @param onResponse A consumer that will be called with the SystemResponse when the operation is complete.
     */
    @Override
    public void debugResume(@NotNull Consumer<SystemResponse> onResponse) {
        validateProgramLoaded();
        Requests.postDebugActionAsync(Endpoints.DEBUG_ACTION, DebugAction.RESUME.toString(),
                new DebugActionCallback(onResponse));

    }

    /**
     * Performs a debug stop action <strong>asynchronously</strong>.
     *
     * @param onResponse A consumer that will be called with the SystemResponse when the operation is complete.
     */
    @Override
    public void debugStop(@NotNull Consumer<SystemResponse> onResponse) {
        validateProgramLoaded();
        Requests.postDebugActionAsync(Endpoints.DEBUG_ACTION, DebugAction.STOP.toString(),
                new DebugActionCallback(onResponse));

    }
}
