package utils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.manager.ProgramManager;
import logic.manager.UserManager;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

import static utils.ServletConstants.*;

public class ServletUtils {
    private static final Object programManagerLock = new Object();
    private static final Object userManagerLock = new Object();

    public static ProgramManager getProgramManager(ServletContext servletContext) {
        synchronized (programManagerLock) {
            if (servletContext.getAttribute(PROGRAM_MANAGER_ATTRIBUTE_NAME) == null) {
                servletContext.setAttribute(PROGRAM_MANAGER_ATTRIBUTE_NAME, new ProgramManager());
            }
        }
        return (ProgramManager) servletContext.getAttribute("programManager");
    }

    public static UserManager getUserManager(ServletContext servletContext) {
        synchronized (userManagerLock) {
            if (servletContext.getAttribute(USER_MANAGER_ATTRIBUTE_NAME) == null) {
                servletContext.setAttribute(USER_MANAGER_ATTRIBUTE_NAME, new UserManager());
            }
        }
        return (UserManager) servletContext.getAttribute(USER_MANAGER_ATTRIBUTE_NAME);
    }

    /**
     * Extracts and validates 'programName' and 'expandLevel' from the request parameters.
     * Throws IllegalArgumentException with a user-friendly message if validation fails.
     */
    public static expandParams getAndValidateExpandParams(HttpServletRequest req, HttpServletResponse resp)
            throws IllegalArgumentException {
        String programName = req.getParameter(PROGRAM_NAME_PARAM);
        int expandLevel;
        try {
            expandLevel = Integer.parseInt(req.getParameter(EXPAND_LEVEL_PARAM));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The 'expandLevel' parameter is missing or is not a valid number.");
        }

        ProgramManager pm = ServletUtils.getProgramManager(req.getServletContext());
        if (programName == null || programName.isEmpty() || !pm.isProgramExists(programName)) {
            throw new IllegalArgumentException("The 'programName' parameter is missing.");
        }

        if (!pm.isProgramExists(programName)) {
            throw new IllegalArgumentException("A program with the name '" + programName + "' does not exist.");
        }
        return new expandParams(programName, expandLevel, pm);
    }

    /**
     * Extracts run/debug parameters. It gets 'programName' and 'expandLevel' from query parameters
     * and the 'arguments' map from the JSON request body.
     * Throws IllegalArgumentException with a user-friendly message if validation fails.
     */
    public static runAndDebugParams getAndValidateRunAndDebugParams(HttpServletRequest req, HttpServletResponse resp)
            throws Exception { // Can still throw IOException
        Gson gson = new Gson();
        Type argumentsMapType = new TypeToken<Map<String, Integer>>() {
        }.getType();

        // 1. Get query parameters first. This method now throws specific exceptions.
        expandParams expParams = getAndValidateExpandParams(req, resp);

        // 2. Get arguments from the request body.
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


        return new runAndDebugParams(expParams.programName, expParams.expandLevel, expParams.pm, arguments);
    }

    public record expandParams(@NotNull String programName, int expandLevel, @NotNull ProgramManager pm) {
    }

    public record runAndDebugParams(@NotNull String programName, int expandLevel, @NotNull ProgramManager pm,
                                    @NotNull Map<String, Integer> arguments) {
    }
}
