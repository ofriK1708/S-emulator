package servlets;

import com.google.gson.Gson;
import dto.engine.ExecutionResultDTO;
import engine.core.Engine;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.User;
import utils.ServletUtils;

import java.io.IOException;
import java.util.Map;

@WebServlet(name = "runProgram", urlPatterns = "/runProgram")
public class runProgram extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = ServletUtils.getUsername(req, getServletContext());
        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("Error! User is not logged in.");
            return;
        }
        Gson gson = new Gson();
        resp.setContentType("application/json");
        ServletUtils.runAndDebugParams runAndDebugParams;
        try {
            runAndDebugParams = ServletUtils.getAndValidateRunAndDebugParams(req, resp);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Error! " + e.getMessage());
            return;
        }
        String programName = runAndDebugParams.programName();
        int expandLevel = runAndDebugParams.expandLevel();
        Engine currentEngine = runAndDebugParams.pm().getProgramOrFunctionEngine(programName);
        if (expandLevel < 0 || expandLevel > currentEngine.getMaxExpandLevel()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Error! expand level must be between 0 to " + currentEngine.getMaxExpandLevel());
            return;
        }
        Map<String, Integer> args = runAndDebugParams.arguments();
        ExecutionResultDTO executionResultDTO = currentEngine.run(expandLevel, args);
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(gson.toJson(executionResultDTO));
    }
}
