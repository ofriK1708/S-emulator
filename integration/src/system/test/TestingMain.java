package system.test;
import jakarta.xml.bind.JAXBException;
import system.SystemController;

public class TestingMain
{
    public static void main(String[] args)
    {
        try
        {
            String xmlFilePath = "integration/src/file/test/basic.xml";
            SystemController systemController = new SystemController();
            systemController.LoadProgramFromFile(xmlFilePath);
            System.out.println("-------Before executing-------");
            systemController.printProgram();
            System.out.println("------After executing -------");
            systemController.runLoadedProgram(0);
            System.out.println("-------After expanding 1 time and executing-------");
            systemController.runLoadedProgram(1);
            System.out.println("-------After expanding 2 times and executing-------");
            systemController.runLoadedProgram(2);
            System.out.println("-------After expanding 3 times and executing-------");
            systemController.runLoadedProgram(3);
        } catch (JAXBException e)
        {
            System.out.println("Exception: " + e.getCause().getMessage());
        }
    }
}
