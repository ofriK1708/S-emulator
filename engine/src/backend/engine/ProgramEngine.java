package backend.engine;

import backend.system.generated.SInstruction;
import backend.system.generated.SProgram;

import java.util.*;
import java.util.stream.Collectors;

public class ProgramEngine
{
    private String programName;
    private Map<String, Integer> contextMap = new HashMap<>();
    private List<Instruction> instructions = new LinkedList<>();
    private Set<String> labels = new HashSet<>();
    private statistics programStats = null;

    public ProgramEngine(SProgram program)
    {
        this.programName = program.getName();
        instructions = program.getSInstructions().getSInstruction()
                .stream()
                .map(Instruction::createInstruction)
                .collect(Collectors.toList());
        labels = program.getSInstructions().getSInstruction()
                .stream()
                .map(SInstruction::getSLabel)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        initializeContextMap();

    }

    private void initializeContextMap()
    {
    }
}
