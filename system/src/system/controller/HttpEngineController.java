package system.controller;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import engine.generated_2.SProgram;
import jakarta.xml.bind.JAXBException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import system.http.utils.EndPoints;
import system.http.utils.Requests;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class HttpEngineController extends EngineController {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void LoadProgramFromFile(@NotNull Path xmlFilePath) throws JAXBException, IOException {
        SProgram program = getSProgramFromFile(xmlFilePath);
        File xmlFile = xmlFilePath.toFile();
        Requests.postRunAsync(EndPoints.UPLOAD_PROGRAM, xmlFile, new Callback() {
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
}
