package system;

import core.ProgramEngine;
import file.processing.XMLHandler;
import generated.SProgram;
import jakarta.xml.bind.JAXBException;

import java.nio.file.Path;

public class SystemController
{
    private final XMLHandler xmlHandler;
    private ProgramEngine engine;

    public SystemController() throws JAXBException
    {
        try
        {
            this.xmlHandler = new XMLHandler();
        } catch (JAXBException e)
        {
            throw new RuntimeException("Failed to initialize XMLHandler " + e.getMessage());
        }
    }

    private void createEngine(SProgram program)
    {
        engine = new ProgramEngine(program);
    }

    public void LoadProgramFromFile(String xmlFilePath)
    {
        if (!xmlFilePath.endsWith(".xml"))
        {
            throw new IllegalArgumentException("File must be an XML file");
        }
        try
        {
            SProgram program = xmlHandler.unmarshallFile(Path.of(xmlFilePath));
            createEngine(program);
        } catch (JAXBException | java.io.IOException e)
        {
            throw new RuntimeException("Failed to load S-program from file: " + xmlFilePath + e.getMessage());
        }
        // TODO - pass the UI dto about the max expandedLevel and what else he needs
    }

    public void runLoadedProgram(int expandLevel)
    {
        if (engine == null)
        {
            throw new IllegalStateException("Program engine has not been initialized");
        }
        engine.run(expandLevel);
        engine.printProgram(expandLevel);
    }

    public void printProgram()
    {
        if (engine == null)
        {
            throw new IllegalStateException("Program engine has not been initialized");
        }
        engine.printProgram(0);
    }
}