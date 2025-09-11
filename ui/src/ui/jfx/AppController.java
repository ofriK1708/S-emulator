package ui.jfx;

import dto.engine.ExecutionResultDTO;
import dto.engine.InstructionDTO;
import dto.engine.ProgramDTO;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import system.controller.controller.SystemController;
import ui.jfx.VariableInputDialog.VariableInputDialogController;
import ui.jfx.cycles.CyclesController;
import ui.jfx.debugger.DebuggerController;
import ui.jfx.execution.ExecutionVariableController;
import ui.jfx.fileHandler.FileHandlerController;
import ui.jfx.instruction.InstructionTableController;
import ui.jfx.program.function.ProgramFunctionController;
import ui.jfx.runControls.RunControlsController;
import ui.jfx.variables.VariablesController;
import ui.utils.UIUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AppController {

    // Core system controller - same as console version
    private final SystemController systemController;
    private boolean programLoaded = false;
    private boolean variablesEntered = false;
    private int maxExpandLevel = 0;
    private int currentExpandLevel = 0;
    private ProgramDTO loadedProgram = null;
    private final Map<String, Integer> programArguments = new HashMap<>();
    private Map<String, Integer> programVariablesAfterExecution = new HashMap<>();

    @FXML
    private HBox fileHandler;
    @FXML
    private FileHandlerController fileHandlerController;
    @FXML
    private HBox cycles;
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
    @FXML
    private VariablesController variablesController;
    @FXML
    private DebuggerController debugControlsController;
    @FXML
    private AnchorPane executionVariable;
    @FXML
    private ExecutionVariableController executionVariableController;
    @FXML
    private VBox runControls;
    @FXML
    private RunControlsController runControlsController;
    @FXML
    private TitledPane runControlsTitledPane;
    @FXML
    private TitledPane debugControlsTitledPane;
    @FXML
    private TitledPane variablesTitledPane;
    @FXML
    private TitledPane staticsTitledPane;

    private BooleanProperty programLoadedProperty = new SimpleBooleanProperty(false);
    private BooleanProperty variablesEnteredProperty = new SimpleBooleanProperty(false);
    private BooleanProperty debugModeProperty = new SimpleBooleanProperty(false);
    private BooleanProperty canRunProgramProperty = new SimpleBooleanProperty(false);
    private BooleanProperty programRanAtLeastOnceProperty = new SimpleBooleanProperty(false);
    private BooleanProperty programRunningProperty = new SimpleBooleanProperty(false);
    private BooleanProperty programFinishedProperty = new SimpleBooleanProperty(false);
    private IntegerProperty maxExpandLevelProperty = new SimpleIntegerProperty(0);
    private IntegerProperty currentExpandLevelProperty = new SimpleIntegerProperty(0);
    private IntegerProperty currentCyclesProperty = new SimpleIntegerProperty(0);

    public AppController() {
        this.systemController = new SystemController();
    }

    @FXML
    public void initialize() {
        if (fileHandlerController != null && cyclesController != null
                && ProgramFunctionController != null && instructionsTableController != null &&
                derivedInstructionsTableController != null && variablesController != null &&
                debugControlsController != null && executionVariableController != null &&
                runControlsController != null) {

            fileHandlerController.initComponent(this::loadProgramFromFile, this::clearLoadedProgram);
            cyclesController.setNumOfCycles(0);
            ProgramFunctionController.setAppController(this);
            ProgramFunctionController.updateProgramState(false, 0, 0);
            instructionsTableController.setAppController(this);
            instructionsTableController.initializeMainInstructionTable();
            derivedInstructionsTableController.setAppController(this);
            derivedInstructionsTableController.setDerivedMap(true);
            variablesController.clearVariables();
            debugControlsController.setAppController(this);
            runControlsController.initComponent(this::RunProgram, this::promptForVariables);


            System.out.println("AppController initialized");
        } else {
            System.err.println("One or more controllers are not injected properly!");
            throw new IllegalStateException("FXML injection failed: required controllers are null.");
        }
    }

    private void bindProperties() {
        // Bind properties to enable/disable UI components based on program state
        runControlsTitledPane.expandedProperty().bind(
                Bindings.and(programLoadedProperty, programRunningProperty.not()));

        debugControlsTitledPane.expandedProperty().bind(
                Bindings.and(programLoadedProperty, Bindings.and(debugModeProperty, programFinishedProperty.not())));

        variablesTitledPane.expandedProperty().bind(
                Bindings.and(programLoadedProperty, Bindings.or(programFinishedProperty, debugModeProperty)));

        staticsTitledPane.expandedProperty().bind(
                Bindings.and(programLoadedProperty, Bindings.and(programRunningProperty.not(), programRanAtLeastOnceProperty)));

    }

    // Load program from file - DO NOT populate UI tables yet
    public void loadProgramFromFile(File file) {
        try {
            if (programLoaded) {
                clearLoadedProgram();
            }
            System.out.println("Loading program from: " + file.getAbsolutePath());
            systemController.LoadProgramFromFile(file.toPath());

            // Get basic program info but don't display instructions yet
            loadedProgram = systemController.getBasicProgram();

            // Update program state
            programLoaded = true;
            variablesEntered = false;
            maxExpandLevel = systemController.getMaxExpandLevel();
            currentExpandLevel = 0;

            System.out.println("Program loaded successfully. MaxExpandLevel: " + maxExpandLevel);

            // Update ProgramFunction component with real data
            ProgramFunctionController.updateProgramState(true, currentExpandLevel, maxExpandLevel);

            // Clear any existing UI data
            instructionsTableController.setInstructions(loadedProgram.instructions());
            derivedInstructionsTableController.clearInstructions();
            cyclesController.setNumOfCycles(0);

        } catch (Exception e) {
            System.err.println("Error loading file: " + e.getMessage());
            e.printStackTrace();
            showError("Error loading file: " + e.getMessage());
        }
    }

    public void RunProgram(ProgramRunType programRunType) {
        if (!programLoaded) {
            showError("No program loaded. Please load a program first.");
            return;
        }
        if (!variablesEntered) {
            showError("Arguments not entered. Please load a program and enter arguments first.");
            return;
        }
        switch (programRunType) {
            case REGULAR:
                startRegularExecution();
                break;
            case DEBUG:
                startDebugExecution();
                break;
            default:
                showError("Unknown run type selected.");
        }
    }

    private void startDebugExecution() {
    }

    public void promptForVariables() {
        // Clear previous variables
        programArguments.clear();

        try {
            // Get required program arguments from SystemController
            Set<String> requiredArguments = systemController.getProgramArgsNames();

            if (requiredArguments.isEmpty()) {
                // No arguments needed, mark as entered
                variablesEntered = true;
                variablesController.showSuccess("No variables required for this program");
                showSuccess("Program loaded successfully from: " + loadedProgram.ProgramName() +
                        "\nNo variables required. Ready for execution.");
                return;
            }

            // Create and show the multi-variable input dialog
            showMultiVariableDialog(requiredArguments);
            variablesEntered = true;
            // Display success message and show variables
            variablesController.showSuccess("Arguments entered successfully!");
            variablesController.setVariables(programArguments);
            showSuccess("Program loaded successfully from: " + loadedProgram.ProgramName() +
                    "\nArguments captured. Ready for execution.");

        } catch (Exception e) {
            System.err.println("Error getting program arguments: " + e.getMessage());
            showError("Error getting program arguments: " + e.getMessage());
        }
    }

    public void showMultiVariableDialog(Set<String> requiredArguments) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/jfx/VariableInputDialog/VariableInputDialog.fxml"));
            Parent root = loader.load();
            VariableInputDialogController controller = loader.getController();
            controller.initialiseController(requiredArguments, programArguments);

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle("Program Arguments");
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startRegularExecution() {
        try {
            List<Integer> runtimeArguments = UIUtils.getRuntimeArgument(programArguments);
            System.out.println("Starting regular execution with arguments: " + runtimeArguments);

            // Execute the program using SystemController
            ExecutionResultDTO executionResult = systemController.runLoadedProgram(currentExpandLevel, runtimeArguments);

            // Get the updated program state after execution
            ProgramDTO executedProgram = systemController.getProgramByExpandLevel(currentExpandLevel);

            // Update UI components with execution results
            instructionsTableController.setInstructions(executedProgram.instructions());
            cyclesController.setNumOfCycles(systemController.getCyclesCount(currentExpandLevel));

            // Show execution results
            showExecutionResults(executionResult);

            showSuccess("Program executed successfully!\n" +
                    "Cycles: " + executionResult.numOfCycles() + "\n" +
                    "Final memory state updated in instruction table.");

        } catch (IllegalArgumentException e) {
            System.err.println("Invalid arguments for execution: " + e.getMessage());
            showError("Invalid arguments for execution: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error during execution: " + e.getMessage());
            showError("Error during execution: " + e.getMessage());
        }
    }

    private void showExecutionResults(ExecutionResultDTO executionResult) {
        // You can create a dedicated component to show execution results
        // For now, we'll update the cycles and show basic info
        System.out.println("=== Execution Results ===");
        System.out.println("Total Cycles: " + executionResult.numOfCycles());
        // Update cycles display
        cyclesController.setNumOfCycles(executionResult.numOfCycles());
        executionVariableController.showWorkVariables(
                executionResult.result(),
                executionResult.argumentsValues(),
                executionResult.workVariablesValues());

    }

    public void clearLoadedProgram() {
        programLoaded = false;
        variablesEntered = false;
        maxExpandLevel = 0;
        currentExpandLevel = 0;
        loadedProgram = null;
        programArguments.clear();
        systemController.clearLoadedProgram();

        // Clear UI components
        ProgramFunctionController.updateProgramState(false, 0, 0);
        instructionsTableController.clearInstructions();
        derivedInstructionsTableController.clearInstructions();
        cyclesController.setNumOfCycles(0);
        executionVariableController.clearVariables();
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

            // Only update instruction table if program has been executed
            // or if user explicitly wants to see the expanded program structure
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

    public int getMaxExpandLevel() {
        return maxExpandLevel;
    }


    public void displayDerivedFromMap(List<InstructionDTO> instructionDTOIntegerMap) {
        derivedInstructionsTableController.setDerivedInstructions(instructionDTOIntegerMap);
    }

    public int getCurrentExpandLevel() {
        return currentExpandLevel;
    }

}