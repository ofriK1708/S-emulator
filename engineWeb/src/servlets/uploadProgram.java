package servlets;

import com.google.gson.Gson;
import engine.generated_2.SProgram;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import jakarta.xml.bind.JAXBException;
import logic.User;
import logic.file.xml.XMLHandler;
import logic.manager.ProgramManager;
import org.jetbrains.annotations.Nullable;
import utils.ServletUtils;

import java.io.IOException;
import java.util.Arrays;


@WebServlet(name = "uploadProgram", urlPatterns = "/uploadProgram")
@MultipartConfig
public class uploadProgram extends HttpServlet {
    private final static String XML_FILE_PART_NAME = "xmlFile";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = ServletUtils.getUser(req, getServletContext());
        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("Error! User is not logged in.");
            return;
        }
        SProgram sProgram = getSProgramFromRequest(req, resp);
        Gson json = new Gson();
        if (sProgram != null) {
            ProgramManager programManager = ServletUtils.getProgramManager(getServletContext());
            String programName = sProgram.getName();
            System.out.println("Received sProgram: " + sProgram);
            synchronized (this) {
                if (programManager.isProgramExists(programName)) {
                    resp.setStatus(HttpServletResponse.SC_CONFLICT);
                    String fileName = req.getPart(XML_FILE_PART_NAME).getSubmittedFileName();
                    resp.getWriter().println("Failed to upload the File \"" + fileName + "\"! " +
                            "A sProgram with the name \"" + programName + "\" already " +
                            "exists in the server, please choose a different name or file.");
                } else {
                    try {
                        // Validate sProgram by trying to create an engine - check for label not exists, etc.
                        programManager.addProgram(programName, sProgram, user);
                        resp.setStatus(HttpServletResponse.SC_OK);
                        resp.setContentType("application/json");
                        resp.getWriter().println("Program " + programName + " uploaded successfully.");
                        System.out.println("Program " + programName + " uploaded successfully.");

                    } catch (Exception e) {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        resp.getWriter().println("Failed to upload sProgram " + programName + ": " + e.getMessage());
                        System.out.println(Arrays.toString(e.getStackTrace()));
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
            resp.setContentType("text/plain");
            resp.getWriter().println("Failed to parse XML file: " + e.getMessage());
            return null;
        } catch (ServletException | IOException e) {
            resp.setContentType("text/plain");
            resp.getWriter().println("Failed to process uploaded file: " + e.getMessage());
            return null;
        }
    }

    /**
     * Validates the uploaded file to ensure it is an XML file. two checks are performed:
     * 1. Content type check: The content type of the uploaded file is checked to be "application/xml" or "text/xml".
     * 2. File extension check: The file name is checked to ensure it ends with ".xml".
     *
     * @param resp     The HttpServletResponse to set error status if validation fails.
     * @param filePart The uploaded file part to validate.
     * @throws IOException      If an I/O error occurs during validation.
     * @throws ServletException If the file is not a valid XML file.
     */
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
