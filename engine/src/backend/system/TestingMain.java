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
            String xmlFilePath = "engine/src/backend/system/resources/synthetic.xml";
            SProgram program = xmlHandler.unmarshallForm(Path.of(xmlFilePath));
            SystemController systemController = new SystemController();
            ProgramEngine engine = systemController.createEngine(program);
            System.out.println("-------Before executing-------");
            engine.printProgramToFile("Before executing");
            System.out.println("------After executing -------");
            engine.run(0);
            engine.printProgramToFile("After executing");
            System.out.println("-------After expanding 1 time and executing-------");
            engine.expand(1);
            engine.printProgramToFile("After expanding 1 time and executing");
            System.out.println("-------After expanding 2 times and executing-------");
            engine.expand(2);
            engine.printProgramToFile("After expanding 2 times and executing");
            System.out.println("-------After expanding 3 times and executing-------");
            engine.expand(3);
            engine.printProgramToFile("After expanding 3 times and executing");
        } catch (JAXBException | IOException e)
        {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    /*private static SProgram deserializeFrom(InputStream in) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(TestingMain.JAXB_XML_GAME_PACKAGE_NAME);
        Unmarshaller u = jc.createUnmarshaller();
        return (SProgram) u.unmarshal(in);
    }*/
}
