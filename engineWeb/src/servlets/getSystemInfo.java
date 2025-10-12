package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.manager.ProgramManager;
import utils.ServletUtils;

import java.io.IOException;

import static utils.ServletConstants.*;

@WebServlet(name = "SystemInfo", urlPatterns = "/systemInfo")
public class getSystemInfo extends HttpServlet {
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html;charset=utf-8");
        resp.getWriter().println(getAllSystemInfoOptionsNames());
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String infoToGet = req.getParameter(INFO_PARAM);
        resp.setContentType("text/html;charset=utf-8");
        ProgramManager pm = ServletUtils.getProgramManager(getServletContext());
        switch (infoToGet) {
            case PROGRAMS_NAMES_INFO:
                resp.getWriter().println(pm.getProgramNames());
                resp.setStatus(HttpServletResponse.SC_OK);
                break;
            case FUNCTIONS_NAMES_INFO:
                resp.getWriter().println(pm.getFunctionNames());
                resp.setStatus(HttpServletResponse.SC_OK);
                break;
            case ALL_NAMES_INFO:
                resp.getWriter().println("Programs: " + pm.getProgramNames() + "\nFunctions: " +
                        pm.getFunctionNames());
                resp.setStatus(HttpServletResponse.SC_OK);
                break;
            default:
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "item parameter is missing or invalid");
                break;
        }
    }
}
