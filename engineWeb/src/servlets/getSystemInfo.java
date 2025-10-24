package servlets;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.manager.ProgramManager;
import logic.manager.UserManager;
import utils.ServletUtils;

import java.io.IOException;

import static utils.ServletConstants.*;

@WebServlet(name = "SystemInfo", urlPatterns = "/systemInfo")
public class getSystemInfo extends HttpServlet {
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html;charset=utf-8");
        resp.getWriter().println(getAllSystemInfoOptionsNames());
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (ServletUtils.checkAndHandleUnauthorized(req, resp, getServletContext())) {
            String infoToGet = req.getParameter(INFO_PARAM);
            if (infoToGet == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "item parameter is missing or invalid, available options are: " +
                                getAllSystemInfoOptionsNames() + ".");
                return;
            }
            Gson gson = new Gson();
            resp.setContentType("application/json");
            ProgramManager pm = ServletUtils.getProgramManager(getServletContext());
            switch (infoToGet) {
                case PROGRAMS_METADATA_INFO -> {
                    resp.getWriter().write(gson.toJson(pm.getProgramsMetadata()));
                    resp.setStatus(HttpServletResponse.SC_OK);
                }
                case FUNCTIONS_METADATA_INFO -> {
                    resp.getWriter().write(gson.toJson(pm.getFunctionsMetadata()));
                    resp.setStatus(HttpServletResponse.SC_OK);
                }
                case PROGRAMS_NAMES_INFO -> {
                    resp.getWriter().write(gson.toJson(pm.getProgramNames()));
                    resp.setStatus(HttpServletResponse.SC_OK);
                }
                case FUNCTIONS_NAMES_INFO -> {
                    resp.getWriter().write(gson.toJson(pm.getFunctionNames()));
                    resp.setStatus(HttpServletResponse.SC_OK);
                }
                case PROGRAMS_AND_FUNCTIONS_METADATA -> {
                    resp.getWriter().write(gson.toJson(pm.getProgramsAndFunctionsMetadata()));
                    resp.setStatus(HttpServletResponse.SC_OK);
                }
                case ALL_USERS_INFO -> {
                    UserManager userManager = ServletUtils.getUserManager(getServletContext());
                    resp.setContentType("application/json");
                    resp.getWriter().write(gson.toJson(userManager.getAllUsersDTO()));
                }
                default -> resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "item parameter is missing or invalid");
            }
        }
    }
}
