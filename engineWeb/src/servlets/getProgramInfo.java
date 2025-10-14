package servlets;

import com.google.gson.Gson;
import dto.engine.ExecutionStatisticsDTO;
import engine.core.ProgramEngine;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.manager.ProgramManager;
import utils.ServletUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static utils.ServletConstants.*;

@WebServlet(name = "getProgramInfo", urlPatterns = "/programInfo")
public class getProgramInfo extends HttpServlet {
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html;charset=utf-8");
        resp.getWriter().println(getAllProgramInfoOptionsNames());
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int expandLevel = -1;
        Gson gson = new Gson();
        String programName = req.getParameter(PROGRAM_NAME_PARAM);
        String infoToGet = req.getParameter(INFO_PARAM);
        ProgramManager pm = ServletUtils.getProgramManager(req.getServletContext());
        if (programName == null || programName.isEmpty() || !pm.isProgramExists(programName)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "programName parameter is missing or invalid");
            return;
        }
        ProgramEngine currentEngine = pm.getProgramOrFunctionEngine(programName);
        resp.setContentType("application/json;charset=UTF-8");
        if (isExpandLevelRequired(infoToGet)) {
            try {
                expandLevel = getAndValidateExpandLevel(req, resp, currentEngine);
            } catch (RuntimeException ignored) {
                return;
            }
        }
        System.out.println("current program: " + currentEngine.getProgramName() + ", info requested: " + infoToGet);
        switch (infoToGet) {
            case BASIC_PROGRAM:
                resp.getWriter().println(gson.toJson(currentEngine.getBasicProgramDTO()));
                break;
            case PROGRAM_BY_EXPAND_LEVEL:
                resp.getWriter().println(gson.toJson(currentEngine.getProgramByExpandLevelDTO(expandLevel)));
                break;
            case MAX_EXPAND_LEVEL:
                resp.getWriter().println(gson.toJson(currentEngine.getMaxExpandLevel()));
                resp.setStatus(HttpServletResponse.SC_OK);
                break;
            case ALL_VARIABLES_AND_LABELS:
                Set<String> allVarsAndLabels = currentEngine.getAllVariablesNames(expandLevel, true);
                String json = gson.toJson(allVarsAndLabels);
                resp.getWriter().println(json);
                resp.setStatus(HttpServletResponse.SC_OK);
                break;
            case ARGUMENTS:
                Map<String, Integer> sortedArguments = currentEngine.getSortedArguments(expandLevel);
                String jsonArgs = gson.toJson(sortedArguments);
                resp.getWriter().println(jsonArgs);
                resp.setStatus(HttpServletResponse.SC_OK);
                break;
            case PROGRAM_RESULT:
                resp.getWriter().println(gson.toJson(currentEngine.getOutput(expandLevel)));
                resp.setStatus(HttpServletResponse.SC_OK);
                break;
            case WORK_VARS:
                Map<String, Integer> workVars = currentEngine.getSortedWorkVars(expandLevel);
                String jsonWorkVars = gson.toJson(workVars);
                resp.getWriter().println(jsonWorkVars);
                resp.setStatus(HttpServletResponse.SC_OK);
                break;
            case ALL_EXECUTION_STATISTICS:
                List<ExecutionStatisticsDTO> allStats = currentEngine.getAllExecutionStatistics();
                String jsonStats = gson.toJson(allStats);
                resp.getWriter().println(jsonStats);
                resp.setStatus(HttpServletResponse.SC_OK);
                break;
            case LAST_EXECUTION_STATISTICS:
                List<ExecutionStatisticsDTO> allStatsList = currentEngine.getAllExecutionStatistics();
                if (allStatsList.isEmpty()) {
                    resp.getWriter().println("No execution statistics available, try running the program first");
                } else {
                    String jsonLastStats = gson.toJson(allStatsList.getLast());
                    resp.getWriter().println(jsonLastStats);
                }
                resp.setStatus(HttpServletResponse.SC_OK);
                break;
            case LAST_EXECUTION_CYCLES:
                resp.getWriter().println(gson.toJson(currentEngine.getLastExecutionCycles()));
                resp.setStatus(HttpServletResponse.SC_OK);
                break;
            default:
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "info parameter is missing or invalid, if you want to see all available options," +
                                "please send an OPTIONS request to this URL");
                break;
        }
    }

    private int getAndValidateExpandLevel(HttpServletRequest req, HttpServletResponse resp, ProgramEngine engine)
            throws IOException {
        int expandLevel;
        String expandLevelStr = req.getParameter(EXPAND_LEVEL_PARAM);
        if (expandLevelStr == null || expandLevelStr.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "expandLevel parameter is missing");
            throw new IOException("expandLevel parameter is missing");
        }
        try {
            expandLevel = Integer.parseInt(expandLevelStr);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "expandLevel parameter is not a valid number");
            throw new IllegalArgumentException("expandLevel parameter is not a valid number");
        }
        if (expandLevel < 0 || expandLevel > engine.getMaxExpandLevel()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "expandLevel must be between 0 and "
                    + engine.getMaxExpandLevel());
            throw new IllegalArgumentException("expandLevel must be between 0 and " + engine.getMaxExpandLevel());
        }
        return expandLevel;
    }

    private boolean isExpandLevelRequired(String infoToGet) {
        return infoToGet.equals(ALL_VARIABLES_AND_LABELS) ||
                infoToGet.equals(ARGUMENTS) ||
                infoToGet.equals(PROGRAM_RESULT) ||
                infoToGet.equals(WORK_VARS); // add other cases if needed
    }
}
