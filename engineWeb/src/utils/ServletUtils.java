package utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.manager.ProgramManager;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static utils.ServletConstants.*;

public class ServletUtils {
    private static final Object programManagerLock = new Object();

    public static ProgramManager getProgramManager(ServletContext servletContext) {
        synchronized (programManagerLock) {
            if (servletContext.getAttribute(PROGRAM_MANAGER_ATTRIBUTE_NAME) == null) {
                servletContext.setAttribute(PROGRAM_MANAGER_ATTRIBUTE_NAME, new ProgramManager());
            }
        }
        return (ProgramManager) servletContext.getAttribute("programManager");
    }

    public static expandParams getAndValidateExpandParams(HttpServletRequest req, HttpServletResponse resp)
            throws Exception {
        String programName = req.getParameter(PROGRAM_NAME_PARAM);
        int expandLevel;
        try {
            expandLevel = Integer.parseInt(req.getParameter(EXPAND_LEVEL_PARAM));
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "expandLevel parameter is missing or not a number");
            throw e;
        }
        ProgramManager pm = ServletUtils.getProgramManager(req.getServletContext());
        if (programName == null || programName.isEmpty() || !pm.isProgramExists(programName)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "programName parameter is missing or program doesn't exist");
            throw new IllegalArgumentException();
        }
        return new expandParams(programName, expandLevel, pm);
    }

    public static runAndDebugParams getAndValidateRunAndDebugParams(HttpServletRequest req, HttpServletResponse resp)
            throws Exception {
        Gson gson = new Gson();
        Type argumentsMapType = new TypeToken<Map<String, Integer>>() {
        }.getType();
        expandParams expParams = getAndValidateExpandParams(req, resp);
        Map<String, Integer> arguments = new HashMap<>();
        String argumentsJsonStr = req.getParameter(ARGUMENTS);

        // some programs don't require arguments
        if (argumentsJsonStr != null && argumentsJsonStr.isEmpty()) {
            arguments = gson.fromJson(argumentsJsonStr, argumentsMapType);
        }

        return new runAndDebugParams(expParams.programName, expParams.expandLevel, expParams.pm, arguments);
    }

    public record expandParams(@NotNull String programName, int expandLevel, @NotNull ProgramManager pm) {
    }

    public record runAndDebugParams(@NotNull String programName, int expandLevel, @NotNull ProgramManager pm,
                                    @NotNull Map<String, Integer> arguments) {
    }
}
