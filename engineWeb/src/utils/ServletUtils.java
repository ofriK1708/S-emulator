package utils;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.manager.ProgramManager;

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

    public static runAndExpandParams validateAndGetParams(HttpServletRequest req, HttpServletResponse resp)
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
        return new runAndExpandParams(programName, expandLevel, pm);
    }

    public record runAndExpandParams(String programName, int expandLevel, ProgramManager pm) {
    }
}
