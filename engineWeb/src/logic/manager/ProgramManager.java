package logic.manager;

import dto.engine.FunctionMetadata;
import dto.engine.ProgramMetadata;
import engine.core.ProgramEngine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ProgramManager {
    @NotNull
    private final Map<String, ProgramEngine> programs = new HashMap<>();
    @NotNull
    private final Map<String, ProgramEngine> functions = new HashMap<>();
    @NotNull
    private final Map<String, ProgramEngine> functionsAndPrograms = new HashMap<>();
    @NotNull
    private final Object programsAndFunctionsListLock = new Object();
    @NotNull
    private final Object currentProgramLock = new Object();
    @Nullable
    private ProgramEngine currentProgram;

    public void addProgram(String programName, ProgramEngine program) {
        synchronized (programsAndFunctionsListLock) {
            programs.put(programName, program);
            functionsAndPrograms.put(programName, program);
            Map<String, ProgramEngine> allFunctionsAndPrograms = program.getFunctionsAndProgramsInSystem();
            if (allFunctionsAndPrograms != null) {
                for (Map.Entry<String, ProgramEngine> entry : allFunctionsAndPrograms.entrySet()) {
                    String functionName = entry.getKey();
                    ProgramEngine functionProgram = entry.getValue();
                    if (!functionsAndPrograms.containsKey(functionName)) {
                        functionsAndPrograms.put(functionName, functionProgram);
                        if (functionProgram.isFunction()) {
                            functions.put(functionName, functionProgram);
                        }
                    }
                }
            }
        }

    }

    public Set<ProgramMetadata> getProgramsMetadata() {
        synchronized (programsAndFunctionsListLock) {
            return programs.values().stream()
                    .map(ProgramEngine::programToMetadata)
                    .collect(Collectors.toSet());
        }
    }

    public Set<String> getProgramNames() {
        synchronized (programsAndFunctionsListLock) {
            return programs.keySet();
        }
    }

    public Map<String, ProgramEngine> getFunctionsAndPrograms() {
        synchronized (programsAndFunctionsListLock) {
            return functionsAndPrograms;
        }
    }

    public Set<FunctionMetadata> getFunctionsMetadata() {
        synchronized (programsAndFunctionsListLock) {
            return functions.values().stream()
                    .map(ProgramEngine::functionToMetadata)
                    .collect(Collectors.toSet());
        }
    }

    public Set<String> getFunctionNames() {
        synchronized (programsAndFunctionsListLock) {
            return functions.keySet();
        }
    }

    public boolean isProgramExists(String programName) {
        synchronized (programsAndFunctionsListLock) {
            return programs.containsKey(programName);
        }
    }

    public ProgramEngine getProgramOrFunctionEngine(String name) {
        synchronized (programsAndFunctionsListLock) {
            if (functionsAndPrograms.containsKey(name)) {
                return functionsAndPrograms.get(name);
            } else {
                throw new IllegalStateException("Program or function " + name + " not found!");
            }
        }
    }

    public ProgramEngine getCurrentProgram() {
        synchronized (currentProgramLock) {
            return currentProgram;
        }
    }

    public void setCurrentProgram(String programName) {
        synchronized (currentProgramLock) {
            if (programs.containsKey(programName)) {
                currentProgram = programs.get(programName);
            } else {
                throw new IllegalStateException("Program " + programName + " not found!");
            }
        }
    }
}
