package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static utils.ServletConstants.PLAIN_TEXT_CONTENT_TYPE;

@WebServlet(name = "health", urlPatterns = "/health")
public class health extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType(PLAIN_TEXT_CONTENT_TYPE);
        resp.getWriter().println("Im running fine! current date and time is: " + dateTime);
    }
}
