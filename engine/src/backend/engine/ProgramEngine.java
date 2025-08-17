package backend.engine;

import backend.system.generated.SInstruction;
import backend.system.generated.SProgram;

import java.util.*;
import java.util.stream.Collectors;

public class ProgramEngine
{
    private final String programName;
    private Map<String, Integer> contextMap = new HashMap<>();
    private List<Instruction> instructions = new LinkedList<>();
    private Set<String> labels = new HashSet<>();
    private statistics programStats = null;
    private final String outputName = "y";

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
        contextMap.clear();
        contextMap.put(outputName, 0);
        contextMap.put(Instruction.ProgramCounter, 0); // Program Counter
        for (int instruction_index = 0; instruction_index < instructions.size(); instruction_index++)
        {
            Instruction instruction = instructions.get(instruction_index);
            contextMap.put(instruction.getMainVarName(), 0);
            if (instruction.getLabel() != null) {
                contextMap.put(instruction.getLabel(), instruction_index);
            }
            for (String argName : instruction.getArgs().values())
            {
                if (!contextMap.containsKey(argName) && !isLabelArgument(argName))
                {
                    contextMap.put(argName, 0);
                }
            }
        }
    }
    private boolean isLabelArgument(String argName)
    {
        return labels.contains(argName) || argName.startsWith("L");
    }

}
