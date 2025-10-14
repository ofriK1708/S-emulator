package system.http.utils;

import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

//import static utils.ServletConstants.*;

public class Requests {
    private final static OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder().build();
    private static final String INFO_PARAM = "1nfo";
    private static final String PROGRAM_NAME_PARAM = "programName";
    private static final String EXPAND_LEVEL_PARAM = "expandLevel";

    public static void getRunAsync(String serverEndpoint, Callback callback) {
        Request request = new Request.Builder()
                .url(serverEndpoint)
                .build();

        Call call = HTTP_CLIENT.newCall(request);

        call.enqueue(callback);
    }

    public static void uploadFileAsync(String serverEndpoint, String jsonBody, Callback callback) {
        Request request = new Request.Builder()
                .url(serverEndpoint)
                .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                .build();

        Call call = HTTP_CLIENT.newCall(request);

        call.enqueue(callback);
    }

    public static void uploadFileAsync(String serverEndpoint, File xmlFile, Callback callback) {
        RequestBody fileBody = RequestBody.create(xmlFile, MediaType.parse("application/xml"));
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("xmlFile", xmlFile.getName(), fileBody)
                .build();

        Request request = new Request.Builder()
                .url(serverEndpoint)
                .post(requestBody)
                .build();

        Call call = HTTP_CLIENT.newCall(request);

        call.enqueue(callback);
    }

    public static Response getProgramInfoSync(String serverEndpoint, String info, String programName, int expandLevel)
            throws IOException {
        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(serverEndpoint)).newBuilder()
                .addQueryParameter(INFO_PARAM, info)
                .addQueryParameter(PROGRAM_NAME_PARAM, programName)
                .addQueryParameter(EXPAND_LEVEL_PARAM, String.valueOf(expandLevel))
                .build();
        System.out.println("about to send request to: " + url);
        Request request = new Request.Builder()
                .url(serverEndpoint)
                .build();

        Call call = HTTP_CLIENT.newCall(request);

        return call.execute();
    }

    /* Overloaded method without expandLevel parameter */
    public static Response getProgramInfoSync(String serverEndpoint, String info, String programName) throws IOException {
        return getProgramInfoSync(serverEndpoint, info, programName, -1);
    }

    public static Response postRunProgramSync(String serverEndpoint, String currentLoadedProgramName, int expandLevel,
                                              String jsonArguments) throws IOException {
        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(serverEndpoint)).newBuilder()
                .addQueryParameter(PROGRAM_NAME_PARAM, currentLoadedProgramName)
                .addQueryParameter(EXPAND_LEVEL_PARAM, String.valueOf(expandLevel))
                .build();

        System.out.println("about to send request to: " + url);

        RequestBody body = RequestBody.create(jsonArguments, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        Call call = HTTP_CLIENT.newCall(request);

        return call.execute();
    }

    public static Response postDebugProgramSync(String serverEndpoint, String currentLoadedProgramName, int expandLevel,
                                                String jsonArguments) throws IOException {
        return postRunProgramSync(serverEndpoint, currentLoadedProgramName, expandLevel, jsonArguments);
    }
}
