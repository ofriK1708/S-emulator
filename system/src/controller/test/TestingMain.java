package controller.test;

import controller.SystemController;
import dto.engine.ProgramDTO;
import jakarta.xml.bind.JAXBException;

import java.util.List;

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
            List<Integer> arguments = List.of(1, 2);
            program = systemController.runLoadedProgram(2, arguments);
            System.out.println("Program loaded and run successfully.");
        } catch (JAXBException e)
        {
            System.out.println("Exception: " + e.getCause().getMessage());
        }
    }
}
