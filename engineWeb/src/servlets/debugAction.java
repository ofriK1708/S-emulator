package servlets;

import com.google.gson.Gson;
import dto.engine.DebugStateChangeResultDTO;
import dto.engine.ExecutionResultStatisticsDTO;
import dto.engine.FullExecutionResultDTO;
import dto.server.SystemResponse;
import engine.core.ProgramDebugger;
import engine.exception.InstructionExecutionException;
import engine.exception.InsufficientCredits;
import engine.utils.DebugAction;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.User;
import logic.manager.ExecutionHistoryManager;
import logic.manager.ProgramManager;
import org.jetbrains.annotations.NotNull;
import utils.ServletUtils;

import java.io.IOException;

import static utils.ServletConstants.*;

@WebServlet(name = "debugAction", urlPatterns = "/debugger/action")
public class debugAction extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Get the user from the session and validate
        User user = ServletUtils.getUser(req, getServletContext());
        resp.setContentType(PLAIN_TEXT_CONTENT_TYPE);
        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().println("You must be logged in to perform debug actions.");
            return;
        }
        // Get the debugger for the user and validate
        ProgramDebugger debugger = user.getDebugger();
        if (debugger == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().println("No active debug session found for user " +
                    user.getName());
            return;
        }
        // Get the debug action parameter and perform the corresponding action
        String debugActionStr = req.getParameter(DEBUG_ACTION_PARAM);
        if (debugActionStr == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().println("Debug action parameter is missing. Available actions: " +
                    getAllDebugActionsOptions());
            return;
        }
        DebugAction debugAction = DebugAction.fromString(debugActionStr);
        if (debugAction == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().println("Unknown debug action: " + debugActionStr + ". Available actions: " +
                    getAllDebugActionsOptions());
            return;
        }

        DebugStateChangeResultDTO stateChange;
        resp.setContentType(JSON_CONTENT_TYPE);
        try {
            switch (debugAction) {
                case STEP_OVER -> {
                    stateChange = debugger.stepOver();
                    checkIfDebugEnded(stateChange, debugger, user);
                    writeDebugStateChangeResult(resp, stateChange);
                }
                case STEP_BACK -> {
                    stateChange = debugger.stepBack();
                    checkIfDebugEnded(stateChange, debugger, user);
                    writeDebugStateChangeResult(resp, stateChange);
                }
                case RESUME -> {
                    stateChange = debugger.resume();
                    checkIfDebugEnded(stateChange, debugger, user);
                    writeDebugStateChangeResult(resp, stateChange);
                }
                case STOP -> {
                    stateChange = debugger.stop();
                    checkIfDebugEnded(stateChange, debugger, user);
                    writeDebugStateChangeResult(resp, stateChange);
                }
            }
        } catch (InsufficientCredits insufficientCredits) {
            String errorMessage = getErrorMessage(insufficientCredits);
            int creditLeft = insufficientCredits.getCreditsLeft();
            handelFailedAction(errorMessage, creditLeft, user, resp,
                    HttpServletResponse.SC_PAYMENT_REQUIRED);
        } catch (InstructionExecutionException e) {
            String errorMessage = getErrorMessage(e);
            int creditsLeft = debugger.getRunningUserCredits();
            handelFailedAction(errorMessage, creditsLeft, user, resp,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handelFailedAction(String errorMessage, int creditsLeft, User user,
                                    HttpServletResponse resp, int errorStatus) throws IOException {
        Gson gson = new Gson();
        SystemResponse errorResponse = getErrorResponse(errorMessage, creditsLeft);
        user.setRemainingCredits(creditsLeft);
        user.clearDebugger();
        resp.setStatus(errorStatus);
        resp.setContentType(JSON_CONTENT_TYPE);
        resp.getWriter().write(gson.toJson(errorResponse));
    }

    private @NotNull String getErrorMessage(@NotNull Exception e) {
        return "Error executing debug action: " + e.getMessage();
    }

    private SystemResponse getErrorResponse(String errorMessage, int creditsLeft) throws IOException {
        return SystemResponse.builder()
                .isSuccess(false)
                .message(errorMessage)
                .creditsLeft(creditsLeft)
                .build();
    }

    /**
     * Checks if the debug session has ended and performs necessary cleanup and logging.
     *
     * @param stateChange The result of the debug state change.
     * @param debugger    The program debugger instance
     * @param user        The user performing the debug action.
     */
    private void checkIfDebugEnded(@NotNull DebugStateChangeResultDTO stateChange,
                                   @NotNull ProgramDebugger debugger,
                                   @NotNull User user) {
        if (stateChange.isFinished()) {
            ExecutionHistoryManager executionHistoryManager = ServletUtils.
                    getExecutionHistoryManager(getServletContext());
            ProgramManager programManager = ServletUtils.getProgramManager(getServletContext());
            programManager.getProgramOrFunctionEngine(
                            debugger.getProgramName())
                    .addExecutionStats(
                            debugger.getCreditCost()
                    );
            FullExecutionResultDTO fullExecutionResult = debugger.getDebugFinishedExecutionResult();
            executionHistoryManager.addExecutionResult(
                    user.getName(),
                    debugger.getProgramName(),
                    ExecutionResultStatisticsDTO.of(fullExecutionResult, user.getTotalRuns()));
            user.clearDebugger();
            user.chargeCredits(fullExecutionResult.creditsCost());
            user.incrementTotalRuns();
        }
    }

    /**
     * Writes the debug state change result as a JSON response.
     *
     * @param resp        The HTTP response object.
     * @param stateChange The debug state change result to write.
     * @throws IOException If an I/O error occurs during writing.
     */
    private void writeDebugStateChangeResult(HttpServletResponse resp, @NotNull DebugStateChangeResultDTO stateChange)
            throws IOException {
        Gson gson = new Gson();
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(gson.toJson(stateChange));
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.getWriter().println("available debug actions: " + getAllDebugActionsOptions());
    }
}
