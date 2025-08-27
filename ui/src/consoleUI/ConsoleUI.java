package consoleUI;

import core.ExecutionStatistics;
import core.ProgramEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ConsoleUI {
    private final Scanner scanner;
    private ProgramDTO currentProgram;
    private ProgramEngine programEngine; // TODO - remove this
    private ExecutionStatisticsDTO currentStatsProgram;
    private final boolean programLoaded; // TODO - remove this
    //private List<ExecutionStatisticsDTO> executionHistory;

    public ConsoleUI() {
        this.scanner = new Scanner(System.in);
        this.programLoaded = false;
        // this.executionHistory = new ArrayList<>();
    }

    public void start() {
        System.out.println("=== S-EMULATOR Console Interface ===");

        while (true) {
            displayMenu();
            int choice = getUserChoice();

            switch (choice) {
                case 1:
                    // loadXMLFile();
                    break;
                case 2:
                    displayLoadedProgram();
                    break;
                case 3:
                    //runProgram();
                    break;
                case 4:
                    // displayStatistics(); // TODO - where is the expand?
                    break;
                case 5:
                    exitSystem();
                    return;
                default:
                    System.out.println("בחירה לא תקינה. אנא בחר מספר בין 1-5."); // TODO - remove hebrew
            }

            System.out.println(); // רווח בין פעולות
        }
    }

    private void displayMenu() {
        System.out.println("\n=== Main Menu ===");
        System.out.println("1. Load XML File");
        System.out.println("2. Display Loaded Program");
        System.out.println("3. Run Program");
        System.out.println("4. Show History/Statistics"); // TODO - where is the expand?
        System.out.println("5. Exit System");
        System.out.print("Choose an option (1-5): ");
    }

    private int getUserChoice() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1; // בחירה לא תקינה
        }
    }

    private void displayLoadedProgram() {
        System.out.println("\n=== Display Loaded Program ===");

        if (!programLoaded || currentProgram == null) {
            System.out.println("No program is currently loaded. Please load an XML file first.");
            return;
        }

        // TODO - change this - no communication with core
        System.out.println("Program Name: " + currentProgram.ProgramName());
        System.out.println("\nArguments:");
        if (currentProgram.arguments().isEmpty()) {
            System.out.println("  No arguments defined");
        } else {
            currentProgram.arguments().forEach((name, value) ->
                    System.out.println("  " + name + " = " + value));
        }

        System.out.println("\nLabels:");
        if (currentProgram.labels().isEmpty()) {
            System.out.println("  No labels defined");
        } else {
            currentProgram.labels().forEach(label ->
                    System.out.println("  " + label));
        }

        System.out.println("\nInstructions:");
        if (currentProgram.instructions().isEmpty()) {
            System.out.println("  No instructions defined");
        } else {
            for (int i = 0; i < currentProgram.instructions().size(); i++) {
                InstructionDTO instruction = currentProgram.instructions().get(i);
                System.out.printf("  %d. [%s] %s %s (cycles: %d)%n",
                        i + 1,
                        instruction.type(),
                        instruction.label() != null ? instruction.label() + ":" : "",
                        instruction.command(),
                        instruction.cycles());
            }
        }
    }

    private void runProgram() {
        System.out.println("\n=== Run Program ===");

        if (!programLoaded || currentProgram == null) {
            System.out.println("No program is currently loaded. Please load an XML file first.");
            return;
        }

        System.out.println("Running program: " + currentProgram.ProgramName());

        // Get arguments from user if program requires them
        List<Integer> runtimeArguments = new ArrayList<>();
        if (!currentProgram.arguments().isEmpty()) {
            System.out.println("\nProgram arguments required:");
            for (Map.Entry<String, Integer> arg : currentProgram.arguments().entrySet()) {
                System.out.print("Enter value for " + arg.getKey() +
                        " (default: " + arg.getValue() + "): ");
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    runtimeArguments.add(arg.getValue());
                } else {
                    try {
                        runtimeArguments.add(Integer.parseInt(input));
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input, using default value: " + arg.getValue());
                        runtimeArguments.add(arg.getValue());
                    }
                }
            }
        }

        try {
            System.out.println("\nExecuting program...");

            // Simulate program execution
            ExecutionStatisticsDTO result = executeProgram(runtimeArguments);

            System.out.println("✓ Program execution completed!");
            System.out.println("Result: " + result.result());
            System.out.println("Cycles used: " + result.cyclesUsed());
            System.out.println("Expansion level: " + result.levelOfExpansion());

            // Add to history
            executionHistory.add(result);

        } catch (Exception e) {
            System.out.println("Error during program execution: " + e.getMessage());
        }
    }

    private void displayStatistics() {
        System.out.println("\n=== Statistics ===");

        if (executionHistory.isEmpty()) {
            System.out.println("No execution history available. Run a program first.");
            return;
        }

        System.out.println("Total executions: " + executionHistory.size());
        System.out.println("\nExecution History:");
        System.out.println("+---------+---------+-----------+--------+--------+");
        System.out.println("| Exec #  | Level   | Arguments | Result | Cycles |");
        System.out.println("+---------+---------+-----------+--------+--------+");

        for (ExecutionStatisticsDTO stat : executionHistory) {
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

    private void exitSystem() {
        System.out.println("Exit From S-EMULATOR");
        scanner.close();
        System.out.println("Success");
    }
}


