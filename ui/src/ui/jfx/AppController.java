package ui.jfx;

import dto.engine.InstructionDTO;
import dto.engine.ProgramDTO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import ui.jfx.cycles.CyclesController;
import ui.jfx.fileHandler.FileHandlerController;
import ui.jfx.instruction.InstructionTableController;
import ui.jfx.program.function.ProgramFunctionController;
import system.controller.controller.SystemController;

import java.io.File;
import java.util.List;

public class AppController {

    // Core system controller - same as console version
    private final SystemController systemController;
    private boolean programLoaded = false;
    private int maxExpandLevel = 0;
    private int currentExpandLevel = 0;
    private ProgramDTO loadedProgram = null;

    @FXML
    private HBox fileHandler;
    @FXML
    private FileHandlerController fileHandlerController;
    @FXML
    private AnchorPane cycles;
    @FXML
    private CyclesController cyclesController;
    @FXML
    private ProgramFunctionController ProgramFunctionController;
    @FXML
    private AnchorPane instructionsTable;
    @FXML
    private InstructionTableController instructionsTableController;
    @FXML
    private AnchorPane derivedInstructionsTable;
    @FXML
    private InstructionTableController derivedInstructionsTableController;


    public AppController() {
        this.systemController = new SystemController();
    }

    @FXML
    public void initialize() {

        if (fileHandlerController != null && cyclesController != null
                && ProgramFunctionController != null && instructionsTableController != null &&
                derivedInstructionsTableController != null) {
            fileHandlerController.setAppController(this);
            cyclesController.setNumOfCycles(0);
            ProgramFunctionController.setAppController(this);
            ProgramFunctionController.updateProgramState(false, 0, 0);
            instructionsTableController.setAppController(this);
            instructionsTableController.initializeMainInstructionTable();
            derivedInstructionsTableController.setAppController(this);
            derivedInstructionsTableController.setDerivedMap(true);
            System.out.println("AppController initialized");
        } else {
            System.err.println("One or more controllers are not injected properly!");
            throw new IllegalStateException("FXML injection failed: required controllers are null.");
        }

    }

    // Load program from file (same logic as console loadXMLFile)
    public void loadProgramFromFile(File file) {
        try {
            if (programLoaded) {
                clearLoadedProgram();
            }
            System.out.println("Loading program from: " + file.getAbsolutePath());
            systemController.LoadProgramFromFile(file.toPath());
            loadedProgram = systemController.getBasicProgram();
            instructionsTableController.setInstructions(loadedProgram.instructions());
            cyclesController.setNumOfCycles(systemController.getCyclesCount(0));

            // Update program state (same as console)
            programLoaded = true;
            maxExpandLevel = systemController.getMaxExpandLevel();
            currentExpandLevel = 0; // Start with basic program

            System.out.println("Program loaded successfully. MaxExpandLevel: " + maxExpandLevel);

            // Update ProgramFunction component with real data
            ProgramFunctionController.updateProgramState(true, currentExpandLevel, maxExpandLevel);


            showSuccess("Program loaded successfully from: " + file.getName());

        } catch (Exception e) {
            System.err.println("Error loading file: " + e.getMessage());
            e.printStackTrace();
            showError("Error loading file: " + e.getMessage());
        }
    }

    public void clearLoadedProgram() {
        programLoaded = false;
        maxExpandLevel = 0;
        currentExpandLevel = 0;
        loadedProgram = null;
        systemController.clearLoadedProgram();

        // Clear UI components

        ProgramFunctionController.updateProgramState(false, 0, 0);

        instructionsTableController.clearInstructions();

        derivedInstructionsTableController.clearInstructions();

        cyclesController.setNumOfCycles(0);


        showInfo("Loaded program cleared.");
    }

    // Expand program to specific level (same logic as console expandProgram)
    public void expandProgramToLevel(int level) {
        if (!programLoaded) {
            showError("No program loaded. Please load a program first.");
            return;
        }

        if (level < 0 || level > maxExpandLevel) {
            showError("Invalid expand level. Must be between 0 and " + maxExpandLevel);
            return;
        }

        try {
            currentExpandLevel = level;
            System.out.println("Program expanded to level: " + currentExpandLevel);

            // Get the program at this expand level (same as console)
            ProgramDTO program = systemController.getProgramByExpandLevel(currentExpandLevel);
            System.out.println("Program at level " + level + " has " + program.instructions().size() + " instructions");

            // Update UI components with new expand level
            ProgramFunctionController.updateProgramState(true, currentExpandLevel, maxExpandLevel);
            derivedInstructionsTableController.clearInstructions();

            // Update instruction table and cycles
            instructionsTableController.setInstructions(program.instructions());
            cyclesController.setNumOfCycles(systemController.getCyclesCount(currentExpandLevel));


            showInfo("Program expanded to level " + level);

        } catch (Exception e) {
            System.err.println("Error expanding program: " + e.getMessage());
            showError("Error expanding program: " + e.getMessage());
        }
    }

    // Get basic program (expand level 0)
    public void displayBasicProgram() {
        if (!programLoaded) {
            showError("No program loaded. Please load a program first.");
            return;
        }
        expandProgramToLevel(0);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Getters for other components
    public boolean isProgramLoaded() {
        return programLoaded;
    }

    public int getCurrentExpandLevel() {
        return currentExpandLevel;
    }

    public int getMaxExpandLevel() {
        return maxExpandLevel;
    }

    public SystemController getSystemController() {
        return systemController;
    }

    // Setters for FXML injection
    public void setCyclesController(CyclesController cyclesController) {
        this.cyclesController = cyclesController;
    }

    public void setFileHandlerController(FileHandlerController fileHandlerController) {
        this.fileHandlerController = fileHandlerController;
    }

    public void displayDerivedFromMap(List<InstructionDTO> instructionDTOIntegerMap) {
        derivedInstructionsTableController.setDerivedInstructions(instructionDTOIntegerMap);
    }
}