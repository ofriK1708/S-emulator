package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.manager.ProgramManager;
import utils.ServletUtils;

import java.io.IOException;

@WebServlet(name = "SystemInfo", urlPatterns = "/SystemInfo")
public class getSystemInfo extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String itemToGet = req.getParameter("item");
        resp.setContentType("text/html;charset=utf-8");
        ProgramManager pm = ServletUtils.getProgramManager(getServletContext());
        switch (itemToGet) {
            case "programsNames":
                resp.getWriter().println(pm.getProgramNames());
                resp.setStatus(HttpServletResponse.SC_OK);
                break;
            case "functionsNames":
                resp.getWriter().println(pm.getFunctionNames());
                resp.setStatus(HttpServletResponse.SC_OK);
                break;
            case "allNames":
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
