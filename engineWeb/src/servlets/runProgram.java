package servlets;

import com.google.gson.Gson;
import dto.engine.ExecutionResultStatisticsDTO;
import dto.engine.FullExecutionResultDTO;
import engine.core.Engine;
import engine.exception.InsufficientCredits;
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
    protected synchronized void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = ServletUtils.getUser(req, getServletContext());
        if (user == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "You must be logged in to run a program.");
            return;
        }

        ExecutionHistoryManager executionHistoryManager = ServletUtils.getExecutionHistoryManager(getServletContext());
        Gson gson = new Gson();
        resp.setContentType("application/json");
        ServletUtils.runAndDebugParams runAndDebugParams;
        runAndDebugParams = ServletUtils.getAndValidateRunAndDebugParams(req, resp);
        if (runAndDebugParams == null) {
            return;
        }

        String programName = runAndDebugParams.programName();
        Engine currentEngine = runAndDebugParams.pm().getProgramOrFunctionEngine(programName);
        int expandLevel = runAndDebugParams.expandLevel();
        Map<String, Integer> args = runAndDebugParams.arguments();

        try {
            FullExecutionResultDTO fullExecutionResultDTO = currentEngine.mainRun(
                    expandLevel, args, user.getCurrentCredits(), runAndDebugParams.architectureType());

            user.increaseTotalRuns();
            user.chargeCredits(fullExecutionResultDTO.creditsCost());

            executionHistoryManager.addExecutionResult(
                    user.getName(), programName, ExecutionResultStatisticsDTO.of(fullExecutionResultDTO,
                            user.getTotalRuns()));

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(gson.toJson(fullExecutionResultDTO));

        } catch (InsufficientCredits insufficientCredits) {
            user.setRemainingCredits(insufficientCredits.getCreditsLeft());
            writeErrorMessage(resp, insufficientCredits, runAndDebugParams, expandLevel);
        } catch (Exception e) {
            writeErrorMessage(resp, e, runAndDebugParams, expandLevel);
        }
    }

    private void writeErrorMessage(HttpServletResponse resp, Exception e,
                                   ServletUtils.runAndDebugParams runAndDebugParams, int expandLevel) throws IOException {
        resp.setContentType("text/plain");
        String errorMessage = String.format(
                "Error trying to run %s at expand level %d: %s",
                runAndDebugParams.programName(),
                expandLevel,
                e.getMessage()
        );
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, errorMessage);
    }
}
