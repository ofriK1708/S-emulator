package consoleUI;

import backend.engine.ProgramEngine;
import backend.system.generated.SProgram;

import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

public class ConsoleUI {
    private Scanner scanner;
    private SProgram currentProgram;
    private ProgramEngine programEngine;
    private boolean programLoaded;
    // private List<ExecutionRecord> executionHistory;

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
                    // displayStatistics();
                    break;
                case 5:
                    exitSystem();
                    return;
                default:
                    System.out.println("בחירה לא תקינה. אנא בחר מספר בין 1-5.");
            }

            System.out.println(); // רווח בין פעולות
        }
    }

    private void displayMenu() {
        System.out.println("\n=== Main Menu ===");
        System.out.println("1. Load XML File");
        System.out.println("2. Display Loaded Program");
        System.out.println("3. Run Program");
        System.out.println("4. Show History/Statistics");
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

        System.out.println("\n==Display Loaded Program==");
    }

    private void runProgram() {
        System.out.println("\n=== Run Program ===");
    }


    private void displayStatistics() {
        System.out.println("\n=== Statistics ===");
    }

    private void exitSystem() {
        System.out.println("Exit From S-EMULATOR");
        scanner.close();
        System.out.println("Success");
    }
}

