package system.http.utils;

import com.google.gson.Gson;
import dto.server.UpdateUserInfoBody;
import engine.utils.ArchitectureType;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

import static utils.ServletConstants.*;

public class Requests {
    private final static SimpleCookieManager cookieManager = new SimpleCookieManager();
    private final static OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .cookieJar(cookieManager)
            .build();
    private final static Gson gson = new Gson();

    /**
     * Safely parses a URL string and returns a HttpUrl.Builder
     *
     * @param url The URL string to parse
     * @return A builder for the parsed URL
     * @throws IllegalArgumentException if the URL is invalid or cannot be parsed
     */
    private static @NotNull HttpUrl.Builder safeUrlBuilder(@NotNull String url) {
        HttpUrl httpUrl = HttpUrl.parse(url);
        if (httpUrl == null) {
            throw new IllegalArgumentException("Invalid URL: " + url);
        }
        return httpUrl.newBuilder();
    }

    /**
     * Uploads an XML file to the specified server endpoint <strong>asynchronously</strong>.
     *
     * @param serverEndpoint The server endpoint URL to which the file will be uploaded.
     * @param xmlFile        The XML file to be uploaded.
     * @param callback       The callback to handle the response or failure.
     */
    public static void uploadFileAsync(String serverEndpoint, File xmlFile, Callback callback) {
        Call call = createPostFileUploadCall(serverEndpoint, xmlFile);
        call.enqueue(callback);
    }

    public static @NotNull Response uploadFile(String uploadProgram, File xmlFile) throws IOException {
        Call call = createPostFileUploadCall(uploadProgram, xmlFile);
        return call.execute();
    }

    private static @NotNull Call createPostFileUploadCall(String uploadProgram, File xmlFile) {
        RequestBody fileBody = RequestBody.create(xmlFile, MediaType.parse("application/xml"));
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("xmlFile", xmlFile.getName(), fileBody)
                .build();

        Request request = new Request.Builder()
                .url(uploadProgram)
                .post(requestBody)
                .build();

        return HTTP_CLIENT.newCall(request);
    }

    /**
     * Retrieves system information <strong>synchronously</strong> from the specified server endpoint.
     *
     * @param serverEndpoint The server endpoint URL from which to retrieve the information.
     * @param info           The specific information to retrieve.
     * @return The response from the server.
     */
    public static @NotNull Response getSystemInfo(@NotNull String serverEndpoint, @NotNull String info) {
        Call call = getSystemInfoCall(serverEndpoint, info);
        try {
            return call.execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static @NotNull Response getAllUsersInSystem(@NotNull String serverEndpoint) throws IOException {
        HttpUrl url = safeUrlBuilder(serverEndpoint).build();

        Request request = new Request.Builder()
                .url(url)
                .build();

        Call call = HTTP_CLIENT.newCall(request);

        return call.execute();
    }

    /**
     * Constructs a Call object to retrieve system information.
     *
     * @param serverEndpoint The server endpoint URL.
     * @param info           The specific information to retrieve.
     * @return The Call object for the request.
     */
    private static @NotNull Call getSystemInfoCall(String serverEndpoint, String info) {
        HttpUrl url = safeUrlBuilder(serverEndpoint)
                .addQueryParameter(INFO_PARAM, info)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .build();

        return HTTP_CLIENT.newCall(request);
    }

    /**
     * Retrieves program information <strong>asynchronously</strong> from the specified server endpoint.
     *
     * @param serverEndpoint The server endpoint URL from which to retrieve the information.
     * @param info           The specific information to retrieve.
     * @param programName    The name of the program.
     * @param expandLevel    The level of detail to expand in the response.
     * @param callback       The callback to handle the response or failure.
     */
    public static void getProgramInfoAsync(@NotNull String serverEndpoint,
                                           @NotNull String info,
                                           @NotNull String programName,
                                           int expandLevel,
                                           @NotNull Callback callback) {

        Call call = getProgramInfoCall(serverEndpoint, info, programName, expandLevel);
        call.enqueue(callback);
    }

    /**
     * Retrieves program information <strong>synchronously</strong> from the specified server endpoint.
     *
     * @param serverEndpoint The server endpoint URL from which to retrieve the information.
     * @param info           The specific information to retrieve.
     * @param programName    The name of the program.
     * @param expandLevel    The level of detail to expand in the response.
     * @return The response from the server.
     */
    public static @NotNull Response getProgramInfo(@NotNull String serverEndpoint,
                                                   @NotNull String info,
                                                   @NotNull String programName,
                                                   int expandLevel)
            throws IOException {

        Call call = getProgramInfoCall(serverEndpoint, info, programName, expandLevel);
        return call.execute();
    }

    /**
     * Constructs a Call object to retrieve program information.
     *
     * @param serverEndpoint The server endpoint URL.
     * @param info           The specific information to retrieve.
     * @param programName    The name of the program.
     * @param expandLevel    The level of detail to expand in the response.
     * @return The Call object for the request.
     */
    private static @NotNull Call getProgramInfoCall(@NotNull String serverEndpoint,
                                                    @NotNull String info,
                                                    @NotNull String programName,
                                                    int expandLevel) {
        HttpUrl url = safeUrlBuilder(serverEndpoint)
                .addQueryParameter(INFO_PARAM, info)
                .addQueryParameter(PROGRAM_NAME_PARAM, programName)
                .addQueryParameter(EXPAND_LEVEL_PARAM, String.valueOf(expandLevel))
                .build();

        System.out.println("about to send request to: " + url);
        Request request = new Request.Builder()
                .url(url)
                .build();

        return HTTP_CLIENT.newCall(request);
    }

    public static void patchUserInfoAsync(@NotNull String serverEndpoint,
                                          @NotNull String username,
                                          @NotNull String infoToUpdate,
                                          @NotNull String newValue,
                                          @NotNull Callback callback) {
        HttpUrl url = safeUrlBuilder(serverEndpoint)
                .addQueryParameter(USERNAME_PARAM, username)
                .build();

        UpdateUserInfoBody updateUserInfoBody = new UpdateUserInfoBody(username, infoToUpdate, newValue);
        String jsonUserInfo = gson.toJson(updateUserInfoBody);

        Request request = getPatchJsonRequest(jsonUserInfo, url);

        Call call = HTTP_CLIENT.newCall(request);
        call.enqueue(callback);
    }

    /**
     * Sends a POST request to run a program <strong>asynchronously</strong>.
     *
     * @param serverEndpoint           The server endpoint URL.
     * @param currentLoadedProgramName The name of the currently loaded program.
     * @param jsonArguments            The JSON string containing the arguments for the program.
     * @param callback                 The callback to handle the response or failure.
     * @param expandLevel              The level of detail to expand in the response.
     */
    public static void postRunProgramAsync(@NotNull String serverEndpoint,
                                           @NotNull String currentLoadedProgramName,
                                           int expandLevel,
                                           @NotNull ArchitectureType architectureType,
                                           @NotNull String jsonArguments,
                                           @NotNull Callback callback) {
        postRunOrDebugProgramAsync(serverEndpoint, currentLoadedProgramName, architectureType, jsonArguments, callback
                , expandLevel);
    }

    /**
     * Sends a POST request to debug a program <strong>asynchronously</strong>.
     *
     * @param serverEndpoint The server endpoint URL.
     * @param programName    The name of the program to debug.
     * @param jsonArguments  The JSON string containing the arguments for the program.
     * @param callback       The callback to handle the response or failure.
     * @param expandLevel    The level of detail to expand in the response.
     */
    public static void postStartDebugAsync(@NotNull String serverEndpoint,
                                           @NotNull String programName,
                                           @NotNull ArchitectureType architectureType,
                                           @NotNull String jsonArguments,
                                           int expandLevel,
                                           @NotNull Callback callback
    ) {
        postRunOrDebugProgramAsync(serverEndpoint, programName, architectureType, jsonArguments, callback, expandLevel);
    }

    /**
     * Sends a POST request to run or debug a program <strong>asynchronously</strong>.
     *
     * @param serverEndpoint           The server endpoint URL.
     * @param currentLoadedProgramName The name of the currently loaded program.
     * @param jsonArguments            The JSON string containing the arguments for the program.
     * @param callback                 The callback to handle the response or failure.
     * @param expandLevel              The level of detail to expand in the response.
     */
    private static void postRunOrDebugProgramAsync(@NotNull String serverEndpoint,
                                                   @NotNull String currentLoadedProgramName,
                                                   @NotNull ArchitectureType architectureType,
                                                   @NotNull String jsonArguments,
                                                   @NotNull Callback callback,
                                                   int expandLevel) {
        Call call = buildRunOrDebugProgramCall(serverEndpoint, currentLoadedProgramName, architectureType,
                jsonArguments, expandLevel);

        call.enqueue(callback);
    }

    /**
     * Constructs a Call object to run or debug a program.
     *
     * @param serverEndpoint           The server endpoint URL.
     * @param currentLoadedProgramName The name of the currently loaded program.
     * @param jsonArguments            The JSON string containing the arguments for the program.
     * @param expandLevel              The level of detail to expand in the response.
     * @return The Call object for the request.
     */
    private static @NotNull Call buildRunOrDebugProgramCall(@NotNull String serverEndpoint,
                                                            @NotNull String currentLoadedProgramName,
                                                            @NotNull ArchitectureType architectureType,
                                                            @NotNull String jsonArguments, int expandLevel) {
        HttpUrl url = safeUrlBuilder(serverEndpoint)
                .addQueryParameter(PROGRAM_NAME_PARAM, currentLoadedProgramName)
                .addQueryParameter(EXPAND_LEVEL_PARAM, String.valueOf(expandLevel))
                .addQueryParameter(ARCHITECTURE_TYPE_PARAM, architectureType.getSymbol())
                .build();

        System.out.println("about to send request to: " + url);

        Request request = getPostJsonRequest(jsonArguments, url);

        return HTTP_CLIENT.newCall(request);
    }

    /**
     * Constructs a POST request with JSON body.
     *
     * @param jsonArguments The JSON string to be sent in the request body.
     * @param url           The URL to which the request will be sent.
     * @return The constructed Request object.
     */
    private static @NotNull Request getPostJsonRequest(@NotNull String jsonArguments, HttpUrl url) {
        RequestBody body = RequestBody.create(jsonArguments, MediaType.parse(JSON_CONTENT_TYPE));
        return new Request.Builder()
                .url(url)
                .post(body)
                .build();
    }

    private static @NotNull Request getPatchJsonRequest(@NotNull String jsonArguments, HttpUrl url) {
        RequestBody body = RequestBody.create(jsonArguments, MediaType.parse(JSON_CONTENT_TYPE));
        return new Request.Builder()
                .url(url)
                .patch(body)
                .build();
    }

    /**
     * Sends a POST request to perform a debug action <strong>asynchronously</strong>.
     *
     * @param serverEndpoint The server endpoint URL.
     * @param debugAction    The debug action to perform.
     * @param callback       The callback to handle the response or failure.
     */
    public static void postDebugActionAsync(@NotNull String serverEndpoint,
                                            @NotNull String debugAction,
                                            @NotNull Callback callback) {
        HttpUrl url = safeUrlBuilder(serverEndpoint)
                .addQueryParameter(DEBUG_ACTION_PARAM, debugAction)
                .build();

        Request request = getPostNoBody(url);

        Call call = HTTP_CLIENT.newCall(request);
        call.enqueue(callback);
    }

    /**
     * Retrieves user statistics <strong>asynchronously</strong> from the specified server endpoint.
     *
     * @param serverEndpoint The server endpoint URL from which to retrieve the statistics.
     * @param username       The username for which to retrieve statistics.
     * @param callback       The callback to handle the response or failure.
     */
    public static void getUserStatisticsAsync(@NotNull String serverEndpoint,
                                              @NotNull String username,
                                              @NotNull Callback callback) {
        HttpUrl url = safeUrlBuilder(serverEndpoint)
                .addQueryParameter(USERNAME_PARAM, username)
                .build();

        System.out.println("about to send request to: " + url);

        Request request = new Request.Builder()
                .url(url)
                .build();

        Call call = HTTP_CLIENT.newCall(request);
        call.enqueue(callback);
    }

    /**
     * Sends a POST request to register a new user <strong>synchronously</strong>.
     *
     * @param serverEndpoint The server endpoint URL.
     * @param username       The username of the new user to register.
     * @return The response from the server.
     */
    public static @NotNull Response postRegisterUser(@NotNull String serverEndpoint,
                                                     @NotNull String username) throws IOException {
        HttpUrl url = safeUrlBuilder(serverEndpoint)
                .addQueryParameter(USERNAME_PARAM, username)
                .build();

        Request request = getPostNoBody(url);

        Call call = HTTP_CLIENT.newCall(request);
        return call.execute();
    }

    /**
     * Sends a POST request to register a new user <strong>asynchronously</strong>.
     *
     * @param serverEndpoint The server endpoint URL.
     * @param username       The username of the new user to register.
     * @param callback       The callback to handle the response or failure.
     */
    public static void postRegisterUserAsync(@NotNull String serverEndpoint,
                                             @NotNull String username,
                                             @NotNull Callback callback) {
        HttpUrl url = safeUrlBuilder(serverEndpoint)
                .addQueryParameter(USERNAME_PARAM, username)
                .build();

        Request request = getPostNoBody(url);

        Call call = HTTP_CLIENT.newCall(request);
        call.enqueue(callback);
    }

    /**
     * Constructs a POST request without a body.
     *
     * @param url The URL to which the request will be sent.
     * @return The constructed Request object.
     */
    private static @NotNull Request getPostNoBody(HttpUrl url) {
        System.out.println("about to send request to: " + url);

        return new Request.Builder()
                .url(url)
                .post(RequestBody.create(new byte[0], null))
                .build();
    }

}
