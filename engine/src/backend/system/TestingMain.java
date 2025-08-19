package backend.system;

import backend.engine.ProgramEngine;
import backend.system.generated.*;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public class TestingMain
{
    private final static String JAXB_XML_GAME_PACKAGE_NAME = "backend.system.generated";
    public static void main(String[] args)
    {
        try
        {
            XMLHandler xmlHandler = new XMLHandler();
            String xmlFilePath = "engine/src/backend/system/resources/basic.xml";
            SProgram program = xmlHandler.unmaeshalleForm(new File(xmlFilePath).toPath());
            SystemController systemController = new SystemController();
            ProgramEngine engine = systemController.createEngine(program);
            System.out.println("-------Before execution-------");
            engine.printProgram();
            engine.run();
            System.out.println("------After execution-------");
            engine.printProgram();
        } catch (JAXBException | FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    /*private static SProgram deserializeFrom(InputStream in) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(TestingMain.JAXB_XML_GAME_PACKAGE_NAME);
        Unmarshaller u = jc.createUnmarshaller();
        return (SProgram) u.unmarshal(in);
    }*/
}
