package system.http.utils;

import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

//import static utils.ServletConstants.*;

public class Requests {
    private final static OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder().build();
    private static final String INFO_PARAM = "1nfo" ;
    private static final String PROGRAM_NAME_PARAM = "programName";
    private static final String EXPAND_LEVEL_PARAM = "expandLevel";

    public static void getRunAsync(String finalUrl, Callback callback) {
        Request request = new Request.Builder()
                .url(finalUrl)
                .build();

        Call call = HTTP_CLIENT.newCall(request);

        call.enqueue(callback);
    }

    public static void postRunAsync(String finalUrl, String jsonBody, Callback callback) {
        Request request = new Request.Builder()
                .url(finalUrl)
                .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                .build();

        Call call = HTTP_CLIENT.newCall(request);

        call.enqueue(callback);
    }

    public static void postRunAsync(String finalUrl, File xmlFile, Callback callback) {
        RequestBody fileBody = RequestBody.create(xmlFile, MediaType.parse("application/xml"));
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("xmlFile", xmlFile.getName(), fileBody)
                .build();

        Request request = new Request.Builder()
                .url(finalUrl)
                .post(requestBody)
                .build();

        Call call = HTTP_CLIENT.newCall(request);

        call.enqueue(callback);
    }

    public static Response getProgramInfoSync(String finalUrl, String info, String programName, int expandLevel)
            throws IOException {
        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(finalUrl)).newBuilder()
                .addQueryParameter(INFO_PARAM, info)
                .addQueryParameter(PROGRAM_NAME_PARAM, programName)
                .addQueryParameter(EXPAND_LEVEL_PARAM, String.valueOf(expandLevel))
                .build();
        System.out.println("about to send request to: " + url);
        Request request = new Request.Builder()
                .url(finalUrl)
                .build();

        Call call = HTTP_CLIENT.newCall(request);

        return call.execute();
    }

    public static Response getProgramInfoSync(String finalUrl, String info, String programName) throws IOException {
        return getProgramInfoSync(finalUrl, info, programName, -1);
    }
}
