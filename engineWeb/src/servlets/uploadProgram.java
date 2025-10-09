package servlets;

import com.google.gson.Gson;
import engine.core.ProgramEngine;
import engine.generated_2.SProgram;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.ProgramManager;
import utils.ServletUtils;

import java.io.IOException;

@WebServlet(name = "uploadProgram", urlPatterns = "/uploadProgram")
public class uploadProgram extends HttpServlet {
    Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ProgramManager programManager = ServletUtils.getProgramManager(getServletContext());
        SProgram program = gson.fromJson(req.getReader(), SProgram.class);
        String programName = program.getName();
        System.out.println("Received program: " + program);
        synchronized (this) {
            if (programManager.isProgramExists(programName)) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                resp.getWriter().println("Program with name " + programName + " already exists");
            } else {
                // Validate program by trying to create an engine - check for label not exists, etc.
                try {
                    ProgramEngine engine = new ProgramEngine(program);
                    // TODO - continue validation - give the program all the function in the system
                    //  and check for conflicts
                    programManager.addProgram(programName, engine);
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().println("Program " + programName + " uploaded successfully");

                } catch (Exception e) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().println("Failed to upload program " + programName + ": " + e.getMessage());
                }

            }
        }
    }
}
