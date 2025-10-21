package utils;

import dto.engine.FullExecutionResultDTO;
import engine.core.Engine;
import engine.exception.FunctionAlreadyExist;
import engine.exception.FunctionNotFound;
import engine.exception.LabelNotExist;
import engine.generated_2.SProgram;
import engine.utils.ArchitectureType;
import jakarta.xml.bind.JAXBException;
import logic.file.xml.XMLHandler;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class test {
    public static void main(String[] args) throws JAXBException, FileNotFoundException {
        String testFilePath = "system/src/system/file/test2/divide.xml";
        try (InputStream inputStream = new FileInputStream(testFilePath)) {
            XMLHandler xmlHandler = new XMLHandler();
            SProgram sProgram = xmlHandler.unmarshallFile(inputStream);
            Engine engine = Engine.createMainProgramEngine(sProgram, Map.of(), "testUser");
            FullExecutionResultDTO resultDTO = engine.run(0, Map.of("x1", 4, "x2", 2),
                    Integer.MAX_VALUE, ArchitectureType.ARCHITECTURE_IV);
            int cycles = resultDTO.cycleCount();
            System.out.println("Program executed in " + cycles + " cycles.");
            resultDTO = engine.run(0, Map.of("x1", 4, "x2", 2),
                    Integer.MAX_VALUE, ArchitectureType.ARCHITECTURE_IV);
            cycles = resultDTO.cycleCount();
            System.out.println("Program executed in " + cycles + " cycles.");
            resultDTO = engine.run(0, Map.of("x1", 4, "x2", 2),
                    Integer.MAX_VALUE, ArchitectureType.ARCHITECTURE_IV);
            cycles = resultDTO.cycleCount();
            System.out.println("Program executed in " + cycles + " cycles.");
        } catch (IOException | LabelNotExist | FunctionNotFound | FunctionAlreadyExist ex) {
            throw new RuntimeException(ex);
        }
    }
}
