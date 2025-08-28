package system.controller.controller;

import dto.engine.ExecutionStatisticsDTO;
import dto.engine.ProgramDTO;
import engine.core.ProgramEngine;
import engine.exception.LabelNotExist;
import engine.generated.SProgram;
import jakarta.xml.bind.JAXBException;
import system.file.file.processing.XMLHandler;

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
        return engine.extractArguments().keySet();
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

    public void LoadProgramFromFile(String xmlFilePath) throws LabelNotExist
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
    }

    public ProgramDTO runLoadedProgram(int expandLevel, List<Integer> arguments)
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
        return getProgramByExpandLevel(expandLevel);
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

    public List<ExecutionStatisticsDTO> getExecutionStatistics()
    {
        if (engine == null)
        {
            throw new IllegalStateException("Program has not been set");
        }
        return engine.getExecutionStatisticsDTOList();
    }
}