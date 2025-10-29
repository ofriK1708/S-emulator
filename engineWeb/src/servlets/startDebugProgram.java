package servlets;

import com.google.gson.Gson;
import dto.server.SystemResponse;
import engine.core.Engine;
import engine.core.ProgramDebugger;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.User;
import utils.ServletUtils;

import java.io.IOException;

import static utils.ServletConstants.PLAIN_TEXT_CONTENT_TYPE;

@WebServlet(name = "startDebugProgram", urlPatterns = "/debugger/start")
public class startDebugProgram extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (ServletUtils.checkAndHandleUnauthorized(req, resp, getServletContext())) {
            User user = ServletUtils.getUser(req, getServletContext());
            Gson gson = new Gson();
            if (user == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().write("Error! User is not logged in.");
                return;
            }
            ServletUtils.runAndDebugParams rdp;
            rdp = ServletUtils.getAndValidateRunAndDebugParams(req, resp);
            if (rdp == null) {
                return;
            }
            Engine engine = rdp.pm().getProgramOrFunctionEngine(rdp.programName());
            int expandLevel = rdp.expandLevel();
            try {
                ProgramDebugger debugger = engine.startDebugSession(expandLevel, rdp.arguments(),
                        user.getCurrentCredits(), rdp.architectureType());
                user.setDebugger(debugger);
                int creditsLeft = debugger.getRunningUserCredits();
                user.setRemainingCredits(creditsLeft);
                resp.setStatus(HttpServletResponse.SC_OK);

                SystemResponse response = SystemResponse.builder()
                        .isSuccess(true)
                        .message("Debug session for program " + rdp.programName() +
                                " started successfully")
                        .creditsLeft(creditsLeft)
                        .build();

                resp.getWriter().write(gson.toJson(response));
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.setContentType(PLAIN_TEXT_CONTENT_TYPE);
                String errorMessage = String.format(
                        "Error trying to start debug session for %s at expand level %d: %s",
                        rdp.programName(),
                        expandLevel,
                        e.getMessage()
                );
                resp.getWriter().write(errorMessage);
            }
        }
    }
}
