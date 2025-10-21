package servlets;

import engine.core.Engine;
import engine.core.ProgramDebugger;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.User;
import utils.ServletUtils;

import java.io.IOException;

@WebServlet(name = "startDebugProgram", urlPatterns = "/debugger/start")
public class startDebugProgram extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (ServletUtils.checkAndHandleUnauthorized(req, resp, getServletContext())) {
            User user = ServletUtils.getUser(req, getServletContext());
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
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().println("Debug session for program " + rdp.programName() +
                        " started successfully");
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.setContentType("text/plain");
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
