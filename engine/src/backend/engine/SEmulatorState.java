package backend.engine;

import backend.engine.loader.ProgramData;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Serializable state container for S-EMULATOR
 */
public class SEmulatorState implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<Command> commands;
    private final ProgramData programData;
    private final Map<String, Integer> variables;
    private final int programCounter;

    public SEmulatorState(List<Command> commands, ProgramData programData,
                          Map<String, Integer> variables, int programCounter) {
        this.commands = commands;
        this.programData = programData;
        this.variables = variables;
        this.programCounter = programCounter;
    }

    public List<Command> getCommands() { return commands; }
    public ProgramData getProgramData() { return programData; }
    public Map<String, Integer> getVariables() { return variables; }
    public int getProgramCounter() { return programCounter; }
}