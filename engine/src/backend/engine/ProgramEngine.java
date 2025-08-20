package backend.engine;

import backend.system.generated.SInstruction;
import backend.system.generated.SProgram;

import java.util.*;
import java.util.stream.Collectors;

public class ProgramEngine
{
    private final String programName;
    private final Map<String, Integer> contextMap = new HashMap<>();
    private final List<Instruction> instructions;
    private final Set<String> labels;
    private final List<ExecutionStatistics> executionStatisticsList = new ArrayList<>();
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
            if (!instruction.getLabel().isEmpty())
            {
                contextMap.put(instruction.getLabel(), instruction_index);
            }
            for (String argName : instruction.getArgs().values())
            {
                if (!contextMap.containsKey(argName))
                {
                    if (isLabelArgument(argName))
                    {
                        if (!validateLabel(argName))
                        {
                            throw new LabelNotExist(
                                    instruction.getClass().getSimpleName(),
                                    instruction_index + 1,
                                    argName);
                        }
                    } else
                    {
                        contextMap.put(argName, 0);
                    }
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
        return argName.startsWith("L");
    }

    private boolean validateLabel(String labelName)
    {
        return labels.contains(labelName);
    }

    public void run()
    {
        ExecutionStatistics exStats = new ExecutionStatistics(executionStatisticsList.size() + 1);
        while(contextMap.get(Instruction.ProgramCounterName) < instructions.size())
        {
            int currentPC = contextMap.get(Instruction.ProgramCounterName);
            Instruction instruction = instructions.get(currentPC);
            try
            {
                instruction.execute(contextMap);
                exStats.incrementCycles(instruction.getCycles());
            } catch (IllegalArgumentException e)
            {
                throw new RuntimeException("Error executing instruction at PC=" + currentPC + ": " + e.getMessage(), e);
            }
        }
        exStats.setY(contextMap.get(outputName));
        executionStatisticsList.add(exStats);
    }

    // TODO - delete this method before commiting
    public void printProgram()
    {
        System.out.println("Program Name: " + programName);
        System.out.println("Instructions:");
        for (int i = 0; i < instructions.size(); i++)
        {
           System.out.println(instructions.get(i).getDisplayFormat(i+1));
        }
        System.out.println("Context Map: " + contextMap);
        System.out.println("Labels: " + labels);
    }
}
