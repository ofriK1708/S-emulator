//package servlets;
//
//import engine.core.ProgramEngine;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.annotation.WebServlet;
//import jakarta.servlet.http.HttpServlet;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import logic.manager.ProgramManager;
//import utils.ServletUtils;
//
//import java.io.IOException;
//
//import static utils.ServletConstants.*;
//
//@WebServlet(name = "debugAction", urlPatterns = "/debugger/action")
//public class debugAction extends HttpServlet {
//    @Override
//    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        ProgramManager pm = ServletUtils.getProgramManager(getServletContext());
//        String programName = req.getParameter(PROGRAM_NAME_PARAM);
//        String debugAction = req.getParameter(DEBUG_ACTION_PARAM);
//        if (!pm.isProgramExists(programName)) {
//            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
//            resp.getWriter().println("Program " + programName + " not found");
//        }
//        ProgramEngine currentEngine = pm.getProgramOrFunctionEngine(programName);
//        // TODO - check if we are in debug mode
//        switch (debugAction) {
//            case DEBUG_ACTION_STEP_OVER:
//                currentEngine.debugStep();
//                afterDebugForwardAction(currentEngine);
//                break;
//            case DEBUG_ACTION_STEP_BACK:
//                currentEngine.debugStepBackward();
//                break;
//            case DEBUG_ACTION_RESUME:
//                currentEngine.debugResume();
//                afterDebugForwardAction(currentEngine);
//                break;
//            case DEBUG_ACTION_STOP:
//                currentEngine.stopDebugSession();
//                break;
//            default:
//                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
//                resp.getWriter().println("Unknown debug action: " + debugAction);
//                return;
//        }
//        resp.setStatus(HttpServletResponse.SC_OK);
//        resp.getWriter().println("Debug action " + debugAction + " executed successfully for program " + programName);
//    }
//
//    private void afterDebugForwardAction(ProgramEngine engine) {
//        if (engine.isDebugFinished()) {
//            engine.finalizeDebugExecution();
//        }
//    }
//}
