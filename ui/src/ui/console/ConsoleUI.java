package ui.console;

import dto.engine.ExecutionResultDTO;
import dto.engine.ExecutionStatisticsDTO;
import dto.engine.ProgramDTO;
import system.controller.controller.SystemController;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import static ui.console.dto.print.DtoPrinter.*;
import static ui.console.utils.UIUtils.*;

public class ConsoleUI
{
    private final Scanner scanner;
    private final SystemController controller;
    private int maxExpandLevel = 0;
    private boolean programLoaded = false;

    public ConsoleUI()
    {
        scanner = new Scanner(System.in);
        controller = new SystemController();
    }

    public void start()
    {
        System.out.println("=== S-EMULATOR Console Interface ===");

        while (true)
        {
            displayMenu();
            int numOfMenuOptions = 6;
            int choice = getUserChoice(numOfMenuOptions);

            switch (choice)
            {
                case 1:
                    loadXMLFile();
                    break;
                case 2:
                    displayLoadedProgram();
                    break;
                case 3:
                    runProgram();
                    break;
                case 4:
                    expandProgram();
                    break;
                case 5:
                    displayStatistics();
                    break;
                case 6:
                    exitSystem();
                    return;
            }

            System.out.println();
        }
    }

    private void displayMenu()
    {
        System.out.println("=== Main Menu ===");
        System.out.println("1. Load XML File");
        System.out.println("2. Display Loaded Program");
        System.out.println("3. Run Program");
        System.out.println("4. Expand Program");
        System.out.println("5. Show History/Statistics");
        System.out.println("6. Exit System");
    }

    private void loadXMLFile()
    {
        Path filePath = null;
        try
        {
            System.out.println("Please enter a full path for the xml file:");
            filePath = Path.of(scanner.nextLine());
            controller.LoadProgramFromFile(filePath);
            System.out.println("The program has been loaded successfully.");
            maxExpandLevel = controller.getMaxExpandLevel();
            programLoaded = true;

        } catch (Exception e)
        {
            System.out.println("Error loading file " + Optional.ofNullable(filePath)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .orElse("no such file"));
            System.out.println(e.getMessage());
            System.out.println("Please try fixing the file or choose another file");
            System.out.println("Returning to main menu...");
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
        printProgram(controller.getBasicProgram());
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
            int expandLevel = getExpandLevelChoiceFromUser(maxExpandLevel);
            printProgram(controller.getProgramByExpandLevel(expandLevel));
        } catch (Exception e)
        {
            System.out.println("Error loading program " + e.getMessage());
            System.out.println("returning to main menu...");
        }
    }

    private void runProgram()
    {
        if (!programLoaded)
        {
            printProgramNotLoaded();
            return;
        }
        int expandLevel = getExpandLevelChoiceFromUser(maxExpandLevel);
        List<Integer> runtimeArguments = new ArrayList<>();
        getUserArguments(controller.getProgramArgsNames(), runtimeArguments);
        try
        {
            System.out.println("\nTrying executing program in expand level " + expandLevel);
            ExecutionResultDTO executionResult = controller.runLoadedProgram(expandLevel, runtimeArguments);
            ProgramDTO program = controller.getProgramByExpandLevel(expandLevel);
            System.out.println("Program finished executing successfully.");
            printProgram(program);
            printExecutionResultDTO(executionResult);

        } catch (Exception e)
        {
            System.out.println("Error executing program: " + e.getMessage());
            System.out.println("returning to main menu...");
        }
    }



    private void displayStatistics()
    {
        if (!programLoaded)
        {
            printProgramNotLoaded();
            return;
        }
        List<ExecutionStatisticsDTO> statisticsList = controller.getAllExecutionStatistics();
        if (statisticsList.isEmpty())
        {
            System.out.println("No execution statistics available.");
            System.out.println("Make sure to run the program at least once.");
            System.out.println("Returning to main menu...");
            return;
        }
        System.out.println("=== Execution Statistics History ===");
        for (ExecutionStatisticsDTO stats : statisticsList)
        {
            printExecutionStatisticsDTO(stats);
            System.out.println();
        }
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


