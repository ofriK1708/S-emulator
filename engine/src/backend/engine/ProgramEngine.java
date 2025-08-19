package backend.engine;

import backend.system.generated.SInstruction;
import backend.system.generated.SProgram;

import java.util.*;
import java.util.stream.Collectors;

public class ProgramEngine
{
    private final String programName;
    private Map<String, Integer> contextMap = new HashMap<>();
    private List<Instruction> instructions;
    private Set<String> labels;
    private statistics programStats = new statistics();
    private final String outputName = "y";
    private final String EXITLabelName = "EXIT";

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
                .filter(label -> label.startsWith("L"))
                .collect(Collectors.toSet());
        initializeContextMap();

    }

    private void initializeContextMap()
    {
        contextMap.clear();
        contextMap.put(outputName, 0);
        contextMap.put(Instruction.ProgramCounterName, 0); // Program Counter
        for (int instruction_index = 0; instruction_index < instructions.size(); instruction_index++)
        {
            Instruction instruction = instructions.get(instruction_index);
            contextMap.put(instruction.getMainVarName(), 0);
            if (instruction.getLabel() != null)
            {
                contextMap.put(instruction.getLabel(), instruction_index);
            }
            for (String argName : instruction.getArgs().values())
            {
                if (!contextMap.containsKey(argName) && !isLabelArgument(argName))
                {
                    contextMap.put(argName, 0); // initialize all work/unhandled input variables to 0
                }
                if (argName.equals(EXITLabelName))
                {
                    contextMap.put(EXITLabelName, instructions.size()); // EXIT label is set to the end of the program
                }

            }
        }
    }

    private boolean isLabelArgument(String argName)
    {
        return labels.contains(argName) || argName.startsWith("L");
    }

    private void run()
    {
        while(contextMap.get(Instruction.ProgramCounterName) < instructions.size())
        {
            int currentPC = contextMap.get(Instruction.ProgramCounterName);
            Instruction instruction = instructions.get(currentPC);
            try
            {
                instruction.execute(contextMap);
                programStats.incrementCycles(instruction.getCycles());
            } catch (IllegalArgumentException e)
            {
                System.err.println("Error executing instruction at PC=" + currentPC + ": " + e.getMessage());
                break;
            }
        }
    }
}
