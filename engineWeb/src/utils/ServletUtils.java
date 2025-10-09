package utils;

import jakarta.servlet.ServletContext;
import logic.ProgramManager;

public class ServletUtils {
    private final static String PROGRAM_MANAGER_ATTRIBUTE_NAME = "programManager";
    private static final Object programManagerLock = new Object();

    public static ProgramManager getProgramManager(ServletContext servletContext) {
        synchronized (programManagerLock) {
            if (servletContext.getAttribute(PROGRAM_MANAGER_ATTRIBUTE_NAME) == null) {
                servletContext.setAttribute(PROGRAM_MANAGER_ATTRIBUTE_NAME, new ProgramManager());
            }
        }
        return (ProgramManager) servletContext.getAttribute("programManager");
    }
}
