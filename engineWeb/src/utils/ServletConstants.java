package utils;

public class ServletConstants {
    public static final String PROGRAM_MANAGER_ATTRIBUTE_NAME = "programManager";
    public static final String INFO_PARAM = "info";
    public static final String PROGRAMS_NAMES_INFO = "programsNames";
    public static final String FUNCTIONS_NAMES_INFO = "functionsNames";
    public static final String ALL_NAMES_INFO = "allNames";
    public static final String PROGRAM_NAME_PARAM = "programName";
    public static final String BASIC_PROGRAM = "basicProgram";
    public static final String PROGRAM_BY_EXPAND_LEVEL = "programByExpandLevel";
    public static final String EXPAND_LEVEL_PARAM = "expandLevel";
    public static final String MAX_EXPAND_LEVEL = "maxExpandLevel";
    public static final String ALL_VARIABLES_AND_LABELS = "allVariablesAndLabels";
    public static final String ARGUMENTS = "arguments";
    public static final String PROGRAM_RESULT = "programResult";
    public static final String WORK_VARS = "workVars";
    public static final String ALL_EXECUTION_STATISTICS = "allExecutionStatistics";
    public static final String LAST_EXECUTION_STATISTICS = "lastExecutionStatistics";
    public static final String LAST_EXECUTION_CYCLES = "lastExecutionCycles";
    public static final String DEBUG_ACTION_PARAM = "debugAction";
    public static final String DEBUG_ACTION_STEP_OVER = "stepOver";
    public static final String DEBUG_ACTION_STEP_BACK = "stepBack";
    public static final String DEBUG_ACTION_RESUME = "resume";
    public static final String DEBUG_ACTION_STOP = "pause";

    public static String getAllSystemInfoOptionsNames() {
        return "[" +
                PROGRAMS_NAMES_INFO + ", " +
                FUNCTIONS_NAMES_INFO + ", " +
                ALL_NAMES_INFO + "]";
    }

    public static String getAllProgramInfoOptionsNames() {
        return "[" +
                MAX_EXPAND_LEVEL + ", " +
                ALL_VARIABLES_AND_LABELS + ", " +
                ARGUMENTS + ", " +
                PROGRAM_RESULT + ", " +
                WORK_VARS + ", " +
                ALL_EXECUTION_STATISTICS + ", " +
                LAST_EXECUTION_STATISTICS + ", " +
                LAST_EXECUTION_CYCLES + "]";
    }


}
