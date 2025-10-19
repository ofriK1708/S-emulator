package servlets;

import com.google.gson.Gson;
import engine.core.Engine;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;

import java.io.IOException;

@WebServlet(name = "startDebugProgram", urlPatterns = "/debugger/start")
public class startDebugProgram extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Gson gson = new Gson();
        ServletUtils.runAndDebugParams runAndDebugParams;
        try {
            runAndDebugParams =
                    ServletUtils.getAndValidateRunAndDebugParams(req);
        } catch (Exception e) {
            return;
        }
        int expandLevel = runAndDebugParams.expandLevel();
        Engine engine = runAndDebugParams.pm().getProgramOrFunctionEngine(runAndDebugParams.programName());
        if (expandLevel < 0 || expandLevel > engine.getMaxExpandLevel()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Error! expand level must be between 0 to " + engine.getMaxExpandLevel());
            return;
        }
        engine.startDebugSession(expandLevel, runAndDebugParams.arguments());
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().println("Debug session for program " + runAndDebugParams.programName() +
                " started successfully");
    }
}
