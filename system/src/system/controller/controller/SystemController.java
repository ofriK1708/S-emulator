package system.controller.controller;

import dto.engine.ExecutionResultDTO;
import dto.engine.ExecutionStatisticsDTO;
import dto.engine.ProgramDTO;
import engine.core.ProgramEngine;
import engine.exception.LabelNotExist;
import engine.generated.SProgram;
import jakarta.xml.bind.JAXBException;
import system.file.file.processing.XMLHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public class SystemController
{
    private final XMLHandler xmlHandler;
    private ProgramEngine engine;
    int maxExpandLevel = 0;

    public SystemController()
    {
        try
        {
            this.xmlHandler = new XMLHandler();
        } catch (JAXBException e)
        {
            throw new RuntimeException("Failed to initialize XMLHandler " + e.getMessage());
        }
    }

    public Set<String> getProgramArgsNames()
    {
        if (engine == null)
        {
            throw new IllegalStateException("Program has not been set");
        }
        return engine.getProgramArgsNames();
    }

    private void createEngine(SProgram program) throws LabelNotExist
    {
        engine = new ProgramEngine(program);
        maxExpandLevel = engine.getMaxExpandLevel();
    }

    public int getMaxExpandLevel()
    {
        if (engine == null)
        {
            throw new IllegalStateException("Program has not been set");
        }
        return maxExpandLevel;
    }

    public void LoadProgramFromFile(Path xmlFilePath) throws LabelNotExist, JAXBException, IOException
    {
        if (!xmlFilePath.getFileName().toString().endsWith(".xml"))
        {
            throw new IllegalArgumentException("File must be an XML file");
        }
        validateRegularAndReadableFile(xmlFilePath);
        SProgram program = xmlHandler.unmarshallFile(xmlFilePath);
        createEngine(program);

    }

    private void validateRegularAndReadableFile(Path filePath) throws IOException
    {
        if (!Files.isRegularFile(filePath))
        {
            throw new IOException("File does not exist or is not a regular file");
        }
        if (!Files.isReadable(filePath))
        {
            throw new IOException("File is not readable");
        }
    }



    public ExecutionResultDTO runLoadedProgram(int expandLevel, List<Integer> arguments)
    {
        if (engine == null)
        {
            throw new IllegalStateException("Program has not been set");
        }
        if (expandLevel < 0 || expandLevel > maxExpandLevel)
        {
            throw new IllegalArgumentException("Expand level must be between 0 and " + maxExpandLevel);
        }
        if (arguments == null || arguments.isEmpty())
        {
            throw new IllegalArgumentException("Arguments list must not be null or empty");
        }
        if (arguments.stream()
                .anyMatch(arg -> arg < 0))
        {
            throw new IllegalArgumentException("All arguments must be non-negative integers");
        }
        engine.run(expandLevel, arguments);
        return engine.toExecutionResultDTO(expandLevel);
    }


    public ProgramDTO getProgramByExpandLevel(int expandLevel)
    {
        if (engine == null)
        {
            throw new IllegalStateException("Program has not been set");
        }
        if (expandLevel < 0 || expandLevel > maxExpandLevel)
        {
            throw new IllegalArgumentException("Expand level must be between 0 and " + maxExpandLevel);
        }
        return engine.toDTO(expandLevel);
    }

    public ProgramDTO getBasicProgram()
    {
        return getProgramByExpandLevel(0);
    }

    public List<ExecutionStatisticsDTO> getAllExecutionStatistics()
    {
        if (engine == null)
        {
            throw new IllegalStateException("Program has not been set");
        }
        return engine.getAllExecutionStatistics();
    }

    public void saveProgramState(Path filePath) throws IOException
    {
        if (engine == null)
        {
            throw new IllegalStateException("Program has not been set");
        }
        engine.saveState(filePath);
    }

    public void loadProgramState(Path filePath) throws IOException, ClassNotFoundException
    {
        if (!Files.isRegularFile(filePath))
        {
            throw new IllegalArgumentException("File does not exist or is not a regular file");
        }
        validateRegularAndReadableFile(filePath);
        engine = ProgramEngine.loadState(filePath);
        maxExpandLevel = engine.getMaxExpandLevel();
    }
}