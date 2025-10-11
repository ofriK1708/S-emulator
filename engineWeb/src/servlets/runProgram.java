package servlets;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import engine.core.ProgramEngine;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

@WebServlet(name = "runProgram", urlPatterns = "/runProgram")
public class runProgram extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Gson gson = new Gson();
        ServletUtils.runAndExpandParams runAndExpandParams;
        try {
            runAndExpandParams = ServletUtils.validateAndGetParams(req, resp);
        } catch (Exception ignored) {
            return;
        }
        String programName = runAndExpandParams.programName();
        int expandLevel = runAndExpandParams.expandLevel();
        ProgramEngine currentEngine = runAndExpandParams.pm().getProgramOrFunctionEngine(programName);
        if (expandLevel < 0 || expandLevel > currentEngine.getMaxExpandLevel()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Error! expand level must be between 0 to " + currentEngine.getMaxExpandLevel());
            return;
        }
        Type type = new TypeToken<Map<String, Integer>>() {
        }.getType();
        Map<String, Integer> args = gson.fromJson(req.getReader(), type);
        currentEngine.run(args, expandLevel);
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().println("Program " + programName + " executed successfully");
    }
}
