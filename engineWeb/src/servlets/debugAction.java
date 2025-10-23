package servlets;

import com.google.gson.Gson;
import dto.engine.DebugStateChangeResultDTO;
import dto.engine.ExecutionResultInfoDTO;
import engine.core.ExecutionStatistics;
import engine.core.ProgramDebugger;
import engine.exception.InsufficientCredits;
import engine.utils.DebugAction;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.User;
import logic.manager.ExecutionHistoryManager;
import org.jetbrains.annotations.NotNull;
import utils.ServletUtils;

import java.io.IOException;

import static utils.ServletConstants.DEBUG_ACTION_PARAM;
import static utils.ServletConstants.getAllDebugActionsOptions;

@WebServlet(name = "debugAction", urlPatterns = "/debugger/action")
public class debugAction extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Get the user from the session and validate
        User user = ServletUtils.getUser(req, getServletContext());
        if (user == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "You must be logged in to perform debug actions.");
            return;
        }
        // Get the debugger for the user and validate
        ProgramDebugger debugger = user.getDebugger();
        if (debugger == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No active debug session found for user " +
                    user.getName());
            return;
        }
        // Get the debug action parameter and perform the corresponding action
        String debugActionStr = req.getParameter(DEBUG_ACTION_PARAM);
        if (debugActionStr == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Debug action parameter is missing. Available actions: " + getAllDebugActionsOptions());
            return;
        }
        DebugAction debugAction = DebugAction.fromString(debugActionStr);
        if (debugAction == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Unknown debug action: " + debugActionStr + ". Available actions: " + getAllDebugActionsOptions());
            return;
        }

        DebugStateChangeResultDTO stateChange;
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
            user.setRemainingCredits(insufficientCredits.getCreditsLeft());
            user.clearDebugger();
            sendErrorMessage(resp, insufficientCredits);
        } catch (Exception e) {
            sendErrorMessage(resp, e);
        }
    }

    private void sendErrorMessage(HttpServletResponse resp, Exception e) throws IOException {
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                "Error executing debug action: " + e.getMessage());
    }

    /**
     * Checks if the debug session has ended and performs necessary cleanup and logging.
     *
     * @param stateChange The result of the debug state change.
     * @param debugger    The program debugger instance
     * @param user        The user performing the debug action.
     * @throws IOException If an I/O error occurs during logging.
     */
    private void checkIfDebugEnded(@NotNull DebugStateChangeResultDTO stateChange,
                                   @NotNull ProgramDebugger debugger,
                                   @NotNull User user) throws IOException {
        if (stateChange.isFinished()) {
            ExecutionHistoryManager executionHistoryManager = ServletUtils.
                    getExecutionHistoryManager(getServletContext());
            ExecutionResultInfoDTO fullExecutionResult = debugger.getDebugFinishedExecutionResult();
            executionHistoryManager.addExecutionResult(
                    user.getName(),
                    debugger.getProgramName(),
                    ExecutionStatistics.of(fullExecutionResult));
            user.clearDebugger();
            user.chargeCredits(fullExecutionResult.creditsCost());
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
        resp.setContentType("application/json");
        resp.getWriter().write(gson.toJson(stateChange));
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.getWriter().println("available debug actions: " + getAllDebugActionsOptions());
    }
}
