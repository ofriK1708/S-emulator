package logic.manager;

import dto.engine.FunctionMetadataDTO;
import dto.engine.ProgramMetadataDTO;
import engine.core.Engine;
import engine.exception.FunctionAlreadyExist;
import engine.exception.FunctionNotFound;
import engine.exception.LabelNotExist;
import engine.generated_2.SProgram;
import logic.User;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class ProgramManager {
    // region data structures
    private final @NotNull Map<String, Engine> programs = new HashMap<>();
    private final @NotNull Map<String, Engine> functions = new HashMap<>();
    private final @NotNull Map<String, Engine> functionsAndPrograms = new HashMap<>();
    // endregion
    // region read-write locks
    private final @NotNull ReadWriteLock programsAndFunctionsLock = new ReentrantReadWriteLock();
    private final @NotNull Lock readLock = programsAndFunctionsLock.readLock();
    private final @NotNull Lock writeLock = programsAndFunctionsLock.writeLock();

    /**
     * Private constructor to prevent instantiation from outside the class.
     */
    private ProgramManager() {
    }

    /**
     * Provides the singleton instance of the manager.
     *
     * @return The single instance of ProgramManager.
     */
    public static ProgramManager getInstance() {
        return ProgramManagerHolder.INSTANCE;
    }

    /**
     * Add a new program to the system. keeps the order of insertion.
     * crates an Engine for the program and adds its functions to the system
     *
     * @param programName the name of the new program, this will be the ID for the program
     * @param sProgram    the SProgram object representing the program
     * @throws LabelNotExist        if a label in the program does not exist
     * @throws FunctionNotFound     if a function called in the program is not found either locally or globally
     * @throws FunctionAlreadyExist if a function being added already exists in the system
     */
    public void addProgram(String programName, SProgram sProgram, User user)
            throws LabelNotExist, FunctionNotFound, FunctionAlreadyExist {
        Engine mainProgramEngine = Engine.createMainProgramEngine(sProgram, functionsAndPrograms, user.getName());
        mainProgramEngine.addProgramAndFunctionsToSystem(functionsAndPrograms, functions);
        user.incrementMainProgramsUploaded();
        user.addFunctionsCount(mainProgramEngine.getFunctionsCount());
        writeLock.lock();
        try {
            programs.put(programName, mainProgramEngine);
        } finally {
            writeLock.unlock();
        }


    }
    // endregion
    // region program and function management methods

    /**
     * Get a program engine by name.
     * @param name the name of the program to retrieve
     * @return the Engine object if found
     * @throws IllegalStateException if the program is not found
     */
    public Engine getProgramOrFunctionEngine(String name) {
        readLock.lock();
        try {
            if (functionsAndPrograms.containsKey(name)) {
                return functionsAndPrograms.get(name);
            } else {
                throw new IllegalStateException("Program or function " + name + " not found!");
            }
        } finally {
            readLock.unlock();
        }
    }

    public Set<ProgramMetadataDTO> getProgramsMetadata() {
        readLock.lock();
        try {
            return programs.values().stream()
                    .map(Engine::programToMetadata)
                    .collect(Collectors.toSet());
        } finally {
            readLock.unlock();
        }
    }

    public Set<String> getProgramNames() {
        readLock.lock();
        try {
            return programs.keySet();
        } finally {
            readLock.unlock();
        }
    }

    public Set<FunctionMetadataDTO> getFunctionsMetadata() {
        readLock.lock();
        try {
            return functions.values().stream()
                    .map(Engine::functionToMetadata)
                    .collect(Collectors.toSet());
        } finally {
            readLock.unlock();
        }
    }

    public Set<String> getFunctionNames() {
        readLock.lock();
        try {
            return functions.keySet();
        } finally {
            readLock.unlock();
        }
    }

    public boolean isProgramExists(String programName) {
        readLock.lock();
        try {
            return programs.containsKey(programName);
        } finally {
            readLock.unlock();
        }
    }

    public boolean isFunctionOrProgramExists(String name) {
        readLock.lock();
        try {
            return functionsAndPrograms.containsKey(name);
        } finally {
            readLock.unlock();
        }
    }

    // endregion
    // region singleton pattern
    private static class ProgramManagerHolder {
        private static final ProgramManager INSTANCE = new ProgramManager();
    }
    // endregion
}
