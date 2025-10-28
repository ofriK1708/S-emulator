package system.http.utils;

import com.google.gson.Gson;
import dto.engine.DebugStateChangeResultDTO;
import dto.server.SystemResponse;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.function.Consumer;

import static system.controller.HttpEngineController.getAndValidateBodyString;
import static system.controller.HttpEngineController.getAndValidateBodySystemResponse;

public class DebugActionCallback implements Callback {
    private final @NotNull Consumer<SystemResponse> onResponse;
    private final Gson gson = new Gson();

    public DebugActionCallback(@NotNull Consumer<SystemResponse> onResponse) {
        this.onResponse = onResponse;
    }

    @Override
    public void onFailure(@NotNull Call call, @NotNull IOException e) {
        SystemResponse systemResponse = SystemResponse.builder()
                .isSuccess(false)
                .message("Failed to step back in debug session: " + e.getMessage())
                .build();
        onResponse.accept(systemResponse);
    }

    @Override
    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
        try (ResponseBody responseBody = response.body()) {
            if (response.isSuccessful()) {
                String jsonBody = getAndValidateBodyString(responseBody);

                DebugStateChangeResultDTO debugStateChangeResultDTO = gson.fromJson(jsonBody,
                        DebugStateChangeResultDTO.class);

                SystemResponse systemResponse = SystemResponse.builder()
                        .isSuccess(true)
                        .debugStateChangeResultDTO(debugStateChangeResultDTO)
                        .build();

                onResponse.accept(systemResponse);
            } else {
                SystemResponse systemResponse = getAndValidateBodySystemResponse(responseBody);
                onResponse.accept(systemResponse);
            }
        }
    }

}
