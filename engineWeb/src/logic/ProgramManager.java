package logic;

import engine.core.ProgramEngine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProgramManager {
    private final Map<String, ProgramEngine> programs = new HashMap<>();
    private final Set<ProgramEngine> functions = new HashSet<>();

    public synchronized void addProgram(String programName, ProgramEngine program) {
        programs.put(programName, program);
    }

    public synchronized Map<String, ProgramEngine> getPrograms() {
        return programs;
    }

    public synchronized Set<ProgramEngine> getFunctions() {
        return functions;
    }

    public boolean isProgramExists(String programName) {
        return programs.containsKey(programName);
    }
}
