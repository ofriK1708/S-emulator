package servlets;

import com.google.gson.Gson;
import dto.engine.ExecutionResultStatisticsDTO;
import dto.engine.FullExecutionResultDTO;
import dto.server.SystemResponse;
import engine.core.Engine;
import engine.exception.ExpandLevelOutOfBounds;
import engine.exception.IllegalArchitectureType;
import engine.exception.InstructionExecutionException;
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

import static utils.ServletConstants.JSON_CONTENT_TYPE;
import static utils.ServletConstants.PLAIN_TEXT_CONTENT_TYPE;

@WebServlet(name = "runProgram", urlPatterns = "/runProgram")
public class runProgram extends HttpServlet {
    @Override
    protected synchronized void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = ServletUtils.getUser(req, getServletContext());
        resp.setContentType(PLAIN_TEXT_CONTENT_TYPE); // default content type
        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().println("You must be logged in to run a program.");
            return;
        }

        ExecutionHistoryManager executionHistoryManager = ServletUtils.getExecutionHistoryManager(getServletContext());
        Gson gson = new Gson();
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
            resp.setContentType(JSON_CONTENT_TYPE);
            resp.getWriter().write(gson.toJson(fullExecutionResultDTO));

        } catch (InsufficientCredits insufficientCredits) {
            String errorMessage = getErrorMessage(insufficientCredits, runAndDebugParams, expandLevel);

            handelFailedRun(resp, HttpServletResponse.SC_PAYMENT_REQUIRED, errorMessage,
                    insufficientCredits.getCreditsLeft(), user);

        } catch (InstructionExecutionException e) {
            String errorMessage = getErrorMessage(e, runAndDebugParams, expandLevel);

            handelFailedRun(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMessage, e.getRemainingCredits()
                    , user);

        } catch (IllegalArchitectureType | ExpandLevelOutOfBounds e) {
            handelFailedRunStringResponse(resp, e, HttpServletResponse.SC_BAD_REQUEST, runAndDebugParams, expandLevel);

        } catch (Exception e) {
            handelFailedRunStringResponse(resp, e, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, runAndDebugParams,
                    expandLevel);
        }
    }

    private void handelFailedRunStringResponse(HttpServletResponse resp, Exception e,
                                               int stausCode, ServletUtils.runAndDebugParams runAndDebugParams,
                                               int expandLevel) throws IOException {
        String errorMessage = getErrorMessage(e, runAndDebugParams, expandLevel);

        resp.setContentType(PLAIN_TEXT_CONTENT_TYPE);
        resp.setStatus(stausCode);
        resp.getWriter().write(errorMessage);
    }

    private void handelFailedRun(HttpServletResponse resp, int statusCode, String errorMessage, int creditsLeft,
                                 User user) throws IOException {
        Gson gson = new Gson();
        user.setRemainingCredits(creditsLeft);
        resp.setStatus(statusCode);
        resp.setContentType(JSON_CONTENT_TYPE);
        SystemResponse errorResponse = getErrorResponse(errorMessage, creditsLeft);
        resp.getWriter().write(gson.toJson(errorResponse));
    }

    private String getErrorMessage(Exception e,
                                   ServletUtils.runAndDebugParams runAndDebugParams, int expandLevel) throws IOException {
        return String.format(
                "Error trying to run %s at expand level %d: %s",
                runAndDebugParams.programName(),
                expandLevel,
                e.getMessage()
        );
    }

    private SystemResponse getErrorResponse(String errorMessage, int creditLeft) throws IOException {
        return SystemResponse.builder()
                .isSuccess(false)
                .message(errorMessage)
                .creditsLeft(creditLeft)
                .build();
    }
}
