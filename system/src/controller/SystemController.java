package controller;

import core.ProgramEngine;
import dto.engine.ExecutionStatisticsDTO;
import dto.engine.ProgramDTO;
import file.processing.XMLHandler;
import generated.SProgram;
import jakarta.xml.bind.JAXBException;

import java.nio.file.Path;
import java.util.List;

public class SystemController
{
    private final XMLHandler xmlHandler;
    private ProgramEngine engine;
    int maxExpandLevel = 0;

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
        maxExpandLevel = engine.getMaxExpandLevel();
    }

    public int getMaxExpandLevel()
    {
        return maxExpandLevel;
    }

    public int LoadProgramFromFile(String xmlFilePath)
    {
        if (!xmlFilePath.endsWith(".xml"))
        {
            throw new IllegalArgumentException("File must be an XML file");
        }
        try
        {
            SProgram program = xmlHandler.unmarshallFile(Path.of(xmlFilePath));
            createEngine(program);
        } catch (Exception e)
        {
            throw new RuntimeException("Failed to load S-program from file: " + xmlFilePath + e.getMessage());
        }
        return maxExpandLevel;
    }

    public ProgramDTO runLoadedProgram(int expandLevel, List<Integer> arguments)
    {
        if (engine == null)
        {
            throw new IllegalStateException("Program engine has not been initialized");
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
        return getProgramByExpandLevel(expandLevel);
    }

    public ProgramDTO getProgramByExpandLevel(int expandLevel)
    {
        if (engine == null)
        {
            throw new IllegalStateException("Program engine has not been initialized");
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

    public List<ExecutionStatisticsDTO> getExecutionStatistics()
    {
        if (engine == null)
        {
            throw new IllegalStateException("Program engine has not been initialized");
        }
        return engine.getExecutionStatisticsDTOList();
    }
}