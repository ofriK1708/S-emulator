package servlets;

import com.google.gson.Gson;
import dto.server.UserDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.manager.UserManager;
import utils.ServletUtils;

import java.io.IOException;

import static utils.ServletConstants.USERNAME;

@WebServlet(name = "register", urlPatterns = {"/register"})
public class Register extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter(USERNAME);
        if (username == null || username.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Username is required.");
            return;
        }
        username = username.trim();
        UserManager userManager = ServletUtils.getUserManager(getServletContext());
        Gson gson = new Gson();
        synchronized (this) {
            if (userManager.isUserExists(username)) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                resp.getWriter().write("User with the username '" + username + "' already exists. " +
                        "Please choose a different username.");
            } else {
                UserDTO userDTO = userManager.addUser(username);
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentType("application/json");
                resp.getWriter().write(gson.toJson(userDTO));
                req.getSession().setAttribute(USERNAME, username);
            }
        }
    }
}
