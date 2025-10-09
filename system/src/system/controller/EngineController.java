package system.controller;

import engine.generated_2.SProgram;
import jakarta.xml.bind.JAXBException;
import org.jetbrains.annotations.NotNull;
import system.file.processing.XMLHandler;

import java.io.IOException;
import java.nio.file.Path;

import static system.utils.systemUtils.validateFile;

public abstract class EngineController implements EngineProgramManager
{
    private final @NotNull XMLHandler xmlHandler;

    protected EngineController() {
        try {
            this.xmlHandler = new XMLHandler();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize XMLHandler " + e.getMessage());
        }
    }

    protected @NotNull SProgram getSProgramFromFile(@NotNull Path xmlFilePath)
            throws JAXBException, IOException
    {
        if (!xmlFilePath.getFileName().toString().endsWith(".xml"))
        {
            throw new IllegalArgumentException("File must be an XML file");
        }
        validateFile(xmlFilePath);
        return xmlHandler.unmarshallFile(xmlFilePath);
    }
}
