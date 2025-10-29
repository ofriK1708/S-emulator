package servlets;

import com.google.gson.Gson;
import engine.core.Engine;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.manager.ProgramManager;
import utils.ServletUtils;

import java.io.IOException;

import static utils.ServletConstants.*;

@WebServlet(name = "getProgramInfo", urlPatterns = "/programInfo")
public class getProgramInfo extends HttpServlet {
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType(PLAIN_TEXT_CONTENT_TYPE);
        resp.getWriter().write(getAllProgramInfoOptionsNames());

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (ServletUtils.checkAndHandleUnauthorized(req, resp, getServletContext())) {
            int expandLevel = 0;
            Gson gson = new Gson();
            String programName = req.getParameter(PROGRAM_NAME_PARAM);
            ProgramManager pm = ServletUtils.getProgramManager(req.getServletContext());
            resp.setContentType(PLAIN_TEXT_CONTENT_TYPE); // default content type
            if (programName == null || programName.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().println("program name parameter is missing or invalid");
                return;
            }
            String infoToGet = req.getParameter(INFO_PARAM);
            if (infoToGet == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().println("Info parameter is missing or invalid, available options are: " +
                        getAllProgramInfoOptionsNames() + ".");
                return;
            }
            Engine currentEngine = pm.getProgramOrFunctionEngine(programName);
            if (isExpandLevelRequired(infoToGet)) {
                expandLevel = getAndValidateExpandLevel(req, resp, currentEngine);
            }
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType(JSON_CONTENT_TYPE); // set content type to JSON for valid responses
            System.out.println("current program: " + currentEngine.getInternalName() + ", info requested: " + infoToGet);

            switch (infoToGet) {
                case BASIC_PROGRAM_INFO -> resp.getWriter().write(gson.toJson(currentEngine.
                        getBasicProgramDTO()));

                case PROGRAM_BY_EXPAND_LEVEL_INFO -> resp.getWriter().write(gson.toJson(currentEngine.
                        getProgramByExpandLevelDTO(expandLevel)));

                case PROGRAMS_STATISTICS_INFO ->
                        resp.getWriter().write(gson.toJson(ServletUtils.getExecutionHistoryManager(getServletContext())
                                .getProgramExecutionHistory(programName)));

                case MAX_EXPAND_LEVEL_INFO -> resp.getWriter().write(gson.toJson(currentEngine.
                        getMaxExpandLevel()));

                case ALL_VARIABLES_AND_LABELS_INFO -> resp.getWriter().write(gson.toJson(currentEngine.
                        getAllVariablesNames(expandLevel, true)));

                case ARGUMENTS_INFO -> resp.getWriter().write(
                        gson.toJson(currentEngine.getSortedArgumentsMap(expandLevel)));

                case WORK_VARS_INFO -> resp.getWriter().write(gson.toJson(currentEngine.
                        getSortedWorkVars(expandLevel)));

                default -> {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.setContentType(PLAIN_TEXT_CONTENT_TYPE);
                    resp.getWriter().println("Info parameter is missing or invalid, if you want to see all available " +
                            "options," +
                            "please send an OPTIONS request to this URL");
                }
            }
        }
    }

    private int getAndValidateExpandLevel(HttpServletRequest req, HttpServletResponse resp, Engine engine)
            throws IOException {
        int expandLevel;
        String expandLevelStr = req.getParameter(EXPAND_LEVEL_PARAM);
        if (expandLevelStr == null || expandLevelStr.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().println("expandLevel parameter is missing");
            throw new IOException("expandLevel parameter is missing");
        }
        try {
            expandLevel = Integer.parseInt(expandLevelStr);
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().println("expandLevel parameter is not a valid number");
            throw new IllegalArgumentException("expandLevel parameter is not a valid number");
        }
        if (expandLevel < 0 || expandLevel > engine.getMaxExpandLevel()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().println("expandLevel must be between 0 and "
                    + engine.getMaxExpandLevel());
            throw new IllegalArgumentException("expandLevel must be between 0 and " + engine.getMaxExpandLevel());
        }
        return expandLevel;
    }

    private boolean isExpandLevelRequired(String infoToGet) {
        return infoToGet.equals(PROGRAM_BY_EXPAND_LEVEL_INFO) ||
                infoToGet.equals(ALL_VARIABLES_AND_LABELS_INFO) ||
                infoToGet.equals(ARGUMENTS_INFO) ||
                infoToGet.equals(PROGRAM_RESULT_INFO) ||
                infoToGet.equals(WORK_VARS_INFO); // add other cases if needed
    }
}
