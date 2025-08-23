package backend.system;

import backend.engine.ProgramEngine;
import backend.system.generated.SProgram;
import jakarta.xml.bind.JAXBException;

import java.io.IOException;
import java.nio.file.Path;

public class TestingMain
{
    public static void main(String[] args)
    {
        try
        {
            XMLHandler xmlHandler = new XMLHandler();
            String xmlFilePath = "engine/src/backend/system/resources/basic.xml";
            SProgram program = xmlHandler.unmarshallForm(Path.of(xmlFilePath));
            SystemController systemController = new SystemController();
            ProgramEngine engine = systemController.createEngine(program);
            System.out.println("-------Before executing-------");
            engine.printProgramToFile(0, "Before executing");
            System.out.println("-------After expanding 3 times and executing-------");
            engine.run(3);
            engine.printProgramToFile(3, "After expanding 3 times and executing");
            System.out.println("------After executing -------");
            engine.run(0);
            engine.printProgramToFile(0, "After executing");
            System.out.println("-------After expanding 1 time and executing-------");
            engine.run(1);
            engine.printProgramToFile(1, "After expanding 1 time and executing");
            System.out.println("-------After expanding 2 times and executing-------");
            engine.run(2);
            engine.printProgramToFile(2, "After expanding 2 times and executing");
        } catch (JAXBException | IOException e)
        {
            System.out.println("Exception: " + e.getCause().getMessage());
        }
    }

    /*private static SProgram deserializeFrom(InputStream in) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(TestingMain.JAXB_XML_GAME_PACKAGE_NAME);
        Unmarshaller u = jc.createUnmarshaller();
        return (SProgram) u.unmarshal(in);
    }*/
}
