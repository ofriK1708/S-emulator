package servlets;

import com.google.gson.Gson;
import dto.engine.DebugStateChangeResultDTO;
import dto.engine.ExecutionResultStatisticsDTO;
import dto.engine.FullExecutionResultDTO;
import engine.core.ProgramDebugger;
import engine.exception.InsufficientCredits;
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

import static utils.ServletConstants.*;

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
        String debugAction = req.getParameter(DEBUG_ACTION_PARAM);
        DebugStateChangeResultDTO stateChange;
        try {
            switch (debugAction) {
                case DEBUG_ACTION_STEP_OVER -> {
                    stateChange = debugger.stepOver();
                    checkIfDebugEnded(stateChange, debugger, user);
                    writeDebugStateChangeResult(resp, stateChange);
                }
                case DEBUG_ACTION_STEP_BACK -> {
                    stateChange = debugger.stepBack();
                    checkIfDebugEnded(stateChange, debugger, user);
                    writeDebugStateChangeResult(resp, stateChange);
                }
                case DEBUG_ACTION_RESUME -> {
                    stateChange = debugger.resume();
                    checkIfDebugEnded(stateChange, debugger, user);
                    writeDebugStateChangeResult(resp, stateChange);
                }
                case DEBUG_ACTION_STOP -> {
                    stateChange = debugger.stop();
                    checkIfDebugEnded(stateChange, debugger, user);
                    writeDebugStateChangeResult(resp, stateChange);
                }
                default -> {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().println("Unknown debug action: " + debugAction);
                }
            }
        } catch (InsufficientCredits insufficientCredits) {
            user.setRemainingCredits(insufficientCredits.getCreditsLeft());
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
            FullExecutionResultDTO fullExecutionResult = debugger.getDebugFinishedExecutionResult();
            executionHistoryManager.addExecutionResult(
                    user.getName(),
                    debugger.getProgramName(),
                    ExecutionResultStatisticsDTO.of(fullExecutionResult));
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
