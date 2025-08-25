package core;

import dto.engine.ExecutionStatisticsDTO;
import dto.engine.ProgramDTO;
import generated.SInstruction;
import generated.SProgram;

import java.util.*;
import java.util.stream.Collectors;

public class ProgramEngine
{
    private final String programName;
    private final Map<String, Integer> originalContextMap = new HashMap<>();
    private final List<Map<String, Integer>> contextMapsByExpandLevel = new ArrayList<>();
    private final List<Instruction> originalInstructions;
    private final List<List<Instruction>> instructionExpansionLevels = new ArrayList<>();
    private final Set<String> originalLabels;
    private final List<Set<String>> labelsByExpandLevel = new ArrayList<>();
    private final List<ExecutionStatistics> executionStatisticsList = new ArrayList<>();

    public ProgramEngine(SProgram program)
    {
        this.programName = program.getName();

        originalInstructions = program.getSInstructions().getSInstruction().stream()
                .map(Instruction::createInstruction)
                .collect(Collectors.toList());

        originalLabels = program.getSInstructions().getSInstruction().stream()
                .map(SInstruction::getSLabel)
                .filter(Objects::nonNull)
                .filter(label -> !label.isBlank())
                .map(String::trim)
                .filter(label -> label.startsWith("L"))
                .collect(Collectors.toSet());
        instructionExpansionLevels.add(originalInstructions);
        labelsByExpandLevel.add(originalLabels);
        initializeContextMap();
        contextMapsByExpandLevel.add(new HashMap<>(originalContextMap));

    }

    private void initializeContextMap()
    {
        originalContextMap.clear();
        originalContextMap.put(ProgramUtils.outputName, 0);
        originalContextMap.put(Instruction.ProgramCounterName, 0); // Program Counter
        for (int instruction_index = 0; instruction_index < originalInstructions.size(); instruction_index++)
        {
            Instruction instruction = originalInstructions.get(instruction_index);
            originalContextMap.put(instruction.getMainVarName().trim(), 0);
            if (!instruction.getLabel().isEmpty())
            {
                originalContextMap.put(instruction.getLabel().trim(), instruction_index);
            }
            for (String argName : instruction.getArgs().values())
            {
                if (!originalContextMap.containsKey(argName))
                {
                    if (isLabelArgument(argName) && !validateLabel(argName))
                    {
                        throw new LabelNotExist(
                                instruction.getClass().getSimpleName(),
                                instruction_index + 1,
                                argName);

                    } else if (!ProgramUtils.isNumber(argName))
                    {
                        originalContextMap.put(argName.trim(), 0);
                    }
                }
                if (argName.equals(ProgramUtils.EXITLabelName))
                {
                    originalContextMap.put(argName, originalInstructions.size()); // EXIT label is set to the end of the program
                    originalLabels.add(argName);
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
        for (String label : originalLabels)
        {
            if (!originalContextMap.containsKey(label))
            {
                originalContextMap.put(label.trim(), -1); // Initialize unused labels with -1 to indicate they are not used
            }
        }
    }

    private boolean validateLabel(String labelName)
    {
        return originalLabels.contains(labelName);
    }

    public void run(int expandLevel, List<Integer> arguments)
    {
        ExecutionStatistics exStats = new ExecutionStatistics(executionStatisticsList.size() + 1,
                expandLevel, arguments);
        insertArguments(arguments);
        expand(expandLevel);
        List<Instruction> executedInstructions = instructionExpansionLevels.get(expandLevel);
        Map<String, Integer> executedContextMap = contextMapsByExpandLevel.get(expandLevel);
        while (executedContextMap.get(Instruction.ProgramCounterName) < executedInstructions.size())
        {
            int currentPC = executedContextMap.get(Instruction.ProgramCounterName);
            Instruction instruction = executedInstructions.get(currentPC);
            try
            {
                instruction.execute(executedContextMap);
                exStats.incrementCycles(instruction.getCycles());
            } catch (IllegalArgumentException e)
            {
                throw new RuntimeException("Error executing instruction at PC=" + currentPC + ": " + e.getMessage(), e);
            }
        }
        exStats.setY(executedContextMap.get(ProgramUtils.outputName));
        executionStatisticsList.add(exStats);
    }

    private void insertArguments(List<Integer> arguments)
    {
        int argIndex = 1;
        String argName;
        for (Integer argValue : arguments)
        {
            argName = "x" + argIndex;
            originalContextMap.put(argName, argValue);
            argIndex++;
        }
    }

    private void expand(int level)
    {
        if (level > 0)
        {
            for (int currLevel = instructionExpansionLevels.size(); currLevel <= level; currLevel++)
            {
                List<Instruction> tempExpanded = new ArrayList<>();
                List<Instruction> previouslyExpanded = instructionExpansionLevels.getLast();
                Map<String, Integer> latestContextMap = new HashMap<>(contextMapsByExpandLevel.getLast());
                for (int i = 0; i < previouslyExpanded.size(); i++)
                {
                    Instruction instruction = previouslyExpanded.get(i);
                    List<Instruction> furtherExpanded = instruction.expand(latestContextMap, i);
                    tempExpanded.addAll(furtherExpanded);
                }
                instructionExpansionLevels.add(tempExpanded);
                contextMapsByExpandLevel.add(latestContextMap);
                updateLabelsAfterExpanding();
            }
        }
    }

    private void updateLabelsAfterExpanding()
    {
        List<Instruction> LatestExpanded = instructionExpansionLevels.getLast();
        Map<String, Integer> latestContextMap = contextMapsByExpandLevel.getLast();
        Set<String> latestLabels = new HashSet<>(labelsByExpandLevel.getLast());
        // update context map with new labels and their indices
        LatestExpanded.stream()
                .filter(instr -> !instr.getLabel().isBlank())
                .forEach(instr -> latestContextMap.put(instr.getLabel(), LatestExpanded.indexOf(instr)));
        // update EXIT label to point to the end of the expanded program
        if (latestLabels.contains(ProgramUtils.outputName))
        {
            latestContextMap.put(ProgramUtils.EXITLabelName, LatestExpanded.size());
        }
        // add any new labels introduced during expansion
        latestContextMap.keySet()
                .stream()
                .filter(var -> var.startsWith("L"))
                .forEach(latestLabels::add);
        labelsByExpandLevel.add(latestLabels);
    }

    public Map<String, Integer> extractArguments()
    {
        Map<String, Integer> arguments = new HashMap<>();
        for (String varName : originalContextMap.keySet())
        {
            if (varName.startsWith("x"))
            {
                arguments.put(varName, originalContextMap.get(varName));
            }
        }
        return arguments;
    }

    public List<String> extractLabels(int expandLevel)
    {
        if (expandLevel < 0 || expandLevel >= labelsByExpandLevel.size())
        {
            throw new IllegalArgumentException("Invalid expand level: " + expandLevel);
        }
        return new ArrayList<>(labelsByExpandLevel.get(expandLevel));
    }

    // TODO - delete this method after testing
//    public void printProgram(int expandLevel)
//    {
//        List<Instruction> instructionsToPrint = instructionExpansionLevels.get(expandLevel);
//        Map<String, Integer> contextMapToPrint = contextMapsByExpandLevel.get(expandLevel);
//        Set<String> labelsToPrint = labelsByExpandLevel.get(expandLevel);
//        System.out.println("Program Name: " + programName);
//        System.out.println("Instructions:");
//        for (int i = 0; i < instructionsToPrint.size(); i++)
//        {
//            System.out.println(instructionsToPrint.get(i).getDisplayFormat(i));
//        }
//        System.out.println("Context Map: " + contextMapToPrint);
//        System.out.println("Labels: " + labelsToPrint);
//        if (!executionStatisticsList.isEmpty())
//        {
//            System.out.println("num of cycles:" + executionStatisticsList.getLast().getNumOfCycles());
//        }
//    }

    // TODO - delete this method after testing
//    public void printProgramToFile(int expandLevel, String fileName)
//    {
//        List<Instruction> instructionsToPrint = instructionExpansionLevels.get(expandLevel);
//        Map<String, Integer> contextMapToPrint = contextMapsByExpandLevel.get(expandLevel);
//        Set<String> labelsToPrint = labelsByExpandLevel.get(expandLevel);
//        String outputDir = "outputs";
//        String outputPath = outputDir + "/" + fileName;
//        try
//        {
//            Files.createDirectories(Paths.get(outputDir));
//            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath)))
//            {
//                writer.write("Program Name: " + programName);
//                writer.newLine();
//                writer.write("Instructions:");
//                writer.newLine();
//                for (int i = 0; i < instructionsToPrint.size(); i++)
//                {
//                    writer.write(instructionsToPrint.get(i).getDisplayFormat(i));
//                    writer.newLine();
//                }
//                writer.write("Context Map: ");
//                writer.newLine();
//                for (Map.Entry<String, Integer> entry : contextMapToPrint.entrySet())
//                {
//                    writer.write(entry.getKey() + " : " + entry.getValue());
//                    writer.newLine();
//                }
//                writer.write("Labels: " + labelsToPrint);
//                writer.newLine();
//                if (!executionStatisticsList.isEmpty())
//                {
//                    System.out.println("num of cycles:" + executionStatisticsList.getLast().getNumOfCycles());
//                }
//            }
//        } catch (IOException e)
//        {
//            throw new RuntimeException("Failed to write program output to file", e);
//        }
//    }

    public int getMaxExpandLevel()
    {
        return ProgramUtils.getMaxExpandLevel(originalInstructions);
    }

    public ProgramDTO toDTO(int expandLevel)
    {
        return new ProgramDTO(
                programName,
                extractArguments(),
                extractLabels(expandLevel),
                instructionExpansionLevels.get(expandLevel).stream()
                        .map(Instruction::toDTO)
                        .collect(Collectors.toList())
        );
    }

    public List<ExecutionStatisticsDTO> getExecutionStatisticsDTOList()
    {
        return executionStatisticsList.stream()
                .map(ExecutionStatistics::toDTO)
                .toList();
    }
}
