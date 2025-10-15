package system.controller;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dto.engine.ExecutionStatisticsDTO;
import dto.engine.FunctionMetadata;
import dto.engine.ProgramDTO;
import dto.engine.ProgramMetadata;
import dto.system.LoadProgramResultDTO;
import jakarta.xml.bind.JAXBException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import system.http.utils.Endpoints;
import system.http.utils.Requests;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static utils.ServletConstants.*;

public class HttpEngineController implements EngineController {

    /* TODO - in order to reduce the number of requests, we can store the info the first time we get it
     *   we need to implement a class that will hold for every program that data on it, and when we clear or change
     *   program we make sure to clear any dynamic data*/

    private final static Type ALL_VARS_NAMES_SET_TYPE = new TypeToken<Set<String>>() {
    }.getType();
    private final static Type VARS_NAME_AND_VALUE_TYPE = new TypeToken<Map<String, Integer>>() {
    }.getType();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final static Type STATS_LIST_TYPE = new TypeToken<List<ExecutionStatisticsDTO>>() {
    }.getType();
    private final Set<ProgramMetadata> programsMetadata = new LinkedHashSet<>();
    private final Set<FunctionMetadata> functionsMetadata = new LinkedHashSet<>();
    String currentLoadedProgramName = null;
    int loadedProgramMaxExpandLevel = -1;

    private void validateProgramLoaded() {
        if (currentLoadedProgramName == null) {
            throw new IllegalStateException("No program loaded");
        }
    }

    private void validateResponseBodyNotNull(Response response) throws IOException {
        if (response.body() == null) {
            throw new IOException("Response body is null");
        }
    }

    public Set<ProgramMetadata> getProgramsMetadata() {
        return programsMetadata;
    }

    public Set<FunctionMetadata> getFunctionsMetadata() {
        return functionsMetadata;
    }

    @Override
    public void LoadProgramFromFile(@NotNull Path xmlFilePath) throws JAXBException, IOException {
        File xmlFile = xmlFilePath.toFile();
        Requests.uploadFileAsync(Endpoints.UPLOAD_PROGRAM, xmlFile, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.out.println("Failed to upload program: " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    Gson json = new Gson();
                    validateResponseBodyNotNull(response);
                    //noinspection DataFlowIssue
                    LoadProgramResultDTO resultDTO = json.fromJson(response.body().string(),
                            LoadProgramResultDTO.class);
                    System.out.println("Program uploaded successfully");
                    System.out.println("resultDTO = " + resultDTO);
                    programsMetadata.addAll(resultDTO.programs());
                    functionsMetadata.addAll(resultDTO.functions());
                } else {
                    System.out.println("Failed to upload program: " + response.body());
                }
                try {
                    response.close();
                } catch (Exception e) {
                    System.out.println("Failed to close response: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void loadProgram(String programName) {
        this.currentLoadedProgramName = programName;
        try {
            getBasicProgram();
            this.loadedProgramMaxExpandLevel = getMaxExpandLevel();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ProgramDTO getBasicProgram() throws IOException {
        validateProgramLoaded();
        try (Response response = Requests.getProgramInfoSync(Endpoints.GET_PROGRAM_INFO,
                BASIC_PROGRAM, currentLoadedProgramName)) {
            if (response.isSuccessful()) {
                validateResponseBodyNotNull(response);
                //noinspection DataFlowIssue
                return gson.fromJson(response.body().string(), ProgramDTO.class);
            } else {
                throw new IOException("Failed to get basic program: " + response.body());
            }
        }
    }

    @Override
    public int getMaxExpandLevel() {
        validateProgramLoaded();
        try (Response response = Requests.getProgramInfoSync(
                Endpoints.GET_PROGRAM_INFO, MAX_EXPAND_LEVEL, currentLoadedProgramName)) {
            if (response.isSuccessful()) {
                validateResponseBodyNotNull(response);
                //noinspection DataFlowIssue
                return Integer.parseInt(response.body().string());
            } else {
                throw new IOException("Failed to get max expand level: " + response.body());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void runLoadedProgram(int expandLevel, @NotNull Map<String, Integer> arguments) {
        validateProgramLoaded();
        String jsonBody = gson.toJson(arguments);
        try (Response response = Requests.postRunProgramSync(Endpoints.RUN_PROGRAM, currentLoadedProgramName,
                expandLevel, jsonBody)) {
            if (response.isSuccessful()) {
                System.out.println("Program ran successfully");
            } else {
                throw new IOException("Failed to run program: " + response.body());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public int getLastExecutionNumberOfCycles() {
        validateProgramLoaded();
        try (Response response = Requests.getProgramInfoSync(Endpoints.GET_PROGRAM_INFO,
                LAST_EXECUTION_CYCLES, currentLoadedProgramName)) {
            if (response.isSuccessful()) {
                validateResponseBodyNotNull(response);
                //noinspection DataFlowIssue
                return Integer.parseInt(response.body().string());
            } else {
                throw new IOException("Failed to get last execution number of cycles: " + response.body());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull ProgramDTO getProgramByExpandLevel(int expandLevel) {
        validateProgramLoaded();
        try (Response response = Requests.getProgramInfoSync(Endpoints.GET_PROGRAM_INFO,
                PROGRAM_BY_EXPAND_LEVEL, currentLoadedProgramName, expandLevel)) {
            if (response.isSuccessful()) {
                validateResponseBodyNotNull(response);
                //noinspection DataFlowIssue
                return gson.fromJson(response.body().string(), ProgramDTO.class);
            } else {
                throw new IOException("Failed to get program by expand level: " + response.body());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ExecutionStatisticsDTO getLastExecutionStatistics() {
        validateProgramLoaded();
        try (Response response = Requests.getProgramInfoSync(Endpoints.GET_PROGRAM_INFO,
                LAST_EXECUTION_STATISTICS, currentLoadedProgramName)) {
            if (response.isSuccessful()) {
                validateResponseBodyNotNull(response);
                //noinspection DataFlowIssue
                return gson.fromJson(response.body().string(), ExecutionStatisticsDTO.class);
            } else {
                throw new IOException("Failed to get last execution statistics: " + response.body());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull Set<String> getAllVariablesAndLabelsNames(int expandLevel, boolean includeLabels) {
        validateProgramLoaded();
        try (Response response = Requests.getProgramInfoSync(Endpoints.GET_PROGRAM_INFO,
                ALL_VARIABLES_AND_LABELS, currentLoadedProgramName, expandLevel)) {
            if (response.isSuccessful()) {
                validateResponseBodyNotNull(response);
                //noinspection DataFlowIssue
                return gson.fromJson(response.body().string(), ALL_VARS_NAMES_SET_TYPE);
            } else {
                throw new IOException("Failed to get all variables and labels names: " + response.body());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull Map<String, Integer> getSortedArguments(int expandLevel) {
        validateProgramLoaded();
        try (Response response = Requests.getProgramInfoSync(Endpoints.GET_PROGRAM_INFO,
                ARGUMENTS, currentLoadedProgramName, expandLevel)) {
            if (response.isSuccessful()) {
                validateResponseBodyNotNull(response);
                //noinspection DataFlowIssue
                return gson.fromJson(response.body().string(), VARS_NAME_AND_VALUE_TYPE);
            } else {
                throw new IOException("Failed to get sorted arguments: " + response.body());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull Map<String, Integer> getSortedArguments() {
        validateProgramLoaded();
        return getSortedArguments(0);
    }

    @Override
    public @NotNull Integer getProgramResult(int expandLevel) {
        validateProgramLoaded();
        try (Response response = Requests.getProgramInfoSync(Endpoints.GET_PROGRAM_INFO,
                PROGRAM_RESULT, currentLoadedProgramName, expandLevel)) {
            if (response.isSuccessful()) {
                validateResponseBodyNotNull(response);
                //noinspection DataFlowIssue
                return Integer.parseInt(response.body().string());
            } else {
                throw new IOException("Failed to get program output: " + response.body());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull Map<String, Integer> getWorkVars(int expandLevel) {
        validateProgramLoaded();
        try (Response response = Requests.getProgramInfoSync(Endpoints.GET_PROGRAM_INFO,
                WORK_VARS, currentLoadedProgramName, expandLevel)) {
            if (response.isSuccessful()) {
                validateResponseBodyNotNull(response);
                //noinspection DataFlowIssue
                return gson.fromJson(response.body().string(), VARS_NAME_AND_VALUE_TYPE);
            } else {
                throw new IOException("Failed to get work vars: " + response.body());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ExecutionStatisticsDTO> getAllExecutionStatistics() {
        validateProgramLoaded();
        try (Response response = Requests.getProgramInfoSync(Endpoints.GET_PROGRAM_INFO,
                ALL_EXECUTION_STATISTICS, currentLoadedProgramName)) {
            if (response.isSuccessful()) {
                validateResponseBodyNotNull(response);
                //noinspection DataFlowIssue
                return gson.fromJson(response.body().string(), STATS_LIST_TYPE);
            } else {
                throw new IOException("Failed to get all execution statistics: " + response.body());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    // TODO - implement the debug methods, first try to refactor the program engine to make it modular

    @Override
    public void startDebugSession(int expandLevel, @NotNull Map<String, Integer> arguments) {

    }

    @Override
    public void debugStep() {

    }

    @Override
    public void debugStepBackward() {

    }

    @Override
    public void debugResume() {

    }

    @Override
    public void stopDebugSession() {

    }

    @Override
    public int getCurrentDebugPC() {
        return 0;
    }

    @Override
    public boolean isDebugFinished() {
        return false;
    }

    @Override
    public int getCurrentDebugCycles() {
        return 0;
    }

    @Override
    public @NotNull Map<String, Integer> getFinalVariableStates(int expandLevel,
                                                                @NotNull Map<String, Integer> arguments) {
        // TODO - here we can check for that data we stored first, and if that data is missing we can make the request
        return Map.of();
    }

    @Override
    public void clearLoadedProgram() {

    }
}
