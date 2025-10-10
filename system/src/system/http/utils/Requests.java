package system.http.utils;

import okhttp3.*;

import java.io.File;

public class Requests {
    private final static OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder().build();

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
}
