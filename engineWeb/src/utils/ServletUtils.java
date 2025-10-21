package utils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import engine.utils.ArchitectureType;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import logic.User;
import logic.manager.ExecutionHistoryManager;
import logic.manager.ProgramManager;
import logic.manager.UserManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
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
        String username = session != null ? session.getAttribute(USERNAME_PARAM).toString() : null;
        return getUserManager(servletContext).getUser(username);
    }

    /**
     * Checks if the user is authenticated. If not, sends a 401 Unauthorized response.
     * Returns true if the user is authenticated, false otherwise.
     *
     * @param request        The HTTP request
     * @param response       The HTTP response
     * @param servletContext The servlet context
     * @return true if the user is authenticated, false otherwise
     * @throws IOException if an I/O error occurs while sending the error response
     */
    public static boolean checkAndHandleUnauthorized(HttpServletRequest request, HttpServletResponse response,
                                                     ServletContext servletContext) throws IOException {
        if (getUser(request, servletContext) == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error! User is not logged in.");
            return false;
        }
        return true;
    }
    /**
     * Extracts and validates 'programName' and 'expandLevel' parameters from the request.
     * @param req: The HTTP request
     * @param resp: The HTTP response
     * @return An expandParams object containing the validated parameters, or null if an error response has been sent.
     *
     * @throws IllegalArgumentException with a user-friendly message if validation fails.
     */
    public static @Nullable expandParams getAndValidateExpandParams(HttpServletRequest req, HttpServletResponse resp)
            throws IllegalArgumentException, IOException {
        String programName = req.getParameter(PROGRAM_NAME_PARAM);
        String expandLevelStr = req.getParameter(EXPAND_LEVEL_PARAM);
        int expandLevel;
        try {
            expandLevel = Integer.parseInt(expandLevelStr);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "The " + EXPAND_LEVEL_PARAM + " ( " + expandLevelStr + " ) parameter is missing or " +
                            "is not a valid number.");
            return null;
        }

        ProgramManager pm = ServletUtils.getProgramManager(req.getServletContext());
        if (programName == null || programName.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "The " + PROGRAM_NAME_PARAM + " parameter is missing or invalid.");
            return null;
        }

        if (!pm.isFunctionOrProgramExists(programName)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "The program/function with name '" + programName + "' does not exist.");
            return null;
        }
        return new expandParams(programName, expandLevel, pm);
    }

    /**
     * Extracts run/debug parameters. It gets 'programName' and 'expandLevel' from query parameters
     * and the 'arguments' map from the JSON request body.
     * @param req: The HTTP request
     * @param resp: The HTTP response
     * @return A runAndDebugParams object containing the validated parameters,
     * or null if an error response has been sent.
     * @throws IllegalArgumentException with a user-friendly message if validation fails.
     */
    public static @Nullable runAndDebugParams getAndValidateRunAndDebugParams(HttpServletRequest req,
                                                                              HttpServletResponse resp) throws IOException {
        Gson gson = new Gson();

        // 1. Get program name, expand level and program manager.
        expandParams expParams = getAndValidateExpandParams(req, resp);
        if (expParams == null) {
            return null;
        }

        // 2. get architecture type
        String architectureTypeStr = req.getParameter(ARCHITECTURE_TYPE_PARAM);
        if (architectureTypeStr == null || architectureTypeStr.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "The " + ARCHITECTURE_TYPE_PARAM + " parameter is missing." +
                            "the supported types are: " + ArchitectureType.getSupportedArchitectures());
            return null;
        }
        if (!ArchitectureType.isValidArchitectureType(architectureTypeStr)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "The " + ARCHITECTURE_TYPE_PARAM + " parameter is missing." +
                            "the supported types are: " + ArchitectureType.getSupportedArchitectures());
            return null;
        }
        ArchitectureType architectureType = ArchitectureType.fromString(architectureTypeStr);

        // 3. Get arguments from the request body.
        Map<String, Integer> arguments;
        try (BufferedReader reader = req.getReader()) {
            arguments = gson.fromJson(reader, ARGUMENTS_MAP_TYPE);
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
