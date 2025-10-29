package system.http.utils;

public class Endpoints {
    private static final String BASE_URL = "http://localhost:8080/engineWeb_Web";
    //    private static final String BASE_URL = "http://localhost:8080/engineWeb_Web_exploded";
    public static final String UPLOAD_PROGRAM = BASE_URL + "/uploadProgram";
    public static final String RUN_PROGRAM = BASE_URL + "/runProgram";
    public static final String GET_SYSTEM_INFO = BASE_URL + "/systemInfo";
    public static final String GET_PROGRAM_INFO = BASE_URL + "/programInfo";
    public static final String START_DEBUG_PROGRAM = BASE_URL + "/debugger/start";
    public static final String DEBUG_ACTION = BASE_URL + "/debugger/action";
    public static final String GET_ALL_USERS = BASE_URL + "/getAllUsersInSystem";
    public static final String GET_USER_EXECUTION_HISTORY = BASE_URL + "/user/ExecutionStatistics";
    public static final String REGISTER_USER = BASE_URL + "/users/register";
    public static final String UPDATE_USER_INFO = BASE_URL + "/updateUserInfo";

}
