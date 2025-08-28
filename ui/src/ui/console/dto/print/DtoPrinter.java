package ui.console.dto.print;

import dto.engine.ExecutionResultDTO;
import dto.engine.ExecutionStatisticsDTO;
import dto.engine.InstructionDTO;
import dto.engine.ProgramDTO;
import ui.console.utils.UIUtils;

import java.util.List;
import java.util.Map;

public class DtoPrinter
{

    public static void printProgram(ProgramDTO program)
    {
        System.out.println("Program Name: " + program.ProgramName());
        System.out.println("Arguments");
        UIUtils.printSortedSet(program.arguments());
        System.out.println("\nLabels:");
        if (program.labels().isEmpty())
        {
            System.out.println("No labels in the program.");
        } else
        {
            UIUtils.printSortedSet(program.labels());
        }

        System.out.println("\nInstructions:");
        List<InstructionDTO> instructions = program.instructions();
        for (int i = 0; i < instructions.size(); i++)
        {
            printInstruction(instructions.get(i), i);
        }
    }

    private static void printInstruction(InstructionDTO instruction, int i)
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

    public static void printExecutionResultDTO(ExecutionResultDTO resultDTO)
    {
        System.out.println("=== Execution Result ===");
        System.out.println("  Result: " + resultDTO.result());
        System.out.println("  Arguments Values: ");
        UIUtils.printSortedMap(resultDTO.argumentsValues());
        System.out.println("  Work Variables Values: ");
        UIUtils.printSortedMap(resultDTO.workVariablesValues());
        System.out.println("  Number of Cycles: " + resultDTO.numOfCycles());
    }

    public static void printExecutionStatisticsDTO(ExecutionStatisticsDTO statsDTO)
    {
        System.out.println("=== Execution Statistics ===");
        System.out.println("  Execution Number: " + statsDTO.executionNumber());
        System.out.println("  Level of Expansion: " + statsDTO.expandLevel());
        System.out.println("  Arguments Values: " + statsDTO.arguments());
        System.out.println("  y = " + statsDTO.result());
        System.out.println("  Number of Cycles: " + statsDTO.cyclesUsed());

    }
}
