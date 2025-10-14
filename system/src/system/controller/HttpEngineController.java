package system.controller;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dto.engine.ExecutionStatisticsDTO;
import dto.engine.ProgramDTO;
import engine.core.ProgramEngine;
import jakarta.xml.bind.JAXBException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import system.http.utils.Endpoints;
import system.http.utils.Requests;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HttpEngineController implements EngineController {

    ProgramEngine engine;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void LoadProgramFromFile(@NotNull Path xmlFilePath) throws JAXBException, IOException {
        File xmlFile = xmlFilePath.toFile();
        Requests.postRunAsync(Endpoints.UPLOAD_PROGRAM, xmlFile, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.out.println("Failed to upload program: " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    System.out.println("Program uploaded successfully");
                } else {
                    assert response.body() != null;
                    System.out.println("Failed to upload program: " + response.body().string());
                }
            }
        });
    }

    @Override
    public ProgramDTO getBasicProgram() throws IOException {
        return null;
    }

    @Override
    public int getMaxExpandLevel() {
        return 0;
    }

    @Override
    public void runLoadedProgram(int expandLevel, @NotNull Map<String, Integer> arguments) {

    }

    @Override
    public int getLastExecutionNumberOfCycles() {
        return 0;
    }

    @Override
    public @NotNull ProgramDTO getProgramByExpandLevel(int expandLevel) {
        return null;
    }

    @Override
    public ExecutionStatisticsDTO getLastExecutionStatistics() {
        return null;
    }

    @Override
    public @NotNull Set<String> getAllVariablesAndLabelsNames(int expandLevel, boolean includeLabels) {
        return Set.of();
    }

    @Override
    public @NotNull Map<String, Integer> getSortedArguments(int expandLevel) {
        return Map.of();
    }

    @Override
    public @NotNull Map<String, Integer> getSortedArguments() {
        return Map.of();
    }

    @Override
    public @NotNull Integer getProgramResult(int expandLevel) {
        return 0;
    }

    @Override
    public @NotNull Map<String, Integer> getWorkVars(int expandLevel) {
        return Map.of();
    }

    @Override
    public List<ExecutionStatisticsDTO> getAllExecutionStatistics() {
        return List.of();
    }

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
    public @NotNull Map<String, Integer> getFinalVariableStates(int expandLevel, @NotNull Map<String, Integer> arguments) {
        return Map.of();
    }

    @Override
    public int getCurrentDebugCycles() {
        return 0;
    }

    @Override
    public void clearLoadedProgram() {

    }
}
