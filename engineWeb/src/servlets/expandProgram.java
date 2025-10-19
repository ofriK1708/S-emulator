package servlets;

import com.google.gson.Gson;
import dto.engine.ProgramDTO;
import engine.core.Engine;
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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!ServletUtils.isUserLoggedIn(req, getServletContext())) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("Error! User is not logged in.");
            return;
        }
        Gson gson = new Gson();
        resp.setContentType("application/json;charset=UTF-8");
        ServletUtils.expandParams expandParams;
        try {
            expandParams = ServletUtils.getAndValidateExpandParams(req, resp);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Error! " + e.getMessage());
            return;
        }

        String programName = expandParams.programName();
        int expandLevel = expandParams.expandLevel();
        ProgramManager pm = expandParams.pm();
        Engine currentEngine = pm.getProgramOrFunctionEngine(programName);
        if (expandLevel < 0 || expandLevel > currentEngine.getMaxExpandLevel()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Error! expand level must be between 0 to " + currentEngine.getMaxExpandLevel());
            return;
        }

        ProgramDTO programDTO = currentEngine.getProgramByExpandLevelDTO(expandLevel);
        String json = gson.toJson(programDTO);
        resp.getWriter().write(json);
    }
}
