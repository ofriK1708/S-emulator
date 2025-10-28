package servlets;

import com.google.gson.Gson;
import dto.server.UpdateUserInfoBody;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.manager.UserManager;
import utils.ServletUtils;

import java.io.IOException;

import static utils.ServletConstants.PLAIN_TEXT_CONTENT_TYPE;

@WebServlet(name = "updateUserInfo", urlPatterns = {"/updateUserInfo"})
public class updateUserInfo extends HttpServlet {
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Gson gson = new Gson();
        resp.setContentType(PLAIN_TEXT_CONTENT_TYPE);
        UserManager uManger = ServletUtils.getUserManager(getServletContext());
        UpdateUserInfoBody updateUserInfoBody = gson.fromJson(req.getReader(), UpdateUserInfoBody.class);
        if (isInvalidRequest(updateUserInfoBody, uManger, resp)) {
            return;
        }
        String username = updateUserInfoBody.username();
        String infoToUpdate = updateUserInfoBody.infoToUpdate();
        String newValue = updateUserInfoBody.newValue();
        uManger.updateUserInfo(username, infoToUpdate, newValue);


        resp.getWriter().println("Successfully updated " + infoToUpdate + " for user " + username + "." + " New " +
                "value: " + newValue);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    private boolean isInvalidRequest(UpdateUserInfoBody updateUserInfoBody, UserManager uManger,
                                     HttpServletResponse resp) throws IOException {
        if (updateUserInfoBody == null) {
            resp.getWriter().println("Invalid request body. updateUserInfoBody is null.");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return true;
        }
        String username = updateUserInfoBody.username();
        if (!uManger.isUserExists(username)) {
            resp.getWriter().println("User " + username + " does not exist.");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return true;
        }
        String infoToUpdate = updateUserInfoBody.infoToUpdate();
        if (infoToUpdate == null || infoToUpdate.isEmpty()) {
            resp.getWriter().println("No information provided to update for user " + username + ".");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return true;
        }
        return false;
    }

    // 2. Override the main service() method to route PATCH requests
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String method = req.getMethod();

        if (method.equalsIgnoreCase("PATCH")) {
            this.doPatch(req, resp);
        } else {
            super.service(req, resp);
        }
    }
}
