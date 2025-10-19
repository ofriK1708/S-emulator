package utils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import engine.utils.ArchitectureType;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import logic.User;
import logic.manager.ExecutionHistoryManager;
import logic.manager.ProgramManager;
import logic.manager.UserManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

import static utils.ServletConstants.*;

public class ServletUtils {
    private static final Object programManagerLock = new Object();
    private static final Object userManagerLock = new Object();
    private static final Object executionHistoryManagerLock = new Object();

    public static @NotNull ProgramManager getProgramManager(ServletContext servletContext) {
        synchronized (programManagerLock) {
            if (servletContext.getAttribute(PROGRAM_MANAGER_ATTRIBUTE_NAME) == null) {
                servletContext.setAttribute(PROGRAM_MANAGER_ATTRIBUTE_NAME, ProgramManager.getInstance());
            }
        }
        return (ProgramManager) servletContext.getAttribute("programManager");
    }

    public static @NotNull UserManager getUserManager(ServletContext servletContext) {
        synchronized (userManagerLock) {
            if (servletContext.getAttribute(USER_MANAGER_ATTRIBUTE_NAME) == null) {
                servletContext.setAttribute(USER_MANAGER_ATTRIBUTE_NAME, UserManager.getInstance());
            }
        }
        return (UserManager) servletContext.getAttribute(USER_MANAGER_ATTRIBUTE_NAME);
    }

    public static @NotNull ExecutionHistoryManager getExecutionHistoryManager(ServletContext servletContext) {
        synchronized (executionHistoryManagerLock) {
            if (servletContext.getAttribute(EXECUTION_HISTORY_MANAGER_ATTRIBUTE_NAME) == null) {
                servletContext.setAttribute(EXECUTION_HISTORY_MANAGER_ATTRIBUTE_NAME,
                        ExecutionHistoryManager.getInstance());
            }
        }
        return (ExecutionHistoryManager) servletContext.getAttribute(EXECUTION_HISTORY_MANAGER_ATTRIBUTE_NAME);
    }

    /**
     * Retrieves the User object associated with the current session.
     * Returns null if no user is logged in.
     * does not create a new session if one does not exist.
     */
    public static @Nullable User getUser(HttpServletRequest request, ServletContext servletContext) {
        HttpSession session = request.getSession(false);
        String username = session != null ? session.getAttribute(USERNAME).toString() : null;
        return getUserManager(servletContext).getUser(username);
    }

    public static boolean isUserNotAuthenticated(HttpServletRequest request, ServletContext servletContext) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return true;
        }
        String username = session.getAttribute(USERNAME) != null ? session.getAttribute(USERNAME).toString() : null;
        if (username == null) {
            return true;
        }
        User user = getUserManager(servletContext).getUser(username);
        return user == null;
    }

    /**
     * Extracts and validates 'programName' and 'expandLevel' from the request parameters.
     * Throws IllegalArgumentException with a user-friendly message if validation fails.
     */
    public static @NotNull expandParams getAndValidateExpandParams(HttpServletRequest req)
            throws IllegalArgumentException {
        String programName = req.getParameter(PROGRAM_NAME_PARAM);
        int expandLevel;
        try {
            expandLevel = Integer.parseInt(req.getParameter(EXPAND_LEVEL_PARAM));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The 'expandLevel' parameter is missing or is not a valid number.");
        }

        ProgramManager pm = ServletUtils.getProgramManager(req.getServletContext());
        if (programName == null || programName.isEmpty()) {
            throw new IllegalArgumentException("The 'programName' parameter is missing.");
        }

        if (!pm.isFunctionOrProgramExists(programName)) {
            throw new IllegalArgumentException("A program with the name '" + programName + "' does not exist.");
        }
        return new expandParams(programName, expandLevel, pm);
    }

    /**
     * Extracts run/debug parameters. It gets 'programName' and 'expandLevel' from query parameters
     * and the 'arguments' map from the JSON request body.
     * Throws IllegalArgumentException with a user-friendly message if validation fails.
     */
    public static @NotNull runAndDebugParams getAndValidateRunAndDebugParams(HttpServletRequest req)
            throws Exception { // Can still throw IOException
        Gson gson = new Gson();
        Type argumentsMapType = new TypeToken<Map<String, Integer>>() {
        }.getType();

        // 1. Get program name, expand level and program manager.
        expandParams expParams = getAndValidateExpandParams(req);

        // 2. get architecture type
        String architectureTypeStr = req.getParameter(ARCHITECTURE_TYPE_PARAM);
        if (architectureTypeStr == null || architectureTypeStr.isEmpty()) {
            throw new IllegalArgumentException("The 'architectureType' parameter is missing." +
                    "the supported types are: " + ArchitectureType.getSupportedArchitectures());
        }
        if (!ArchitectureType.isValidArchitectureType(architectureTypeStr)) {
            throw new IllegalArgumentException("The 'architectureType' parameter is invalid." +
                    "the supported types are: " + ArchitectureType.getSupportedArchitectures());
        }
        ArchitectureType architectureType = ArchitectureType.fromString(architectureTypeStr);

        // 3. Get arguments from the request body.
        Map<String, Integer> arguments;
        try (BufferedReader reader = req.getReader()) {
            arguments = gson.fromJson(reader, argumentsMapType);
            // Handle the case of an empty or null JSON body gracefully.
            if (arguments == null) {
                arguments = Collections.emptyMap();
            }
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("Invalid JSON format in request body: " + e.getMessage());
        }


        return new runAndDebugParams(expParams.programName, expParams.expandLevel, expParams.pm, arguments,
                architectureType);
    }

    public record expandParams(@NotNull String programName, int expandLevel, @NotNull ProgramManager pm) {
    }

    public record runAndDebugParams(@NotNull String programName, int expandLevel,
                                    @NotNull ProgramManager pm,
                                    @NotNull Map<String, Integer> arguments,
                                    @NotNull ArchitectureType architectureType) {
    }
}
