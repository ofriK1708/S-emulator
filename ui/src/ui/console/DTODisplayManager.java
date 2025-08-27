package ui.console;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DTODisplayManager
{

    private final List<DisplayObserver> observers;
    public DTODisplayManager() {
        this.observers = new ArrayList<>();
    }

    public void displayInstructions(List<InstructionDTO> instructions)
    {
        if (instructions == null || instructions.isEmpty())
        {
            displayMessage("");
            return;
        }

        // Sort
        List<InstructionDTO> sortedInstructions = instructions.stream()
                .sorted(Comparator.comparingInt(InstructionDTO::InstructionNumber))
                .collect(Collectors.toList());

        displayHeader("Program Instructions", sortedInstructions.size());
        displayInstructions(sortedInstructions);
        notifyObservers("Instructions displayed: " + sortedInstructions.size());

    }

    private void displayInstructions(List<InstructionDTO> instructions) {
        for (InstructionDTO instruction : instructions) {
            System.out.printf("[%03d] %s: %s (%d cycles)%n",
                   // instruction.InstructionNumber(), TODO - ITERATE
                    instruction.type(),
                    instruction.command(),
                    instruction.cycles());
        }
    }
    public void displayExecutionStatistics(List<ExecutionStatisticsDTO> statistics) {
        if (statistics == null || statistics.isEmpty()) {
            displayMessage("No execution data to display");
            return;
        }

        displayHeader("Execution Statistics", statistics.size());

        for (ExecutionStatisticsDTO stat : statistics) {
            displayExecutionStat(stat);
        }

        displaySummaryStatistics(statistics);
        notifyObservers("Execution statistics displayed: " + statistics.size());
    }

    private void displaySummaryStatistics(List<ExecutionStatisticsDTO> statistics) {
        int totalCycles = statistics.cyclesUsed;
        int totalExecutions = statistics.size();

        System.out.println();
        System.out.println("┌─── Execution Summary ───┐");
        System.out.printf("│ Total Executions: %6d │%n", totalExecutions);
        System.out.printf("│ Total Cycles:     %6d │%n", totalCycles);
        System.out.println("└─────────────────┘");
    }


    private void displayHeader(String title, int count) {
        String headerLine = "═".repeat(50);
        System.out.println(headerLine);
        System.out.println();
    }


    private void displayMessage(String message) {
        System.out.println("💡 " + message);
    }

}
