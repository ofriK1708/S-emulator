package servlets;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.manager.UserManager;
import utils.ServletConstants;
import utils.ServletUtils;

import java.io.IOException;

import static utils.ServletConstants.*;

@WebServlet(name = "getUserStatistics", urlPatterns = "/user/ExecutionStatistics")
public class getUserExecutionStatistics extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Gson gson = new Gson();
        String username = req.getParameter(USERNAME_PARAM);
        UserManager userManager = ServletUtils.getUserManager(getServletContext());
        if (userManager.isUserExists(username)) {
            resp.setContentType(JSON_CONTENT_TYPE);
            resp.getWriter().println(gson.toJson(userManager.getUserStatisticsDTO(username)));
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType(PLAIN_TEXT_CONTENT_TYPE);
            resp.getWriter().println("User " + username + " does not exist!");
        }
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType(PLAIN_TEXT_CONTENT_TYPE);
        resp.getWriter().println(ServletConstants.getAllUsersOptionsNames());
    }
}
