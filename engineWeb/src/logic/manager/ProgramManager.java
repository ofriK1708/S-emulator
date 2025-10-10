package logic.manager;

import engine.core.ProgramEngine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ProgramManager {
    @NotNull
    private final Map<String, ProgramEngine> programs = new HashMap<>();
    @NotNull
    private final Map<String, ProgramEngine> functions = new HashMap<>();
    @NotNull
    private final Object programsAndFunctionsListLock = new Object();
    @NotNull
    private final Object currentProgramLock = new Object();
    @Nullable
    private ProgramEngine currentProgram;

    public void addProgram(String programName, ProgramEngine program) {
        synchronized (programsAndFunctionsListLock) {
            programs.put(programName, program);
            functions.putAll(program.getAllFunctionsInMain());
            functions.put(programName, program); // the program is also a function
        }
    }

    public Map<String, ProgramEngine> getPrograms() {
        synchronized (programsAndFunctionsListLock) {
            return programs;
        }
    }

    public Map<String, ProgramEngine> getFunctions() {
        synchronized (programsAndFunctionsListLock) {
            return functions;
        }
    }

    public boolean isProgramExists(String programName) {
        synchronized (programsAndFunctionsListLock) {
            return programs.containsKey(programName);
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
