package servlets;

import com.google.gson.Gson;
import dto.engine.ExecutionResultDTO;
import dto.engine.ExecutionResultStatisticsDTO;
import engine.core.Engine;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.User;
import logic.manager.ExecutionHistoryManager;
import utils.ServletUtils;

import java.io.IOException;
import java.util.Map;

@WebServlet(name = "runProgram", urlPatterns = "/runProgram")
public class runProgram extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = ServletUtils.getUser(req, getServletContext());
        ExecutionHistoryManager executionHistoryManager = ServletUtils.getExecutionHistoryManager(getServletContext());
        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("Error! User is not logged in.");
            return;
        }
        Gson gson = new Gson();
        resp.setContentType("application/json");
        ServletUtils.runAndDebugParams runAndDebugParams;
        try {
            runAndDebugParams = ServletUtils.getAndValidateRunAndDebugParams(req);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Error! " + e.getMessage());
            return;
        }
        String programName = runAndDebugParams.programName();
        int expandLevel = runAndDebugParams.expandLevel();
        Engine currentEngine = runAndDebugParams.pm().getProgramOrFunctionEngine(programName);
        Map<String, Integer> args = runAndDebugParams.arguments();
        try {
            ExecutionResultDTO executionResultDTO = currentEngine.run(
                    expandLevel, args, user.getCurrentCredits(), runAndDebugParams.architectureType());
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(gson.toJson(executionResultDTO));
            executionHistoryManager.addExecutionResult(
                    user.getName(), programName, ExecutionResultStatisticsDTO.of(executionResultDTO));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("text/plain");
            String errorMessage = String.format(
                    "Error trying to run %s at expand level %d: %s",
                    runAndDebugParams.programName(),
                    expandLevel,
                    e.getMessage()
            );
            resp.getWriter().write(errorMessage);
        }
    }
}
