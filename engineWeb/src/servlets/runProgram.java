package servlets;

import com.google.gson.Gson;
import dto.engine.ExecutionResult;
import engine.core.ProgramEngine;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;

import java.io.IOException;
import java.util.Map;

@WebServlet(name = "runProgram", urlPatterns = "/runProgram")
public class runProgram extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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
        ProgramEngine currentEngine = runAndDebugParams.pm().getProgramOrFunctionEngine(programName);
        if (expandLevel < 0 || expandLevel > currentEngine.getMaxExpandLevel()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Error! expand level must be between 0 to " + currentEngine.getMaxExpandLevel());
            return;
        }
        Map<String, Integer> args = runAndDebugParams.arguments();
        ExecutionResult executionResult = currentEngine.run(expandLevel, args);
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(gson.toJson(executionResult));
    }
}
