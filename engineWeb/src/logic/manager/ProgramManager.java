package logic.manager;

import dto.engine.FunctionMetadata;
import dto.engine.ProgramMetadata;
import engine.core.Engine;
import engine.exception.FunctionAlreadyExist;
import engine.exception.FunctionNotFound;
import engine.exception.LabelNotExist;
import engine.generated_2.SProgram;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class ProgramManager {
    @NotNull
    private final Map<String, Engine> programs = new HashMap<>();
    @NotNull
    private final Map<String, Engine> functions = new HashMap<>();
    @NotNull
    private final Map<String, Engine> functionsAndPrograms = new HashMap<>();

    @NotNull
    private final ReadWriteLock programsAndFunctionsLock = new ReentrantReadWriteLock();
    @NotNull
    private final Lock readLock = programsAndFunctionsLock.readLock();
    @NotNull
    private final Lock writeLock = programsAndFunctionsLock.writeLock();

    public void addProgram(String programName, SProgram sProgram)
            throws LabelNotExist, FunctionNotFound, FunctionAlreadyExist {
        writeLock.lock();
        try {
            Engine mainProgramEngine = Engine.createMainProgramEngine(sProgram, functionsAndPrograms);
            mainProgramEngine.addProgramAndFunctionsToSystem(functionsAndPrograms, functions);
            programs.put(programName, mainProgramEngine);
        } finally {
            writeLock.unlock();
        }

    }

    public Set<ProgramMetadata> getProgramsMetadata() {
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

    public Map<String, Engine> getFunctionsAndPrograms() {
        readLock.lock();
        try {
            return functionsAndPrograms;
        } finally {
            readLock.unlock();
        }
    }

    public Set<FunctionMetadata> getFunctionsMetadata() {
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
}
