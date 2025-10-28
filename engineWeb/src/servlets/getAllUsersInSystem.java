package servlets;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.manager.UserManager;
import utils.ServletUtils;

import java.io.IOException;

import static utils.ServletConstants.JSON_CONTENT_TYPE;

@WebServlet(name = "getAllUsersInSystem", value = "/getAllUsersInSystem")
public class getAllUsersInSystem extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Gson gson = new Gson();
        UserManager userManager = ServletUtils.getUserManager(getServletContext());
        resp.setContentType(JSON_CONTENT_TYPE);
        resp.getWriter().write(gson.toJson(userManager.getAllUsersDTO()));
    }
}
