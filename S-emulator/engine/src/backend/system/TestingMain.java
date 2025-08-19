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
            InputStream inputStream = new FileInputStream("engine/src/backend/system/resources/error-1.xml");
            SProgram program = deserializeFrom(inputStream);
            ProgramEngine engine = new ProgramEngine(program);
        } catch (JAXBException | FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    private static SProgram deserializeFrom(InputStream in) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(TestingMain.JAXB_XML_GAME_PACKAGE_NAME);
        Unmarshaller u = jc.createUnmarshaller();
        return (SProgram) u.unmarshal(in);
    }
}
