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
            String xmlFilePath = "engine/src/backend/system/resources/successor.xml";
            SProgram program = xmlHandler.unmarshallForm(Path.of(xmlFilePath));
            SystemController systemController = new SystemController();
            ProgramEngine engine = systemController.createEngine(program);
            System.out.println("-------Before execution-------");
            engine.printProgram();
            engine.run();
            System.out.println("------After execution-------");
            engine.printProgram();
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
