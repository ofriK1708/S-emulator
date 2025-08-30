package system.controller.controller;

import dto.engine.ExecutionResultDTO;
import dto.engine.ExecutionStatisticsDTO;
import dto.engine.ProgramDTO;
import engine.core.ProgramEngine;
import engine.exception.LabelNotExist;
import engine.generated.SProgram;
import jakarta.xml.bind.JAXBException;
import system.file.file.processing.XMLHandler;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    public String saveState(String directoryPath) throws IOException
    {
        if (engine == null)
        {
            throw new IllegalStateException("No program loaded to save");
        }

        // יצירת תיקיה אם לא קיימת
        File directory = new File(directoryPath);
        if (!directory.exists())
        {
            directory.mkdirs();
        }

        // יצירת שם קובץ עם תאריך ושעה
        String programName = engine.getProgramName(); // נדרש להוסיף פונקציה זו ב-Engine
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String fileName = "bonus_save_" + programName + "_" + timestamp + ".ser";
        String fullPath = Paths.get(directoryPath, fileName).toString();

        // שמירת המצב המלא
        StateData stateData = new StateData(engine, maxExpandLevel);

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fullPath)))
        {
            oos.writeObject(stateData);
        }
        catch (IOException e)
        {
            throw new IOException("Failed to save state: " + e.getMessage(), e);
        }

        return fullPath;
    }
    public void loadState(String filePath) throws IOException, ClassNotFoundException
    {
        File file = new File(filePath);
        if (!file.exists())
        {
            throw new IOException("State file does not exist: " + filePath);
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath)))
        {
            StateData stateData = (StateData) ois.readObject();
            this.engine = stateData.getEngine();
            this.maxExpandLevel = stateData.getMaxExpandLevel();
        }
        catch (IOException e)
        {
            throw new IOException("Failed to load state: " + e.getMessage(), e);
        }
        catch (ClassNotFoundException e)
        {
            throw new ClassNotFoundException("Invalid state file format: " + e.getMessage(), e);
        }
    }
    private static class StateData implements Serializable
    {
        private static final long serialVersionUID = 1L;
        private final ProgramEngine engine;
        private final int maxExpandLevel;

        public StateData(ProgramEngine engine, int maxExpandLevel)
        {
            this.engine = engine;
            this.maxExpandLevel = maxExpandLevel;
        }

        public ProgramEngine getEngine() { return engine; }
        public int getMaxExpandLevel() { return maxExpandLevel; }
    }
}