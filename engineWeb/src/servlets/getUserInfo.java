package servlets;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.manager.UserManager;
import utils.ServletConstants;
import utils.ServletUtils;

import java.io.IOException;

import static utils.ServletConstants.*;

@WebServlet(name = "getUserInfoServlet", urlPatterns = "/users/info")
public class getUserInfo extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Gson gson = new Gson();
        String username = req.getParameter(USERNAME_PARAM);
        UserManager userManager = ServletUtils.getUserManager(getServletContext());
        if (userManager.isUserExists(username)) {
            String infoToGet = req.getParameter(INFO_PARAM);
            if (infoToGet == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "missing info parameter, available options are: " +
                        ServletConstants.getAllUsersOptionsNames() + ".");
                return;
            }
            resp.setContentType("application/json");
            switch (infoToGet) {
                case ALL_USERS_INFO -> resp.getWriter().println(gson.toJson(userManager.getAllUsersDTO()));
                case USER_STATISTICS_INFO ->
                        resp.getWriter().println(gson.toJson(userManager.getUserStatisticsDTO(username)));
                default -> {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().println("unrecognised info parameter, available options are: " +
                            ServletConstants.getAllUsersOptionsNames() + ".");
                }
            }
        }
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("text/plain;charset=utf-8");
        resp.getWriter().println(ServletConstants.getAllUsersOptionsNames());
    }
}
