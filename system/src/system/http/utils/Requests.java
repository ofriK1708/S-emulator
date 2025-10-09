package system.http.utils;

import okhttp3.*;

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
}
