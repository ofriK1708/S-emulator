package ui.console;

import dto.engine.ExecutionStatisticsDTO;
import dto.engine.InstructionDTO;
import dto.engine.ProgramDTO;
import system.controller.controller.SystemController;

import java.util.*;

public class ConsoleUI
{
    private final Scanner scanner;
    private ProgramDTO programDTO;
    private List<ExecutionStatisticsDTO> executionHistory;
    private final SystemController systemController;
    private final String invalidChoiceFormat = "Invalid choice. please enter a number between %d and %d.%n";
    private final Comparator<String> dataNameComparator =
            Comparator.comparingInt(str -> Integer.parseInt(str.substring(1)));
    private final boolean programLoaded = false;

    public ConsoleUI()
    {
        scanner = new Scanner(System.in);
        systemController = new SystemController();
    }

    public void start()
    {
        System.out.println("=== S-EMULATOR Console Interface ===");

        while (true)
        {
            displayMenu();
            int choice = getUserChoice();

            switch (choice)
            {
                case 1:
                    loadXMLFile();
                    break;
                case 2:
                    displayLoadedProgram();
                    break;
                case 3:
                    //runProgram();
                    break;
                case 4:
                    // expandProgram();
                    break;
                case 5:
                    // displayStatistics();
                    break;
                case 6:
                    exitSystem();
                    return;
            }

            System.out.println(); // רווח בין פעולות
        }
    }

    private void displayMenu()
    {
        System.out.println("\n=== Main Menu ===");
        System.out.println("1. Load XML File");
        System.out.println("2. Display Loaded Program");
        System.out.println("3. Run Program");
        System.out.println("4. Expand Program");
        System.out.println("5. Show History/Statistics");
        System.out.println("6. Exit System");
    }

    private void loadXMLFile()
    {
        try
        {
            System.out.println("Please enter a full path for the xml file:");
            String filePath = scanner.nextLine();
            systemController.LoadProgramFromFile(filePath);
            System.out.println("The program has been loaded successfully.");
        } catch (Exception e)
        {
            System.out.println("Error loading file " + e.getMessage());
            System.out.println("Please try fixing the file or choose another file");
            System.out.println("Returning to main menu...");
        }

    }

    private int getUserChoice()
    {
        while (true)
        {
            try
            {
                System.out.print("Enter an option (1-6): ");
                int choice = Integer.parseInt(scanner.nextLine().trim());
                if (choice >= 1 && choice <= 6)
                {
                    return choice;
                }
                System.out.printf(invalidChoiceFormat, 1, 6);
            } catch (IllegalArgumentException e)
            {
                System.out.printf(invalidChoiceFormat, 1, 6);
                displayMenu();
            }
        }
    }

    private void displayLoadedProgram()
    {
        if (!programLoaded)
        {
            printProgramNotLoaded();
            return;
        }
        System.out.println("=== Display Loaded Program ===");
        printProgram();
    }

    private void printProgram()
    {
        System.out.println("Program Name: " + programDTO.ProgramName());
        System.out.println("Arguments");
        programDTO.arguments().keySet().stream()
                .sorted(dataNameComparator)
                .forEach(System.out::println);
        System.out.println("\nLabels:");
        if (programDTO.labels().isEmpty())
        {
            System.out.println("No labels in the program.");
        } else
        {
            programDTO.labels().stream()
                    .sorted(dataNameComparator)
                    .forEach(System.out::println);
        }

        System.out.println("\nInstructions:");
        List<InstructionDTO> instructions = programDTO.instructions();
        for (int i = 0; i < instructions.size(); i++)
        {
            printInstruction(instructions.get(i), i);
        }
    }

    private void printInstruction(InstructionDTO instruction, int i)
    {
        String numberPart = "#" + (i + 1);
        String typePart = instruction.type();
        String labelPart = "[ " + String.format("%-4s", instruction.label()) + "]";
        String cyclesPart = "(" + instruction.cycles() + ")";
        String full = String.format("%s %s %s %s %s", numberPart, typePart,
                labelPart, instruction.command(), cyclesPart);
        System.out.print(full);
        Map<InstructionDTO, Integer> derivedFrom = instruction.derivedFromInstructions();
        for (Map.Entry<InstructionDTO, Integer> entry : derivedFrom.entrySet())
        {
            System.out.print(" >>> ");
            printInstruction(entry.getKey(), entry.getValue());
        }
    }

    private void expandProgram()
    {
        if (!programLoaded)
        {
            printProgramNotLoaded();
            return;
        }
        try
        {
            int expandLevel = getExpandLevelChoiceFromUser();
            programDTO = systemController.getProgramByExpandLevel(expandLevel);
            printProgram();
        } catch (Exception e)
        {
            System.out.println("Error loading program " + e.getMessage());
            System.out.println("returning to main menu...");
        }
    }

    private int getExpandLevelChoiceFromUser()
    {
        int maxLevel = systemController.getMaxExpandLevel();
        System.out.println("Please enter the expand level you would like to expand:");
        System.out.println("Please enter a number between 0 and " + maxLevel);
        while (true)
        {
            try
            {
                int choice = Integer.parseInt(scanner.nextLine());
                if (choice >= 0 && choice <= maxLevel)
                {
                    return choice;
                }
                System.out.printf(invalidChoiceFormat, 0, maxLevel);
            } catch (IllegalArgumentException e)
            {
                System.out.printf(invalidChoiceFormat, 0, maxLevel);
            }
        }
    }

    private void runProgram()
    {
        if (!programLoaded)
        {
            printProgramNotLoaded();
            return;
        }
        int expandLevel = getExpandLevelChoiceFromUser();
        List<Integer> runtimeArguments = new ArrayList<>();
        getUserArguments(runtimeArguments);
        try
        {
            System.out.println("\nTrying executing program in expand level " + expandLevel);
            programDTO = systemController.runLoadedProgram(expandLevel, runtimeArguments);
            System.out.println("Program finished executing successfully.");
            printProgram();

        } catch (Exception e)
        {
            System.out.println("Error executing program: " + e.getMessage());
            System.out.println("returning to main menu...");
        }
    }

    public void getUserArguments(List<Integer> arguments)
    {
        System.out.println("Please enter the program arguments (non-negative numbers separated by commas):");
        systemController.getProgramArgsNames().stream()
                .sorted(dataNameComparator)
                .forEach(arg -> System.out.print(arg + ","));
        boolean valid = false;
        String userArguments = scanner.nextLine();
        while (!valid)
        {
            try
            {
                arguments.clear();
                for (String arg : userArguments.split(","))
                {
                    int progArg = Integer.parseInt(arg.trim());
                    arguments.add(progArg);
                }
                valid = arguments.stream().allMatch(value -> value >= 0);
                if (!valid)
                {
                    System.out.println("Invalid input! Please enter only non-negative integers separated by commas.");
                    userArguments = scanner.nextLine();
                }
            } catch (NumberFormatException e)
            {
                System.out.println("Invalid input! Please enter only non-negative integers separated by commas.");
                userArguments = scanner.nextLine();
            }
        }
        System.out.println("Arguments loaded successfully.");
    }

    private void displayStatistics()
    {
        System.out.println("\n=== Statistics ===");

        if (executionHistory.isEmpty())
        {
            System.out.println("No execution history available. Run a program first.");
            return;
        }

        System.out.println("Total executions: " + executionHistory.size());
        System.out.println("\nExecution History:");
        System.out.println("+---------+---------+-----------+--------+--------+");
        System.out.println("| Exec #  | Level   | Arguments | Result | Cycles |");
        System.out.println("+---------+---------+-----------+--------+--------+");

        for (ExecutionStatisticsDTO stat : executionHistory)
        {
            System.out.printf("| %-7d | %-7d | %-9s | %-6d | %-6d |%n",
                    stat.executionNumber(),
                    stat.levelOfExpansion(),
                    stat.arguments().toString(),
                    stat.result(),
                    stat.cyclesUsed());
        }
        System.out.println("+---------+---------+-----------+--------+--------+");

        // Calculate additional statistics
        int totalCycles = executionHistory.stream()
                .mapToInt(ExecutionStatisticsDTO::cyclesUsed)
                .sum();
        double avgCycles = (double) totalCycles / executionHistory.size();
        int maxCycles = executionHistory.stream()
                .mapToInt(ExecutionStatisticsDTO::cyclesUsed)
                .max().orElse(0);
        int minCycles = executionHistory.stream()
                .mapToInt(ExecutionStatisticsDTO::cyclesUsed)
                .min().orElse(0);

        System.out.println("\nStatistics Summary:");
        System.out.println("Average cycles per execution: " + String.format("%.2f", avgCycles));
        System.out.println("Maximum cycles in single execution: " + maxCycles);
        System.out.println("Minimum cycles in single execution: " + minCycles);
        System.out.println("Total cycles across all executions: " + totalCycles);
    }

    private void exitSystem()
    {
        System.out.println("Exit From S-EMULATOR");
        scanner.close();
        System.out.println("Success");
    }

    private void printProgramNotLoaded()
    {
        System.out.println("Program is not loaded, please first load a program!");
        System.out.println("Returning to main menu...");
    }
}


