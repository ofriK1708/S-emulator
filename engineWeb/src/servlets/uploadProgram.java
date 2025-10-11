package servlets;

import engine.core.ProgramEngine;
import engine.generated_2.SProgram;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import jakarta.xml.bind.JAXBException;
import logic.file.xml.XMLHandler;
import logic.manager.ProgramManager;
import org.jetbrains.annotations.Nullable;
import utils.ServletUtils;

import java.io.IOException;


@WebServlet(name = "uploadProgram", urlPatterns = "/uploadProgram")
@MultipartConfig
public class uploadProgram extends HttpServlet {
    private final static String XML_FILE_PART_NAME = "xmlFile";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        SProgram program = getSProgramFromRequest(req, resp);
        if (program != null) {
            ProgramManager programManager = ServletUtils.getProgramManager(getServletContext());
            String programName = program.getName();
            System.out.println("Received program: " + program);
            synchronized (this) {
                if (programManager.isProgramExists(programName)) {
                    resp.setStatus(HttpServletResponse.SC_CONFLICT);
                    String fileName = req.getPart(XML_FILE_PART_NAME).getSubmittedFileName();
                    resp.getWriter().println("Failed to upload the File \"" + fileName + "\"! " +
                            "A program with the name \"" + programName + "\" already " +
                            "exists in the system, please choose a different name or file.");
                } else {
                    // Validate program by trying to create an engine - check for label not exists, etc.
                    try {
                        ProgramEngine engine = new ProgramEngine(program);
                        engine.finishInitProgram(programManager.getFunctionsAndPrograms());
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

    private @Nullable SProgram getSProgramFromRequest(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        try {
            XMLHandler xmlHandler = new XMLHandler();
            Part xmlFilePart = req.getPart(XML_FILE_PART_NAME);
            validateFile(resp, xmlFilePart);
            return xmlHandler.unmarshallFile(xmlFilePart.getInputStream());
        } catch (JAXBException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().println("Failed to parse XML file: " + e.getMessage());
            return null;
        } catch (ServletException | IOException e) {
            resp.getWriter().println("Failed to process uploaded file: " + e.getMessage());
            return null;
        }
    }

    private void validateFile(HttpServletResponse resp, Part filePart)
            throws IOException, ServletException {

        // 1. Check content type
        String contentType = filePart.getContentType();
        if (!contentType.equals("application/xml") && !contentType.equals("text/xml")) {
            resp.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            throw new ServletException("Uploaded file is not an XML file (content type check)");
        }

        // 2. Check file extension
        String fileName = filePart.getSubmittedFileName();
        if (fileName == null || !fileName.toLowerCase().endsWith(".xml")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            throw new ServletException("Uploaded file is not an XML file (extension check)");
        }
    }

}
