package utils;

import com.google.gson.reflect.TypeToken;
import dto.server.UserDTO;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

public class ServletConstants {
    // region Attributes Names
    public static final String PROGRAM_MANAGER_ATTRIBUTE_NAME = "programManager";
    public static final String USER_MANAGER_ATTRIBUTE_NAME = "userManager";
    public static final String EXECUTION_HISTORY_MANAGER_ATTRIBUTE_NAME = "executionHistoryManager";
    // endregion

    // region Parameters Names
    public static final String INFO_PARAM = "info";
    public static final String PROGRAM_NAME_PARAM = "program_name";
    public static final String ARCHITECTURE_TYPE_PARAM = "architecture_type";
    public static final String EXPAND_LEVEL_PARAM = "expand_level";
    public static final String USERNAME_PARAM = "username";
    public static final String DEBUG_ACTION_PARAM = "debug_action";
    // endregion

    // region info queries options names
    public static final String PROGRAMS_NAMES_INFO = "programs_names";
    public static final String FUNCTIONS_NAMES_INFO = "functions_names";
    public static final String ALL_NAMES_INFO = "all_names";
    public static final String USER_STATISTICS_INFO = "user_statistics";
    public static final String PROGRAMS_STATISTICS_INFO = "programs_statistics";
    public static final String ALL_USERS_INFO = "all_users";
    public static final String BASIC_PROGRAM_INFO = "basic_program";
    public static final String PROGRAM_BY_EXPAND_LEVEL_INFO = "program_by_expand_level";
    public static final String MAX_EXPAND_LEVEL_INFO = "max_expand_level";
    public static final String ALL_VARIABLES_AND_LABELS_INFO = "all_variables_and_labels";
    public static final String ARGUMENTS_INFO = "arguments";
    public static final String PROGRAM_RESULT_INFO = "program_result";
    public static final String WORK_VARS_INFO = "work_vars";
    // endregion

    // region Debug Actions Names
    public static final String DEBUG_ACTION_STEP_OVER = "step_over";
    public static final String DEBUG_ACTION_STEP_BACK = "step_back";
    public static final String DEBUG_ACTION_RESUME = "resume";
    public static final String DEBUG_ACTION_STOP = "pause";
    // endregion

    // region classes Types (for Gson deserialization)
    public static final Type ARGUMENTS_MAP_TYPE = new TypeToken<Map<String, Integer>>() {
    }.getType();
    public static final Type USER_DTO_SET_TYPE = new TypeToken<Set<UserDTO>>() {
    }.getType();
    // endregion

    // region helper methods for all options names
    @Contract(pure = true)
    public static @NotNull String getAllSystemInfoOptionsNames() {
        return "[" +
                PROGRAMS_NAMES_INFO + ", " +
                FUNCTIONS_NAMES_INFO + ", " +
                ALL_NAMES_INFO + "]";
    }

    @Contract(pure = true)
    public static @NotNull String getAllProgramInfoOptionsNames() {
        return "[" +
                BASIC_PROGRAM_INFO + ", " +
                PROGRAM_BY_EXPAND_LEVEL_INFO + ", " +
                MAX_EXPAND_LEVEL_INFO + ", " +
                ALL_VARIABLES_AND_LABELS_INFO + ", " +
                ARGUMENTS_INFO + ", " +
                PROGRAM_RESULT_INFO + ", " +
                WORK_VARS_INFO + "]";
    }

    @Contract(pure = true)
    public static @NotNull String getAllUsersOptionsNames() {
        return "[" +
                ALL_USERS_INFO + ", " +
                USER_STATISTICS_INFO + "]";
    }

    @Contract(pure = true)
    public static @NotNull String getAllDebugActionsOptions() {
        return "[" +
                DEBUG_ACTION_STEP_OVER + ", " +
                DEBUG_ACTION_STEP_BACK + ", " +
                DEBUG_ACTION_RESUME + ", " +
                DEBUG_ACTION_STOP + "]";
    }
    // endregion


}
