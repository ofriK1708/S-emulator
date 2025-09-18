package system.controller.controller;

import dto.engine.ExecutionResultDTO;
import dto.engine.ExecutionStatisticsDTO;
import dto.engine.ProgramDTO;
import engine.core.ProgramEngine;
import engine.exception.LabelNotExist;
import engine.generated.SProgram;
import engine.utils.ProgramUtils;
import jakarta.xml.bind.JAXBException;
import system.file.file.processing.XMLHandler;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class EngineController
{
    private final XMLHandler xmlHandler;
    private ProgramEngine engine;
    public static Predicate<String> validArgumentValueCheck = ProgramUtils.validArgumentCheck;
    private int maxExpandLevel = 0;

    // Add these fields to Debuger:
    private boolean inDebugSession = false;


    public EngineController()
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
        validateFileExistRegularAndReadable(xmlFilePath);
        SProgram program = xmlHandler.unmarshallFile(xmlFilePath);
        createEngine(program);

    }

    private void validateFileExistRegularAndReadable(Path filePath) throws IOException
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


    public ExecutionResultDTO runLoadedProgram(int expandLevel, Map<String, Integer> arguments)
    {
        if (engine == null)
        {
            throw new IllegalStateException("Program has not been set");
        }
        if (expandLevel < 0 || expandLevel > maxExpandLevel)
        {
            throw new IllegalArgumentException("Expand level must be between 0 and " + maxExpandLevel);
        }
        engine.run(expandLevel, arguments);
        return engine.toExecutionResultDTO(expandLevel);
    }


    public int getCyclesCount(int expandLevel) {
        return engine.getTotalCycles(expandLevel);
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

    public void clearLoadedProgram() {
        engine = null;
        maxExpandLevel = 0;
    }

    public void saveProgramState(Path directoryPath) throws IOException
    {
        if (engine == null)
        {
            throw new IllegalStateException("No program loaded to save");
        }

        StateData stateData = new StateData(engine, maxExpandLevel);

        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(directoryPath)))
        {
            oos.writeObject(stateData);
        }
        catch (IOException e)
        {
            throw new IOException("Failed to save state: " + e.getMessage(), e);
        }
    }

    public void loadProgramState(Path filePath) throws IOException, ClassNotFoundException
    {
        validateFileExistRegularAndReadable(filePath);
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(filePath)))
        {
            StateData stateData = (StateData) ois.readObject();
            this.engine = stateData.engine();
            this.maxExpandLevel = stateData.maxExpandLevel();
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

    private record StateData(ProgramEngine engine, int maxExpandLevel) implements Serializable
    {
        @Serial
        private static final long serialVersionUID = 1L;
    }
    // Add these methods to implement debugging functionality:

    public void startDebugSession(int expandLevel, Map<String, Integer> arguments) {
        if (engine == null) {
            throw new IllegalStateException("Program has not been set");
        }
        if (expandLevel < 0 || expandLevel > maxExpandLevel) {
            throw new IllegalArgumentException("Expand level must be between 0 and " + maxExpandLevel);
        }

        engine.startDebugSession(expandLevel, arguments);
        inDebugSession = true;
    }

    public ExecutionResultDTO debugStep() {
        if (engine == null || !inDebugSession) {
            throw new IllegalStateException("Debug session not active");
        }
        return engine.debugStep();
    }

    public ExecutionResultDTO debugStepBackward() {
        if (engine == null || !inDebugSession) {
            throw new IllegalStateException("Debug session not active");
        }
        return engine.debugStepBackward();
    }

    public ExecutionResultDTO debugResume() {
        if (engine == null || !inDebugSession) {
            throw new IllegalStateException("Debug session not active");
        }
        ExecutionResultDTO result = engine.debugResume();
        inDebugSession = false; // Session ends after resume
        return result;
    }

    public void stopDebugSession() {
        if (engine != null && inDebugSession) {
            engine.stopDebugSession();
            inDebugSession = false;
        }
    }

    public int getCurrentDebugPC() {
        if (engine == null || !inDebugSession) {
            return -1;
        }
        return engine.getCurrentDebugPC();
    }

    public boolean isInDebugSession() {
        return inDebugSession && engine != null && engine.isInDebugMode();
    }

    public boolean isDebugFinished() {
        return engine != null && engine.isDebugFinished();
    }

}