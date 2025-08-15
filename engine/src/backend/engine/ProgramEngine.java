package backend.engine;

import backend.system.generated.SProgram;

import java.util.*;

public class ProgramEngine
{
    private String programName;
    private Map<String, Integer> contextMap = new HashMap<>();
    private LinkedList<Command> commands = new LinkedList<>();
    private Set<String> labels = new HashSet<>();
    private statistics programStats = null;

    public ProgramEngine(SProgram program)
    {
        this.programName = program.getName();

    }
}
