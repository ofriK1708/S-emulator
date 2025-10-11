package servlets;

import com.google.gson.Gson;
import dto.engine.ProgramDTO;
import engine.core.ProgramEngine;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.manager.ProgramManager;
import utils.ServletUtils;

import java.io.IOException;

@WebServlet(name = "expandProgram", urlPatterns = "/expandProgram")
public class expandProgram extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Gson gson = new Gson();
        resp.setContentType("application/json;charset=UTF-8");
        ServletUtils.runAndExpandParams runAndExpandParams;
        try {
            runAndExpandParams = ServletUtils.validateAndGetParams(req, resp);
        } catch (Exception ignored) {
            return;
        }

        String programName = runAndExpandParams.programName();
        int expandLevel = runAndExpandParams.expandLevel();
        ProgramManager pm = runAndExpandParams.pm();

        ProgramEngine currentEngine = pm.getProgramOrFunctionEngine(programName);
        ProgramDTO programDTO = currentEngine.toDTO(expandLevel);
        String json = gson.toJson(programDTO);
        resp.getWriter().write(json);
    }
}
