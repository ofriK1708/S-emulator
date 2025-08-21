package backend.engine;

import backend.system.generated.SInstruction;
import backend.system.generated.SProgram;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ProgramEngine
{
    private final String programName;
    private final Map<String, Integer> contextMap = new HashMap<>();
    private final List<Instruction> instructions;
    private List<Instruction> expandedInstructions = new ArrayList<>();
    private final Set<String> labels;
    private final List<ExecutionStatistics> executionStatisticsList = new ArrayList<>();
    public static final String outputName = "y"; // TODO - not sure about this being public
    private static final String EXITLabelName = "EXIT";

    Random random = new Random(); // TODO - delete this!!!
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
            contextMap.put(instruction.getMainVarName(), random.nextInt(100)); // TODO - change this to 0!!!
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
                        contextMap.put(argName, random.nextInt(100)); // TODO - change this to 0!!!
                    }
                }
                if (argName.equals(EXITLabelName))
                {
                    contextMap.put(EXITLabelName, instructions.size()); // EXIT label is set to the end of the program
                    labels.add(argName);
                }
            }
        }
        fillUnusedLabels();
    }

    private boolean isLabelArgument(String argName)
    {
        return argName.startsWith("L");
    }

    private void fillUnusedLabels()
    {
        for (String label : labels)
        {
            if (!contextMap.containsKey(label))
            {
                contextMap.put(label, -1); // Initialize unused labels with -1 to indicate they are not used
            }
        }
    }
    private boolean validateLabel(String labelName)
    {
        return labels.contains(labelName);
    }

    public void run(int expandLevel)
    {
        ExecutionStatistics exStats = new ExecutionStatistics(executionStatisticsList.size() + 1);
        expand(expandLevel);
        List<Instruction> executedInstructions = expandedInstructions.isEmpty() ? instructions : expandedInstructions;
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

    public void expand(int level)
    {
        if (level > 0)
        {
            expandedInstructions.clear();
            for (int i = 0; i < instructions.size(); i++)
            {
                Instruction instruction = instructions.get(i);
                List<Instruction> expanded = instruction.expand(contextMap, i);
                expandedInstructions.addAll(expanded);
            }
            updateLabelsAfterExpanding();
            if (level > 1)
            {
                for (int currLevel = 1; currLevel < level; currLevel++)
                {
                    List<Instruction> tempExpanded = new ArrayList<>();
                    for (int i = 0; i < expandedInstructions.size(); i++)
                    {
                        Instruction instruction = expandedInstructions.get(i);
                        List<Instruction> furtherExpanded = instruction.expand(contextMap, i);
                        tempExpanded.addAll(furtherExpanded);
                    }
                    expandedInstructions = tempExpanded;
                    updateLabelsAfterExpanding();
                }
            }

        }
    }

    private void updateLabelsAfterExpanding()
    {
        for (int instruction_index = 0; instruction_index < expandedInstructions.size(); instruction_index++)
        {
            Instruction instruction = expandedInstructions.get(instruction_index);
            if (!instruction.getLabel().isEmpty())
            {
                contextMap.put(instruction.getLabel(), instruction_index);
            }
            if (labels.contains(EXITLabelName))
            {
                contextMap.put(EXITLabelName, expandedInstructions.size()); // EXIT label is set to the end of the expanded program
            }
        }
        contextMap.keySet()
                .stream()
                .filter(var -> var.startsWith("L"))
                .forEach(labels::add);
    }

    // TODO - delete this method after testing
    public void printProgram()
    {
        List<Instruction> instructionsToPrint = expandedInstructions.isEmpty() ? instructions : expandedInstructions;
        System.out.println("Program Name: " + programName);
        System.out.println("Instructions:");
        for (int i = 0; i < instructionsToPrint.size(); i++)
        {
            System.out.println(instructionsToPrint.get(i).getDisplayFormat(i));
        }
        System.out.println("Context Map: " + contextMap);
        System.out.println("Labels: " + labels);
    }

    // TODO - delete this method after testing
    public void printProgramToFile(String fileName)
    {
        List<Instruction> instructionsToPrint = expandedInstructions.isEmpty() ? instructions : expandedInstructions;
        String outputDir = "outputs";
        String outputPath = outputDir + "/" + fileName;
        try
        {
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get(outputDir));
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath)))
            {
                writer.write("Program Name: " + programName);
                writer.newLine();
                writer.write("Instructions:");
                writer.newLine();
                for (int i = 0; i < instructionsToPrint.size(); i++)
                {
                    writer.write(instructionsToPrint.get(i).getDisplayFormat(i));
                    writer.newLine();
                }
                writer.write("Context Map: " + contextMap);
                writer.newLine();
                writer.write("Labels: " + labels);
                writer.newLine();
            }
        } catch (IOException e)
        {
            throw new RuntimeException("Failed to write program output to file", e);
        }
    }
}
