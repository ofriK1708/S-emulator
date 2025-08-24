package backend.engine;

import backend.system.generated.SInstruction;
import backend.system.generated.SProgram;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class ProgramEngine
{
    private final String programName;
    private final Map<String, Integer> contextMap = new HashMap<>();
    private final List<Instruction> originalInstructions;
    private final List<List<Instruction>> instructionExpansionLevels = new ArrayList<>();
    private final Set<String> labels;
    private final List<ExecutionStatistics> executionStatisticsList = new ArrayList<>();
    public static final String outputName = "y"; // TODO - not sure about this being public
    private static final String EXITLabelName = "EXIT";

    Random random = new Random(); // TODO - delete this!!!

    public ProgramEngine(SProgram program)
    {
        this.programName = program.getName();
        originalInstructions = program.getSInstructions().getSInstruction()
                .stream()
                .map(Instruction::createInstruction)
                .collect(Collectors.toList());
        labels = program.getSInstructions().getSInstruction()
                .stream()
                .map(SInstruction::getSLabel)
                .filter(Objects::nonNull)
                .filter(label -> label.startsWith("L"))
                .collect(Collectors.toSet());
        instructionExpansionLevels.add(originalInstructions); // Level 0 is the original instructions
        initializeContextMap();

    }

    private void initializeContextMap()
    {
        contextMap.clear();

        contextMap.put(outputName, 0);
        contextMap.put(Instruction.ProgramCounterName, 0); // Program Counter
        for (int instruction_index = 0; instruction_index < originalInstructions.size(); instruction_index++)
        {
            Instruction instruction = originalInstructions.get(instruction_index);
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
                    contextMap.put(EXITLabelName, originalInstructions.size()); // EXIT label is set to the end of the program
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
        List<Instruction> executedInstructions = instructionExpansionLevels.get(expandLevel);
        while (contextMap.get(Instruction.ProgramCounterName) < originalInstructions.size())
        {
            int currentPC = contextMap.get(Instruction.ProgramCounterName);
            Instruction instruction = originalInstructions.get(currentPC);
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
            for (int currLevel = instructionExpansionLevels.size(); currLevel <= level; currLevel++)
            {
                List<Instruction> tempExpanded = new ArrayList<>();
                List<Instruction> previouslyExpanded = instructionExpansionLevels.get(currLevel - 1);
                for (int i = 0; i < previouslyExpanded.size(); i++)
                {
                    Instruction instruction = previouslyExpanded.get(i);
                    List<Instruction> furtherExpanded = instruction.expand(contextMap, i);
                    tempExpanded.addAll(furtherExpanded);
                }
                instructionExpansionLevels.add(tempExpanded);
                updateLabelsAfterExpanding();
            }

        }
    }

    private void updateLabelsAfterExpanding()
    {
        List<Instruction> LatestExpanded = instructionExpansionLevels.getLast();
        for (int instruction_index = 0; instruction_index < LatestExpanded.size(); instruction_index++)
        {
            Instruction instruction = LatestExpanded.get(instruction_index);
            if (!instruction.getLabel().isEmpty())
            {
                contextMap.put(instruction.getLabel(), instruction_index);
            }
            if (labels.contains(EXITLabelName))
            {
                contextMap.put(EXITLabelName, LatestExpanded.size()); // EXIT label is set to the end of the expanded program
            }
        }
        contextMap.keySet()
                .stream()
                .filter(var -> var.startsWith("L"))
                .forEach(labels::add);
    }

    // TODO - delete this method after testing
    public void printProgram(int expandLevel)
    {
        List<Instruction> instructionsToPrint = instructionExpansionLevels.get(expandLevel);
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
    public void printProgramToFile(int expandLevel, String fileName)
    {
        List<Instruction> instructionsToPrint = instructionExpansionLevels.get(expandLevel);
        String outputDir = "outputs";
        String outputPath = outputDir + "/" + fileName;
        try
        {
            Files.createDirectories(Paths.get(outputDir));
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
