package controller.test;

import controller.SystemController;
import dto.engine.ProgramDTO;
import jakarta.xml.bind.JAXBException;

public class TestingMain
{

    public static void main(String[] args)
    {
        ProgramDTO program;
        try
        {
            String xmlFilePath = "system/src/file/test/synthetic.xml";
            SystemController systemController = new SystemController();
            systemController.LoadProgramFromFile(xmlFilePath);
            program = systemController.runLoadedProgram(2);
            System.out.println("Program loaded and run successfully.");
        } catch (JAXBException e)
        {
            System.out.println("Exception: " + e.getCause().getMessage());
        }
    }
}
