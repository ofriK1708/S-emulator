package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.manager.ExecutionHistoryManager;
import logic.manager.UserManager;
import utils.ServletUtils;

import java.io.IOException;

import static utils.ServletConstants.USERNAME_PARAM;

@WebServlet(name = "registerUser", urlPatterns = "/users/register")
public class Register extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter(USERNAME_PARAM);
        if (username == null || username.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Username is required.");
            return;
        }
        username = username.trim();
        UserManager userManager = ServletUtils.getUserManager(getServletContext());
        synchronized (this) {
            if (userManager.isUserExists(username)) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                resp.getWriter().println("User with the username '" + username + "' already exists. " +
                        "Please choose a different username.");
            } else {
                userManager.addUser(username);
                ExecutionHistoryManager executionHistoryManager =
                        ServletUtils.getExecutionHistoryManager(getServletContext());
                executionHistoryManager.initUserHistory(username);
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().println("user " + username + " has been successfully registered.");
                req.getSession().setAttribute(USERNAME_PARAM, username);
            }
        }
    }
}
