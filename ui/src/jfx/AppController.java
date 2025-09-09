package jfx;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.util.HashMap;
import java.util.Optional;

import dto.engine.ExecutionResultDTO;
import dto.engine.InstructionDTO;
import dto.engine.ProgramDTO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import jfx.cycles.CyclesController;
import jfx.fileHandler.FileHandlerController;
import jfx.instruction.InstructionTableController;
import jfx.program.function.ProgramFunctionController;
import jfx.variables.VariablesController;
import jfx.debugger.DebuggerController;
import system.controller.controller.SystemController;
import jfx.execution.ExecutionVariableController;


import java.io.File;
import java.util.*;

public class AppController {

    // Core system controller - same as console version
    private final SystemController systemController;
    private boolean programLoaded = false;
    private boolean variablesEntered = false;
    private int maxExpandLevel = 0;
    private int currentExpandLevel = 0;
    private ProgramDTO loadedProgram = null;
    private Map<String, String> programVariables = new HashMap<>();
    private List<Integer> runtimeArguments = new ArrayList<>();

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
    @FXML
    private VariablesController variablesController;
    @FXML
    private DebuggerController debugControlsController;
    @FXML
    private AnchorPane executionVariable;
    @FXML
    private ExecutionVariableController executionVariableController;

    public AppController() {
        this.systemController = new SystemController();
    }

    @FXML
    public void initialize() {
        if (fileHandlerController != null && cyclesController != null
                && ProgramFunctionController != null && instructionsTableController != null &&
                derivedInstructionsTableController != null && variablesController != null &&
                debugControlsController != null) {

            fileHandlerController.setAppController(this);
            cyclesController.setNumOfCycles(0);
            ProgramFunctionController.setAppController(this);
            ProgramFunctionController.updateProgramState(false, 0, 0);
            instructionsTableController.setAppController(this);
            instructionsTableController.initializeMainInstructionTable();
            derivedInstructionsTableController.setAppController(this);
            derivedInstructionsTableController.setDerivedMap(true);
            variablesController.clearVariables();
            debugControlsController.setAppController(this);
            executionVariableController.setAppController(this);


            System.out.println("AppController initialized");
        } else {
            System.err.println("One or more controllers are not injected properly!");
            throw new IllegalStateException("FXML injection failed: required controllers are null.");
        }
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
            instructionsTableController.clearInstructions();
            derivedInstructionsTableController.clearInstructions();
            cyclesController.setNumOfCycles(0);

            // Prompt for variables after successful load
            promptForVariables();

        } catch (Exception e) {
            System.err.println("Error loading file: " + e.getMessage());
            e.printStackTrace();
            showError("Error loading file: " + e.getMessage());
        }
    }

    private void promptForVariables() {
        // Clear previous variables
        programVariables.clear();
        runtimeArguments.clear();

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
            Map<String, String> collectedVariables = showMultiVariableDialog(requiredArguments);

            if (collectedVariables != null && collectedVariables.size() == requiredArguments.size()) {
                // All variables were entered successfully
                programVariables.putAll(collectedVariables);

                // Convert to runtime arguments (integers)
                for (String varName : requiredArguments) {
                    String value = collectedVariables.get(varName);
                    try {
                        int intValue = Integer.parseInt(value);
                        if (intValue < 0) {
                            showError("All arguments must be non-negative integers. Got: " + intValue + " for " + varName);
                            return;
                        }
                        runtimeArguments.add(intValue);
                    } catch (NumberFormatException e) {
                        showError("Invalid number format for " + varName + ": " + value);
                        return;
                    }
                }

                variablesEntered = true;
                // Display success message and show variables
                variablesController.showSuccess("Arguments entered successfully!");
                variablesController.setVariables(programVariables);
                showSuccess("Program loaded successfully from: " + loadedProgram.ProgramName() +
                        "\nArguments captured. Ready for execution.");
            } else {
                showError("Variable entry cancelled or incomplete. All arguments are required for execution.");
            }

        } catch (Exception e) {
            System.err.println("Error getting program arguments: " + e.getMessage());
            showError("Error getting program arguments: " + e.getMessage());
        }
    }

    private Map<String, String> showMultiVariableDialog(Set<String> requiredArguments) {
        // Create a custom dialog
        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Program Arguments");
        dialog.setHeaderText("Enter all argument values for program execution");

        // Set the button types
        ButtonType submitButton = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButton, cancelButton);

        // Create the content
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        // Store text fields for each variable
        Map<String, TextField> textFields = new HashMap<>();

        // Create input fields for each required argument
        for (String varName : requiredArguments) {
            Label label = new Label(varName + " (integer value):");
            TextField textField = new TextField();
            textField.setPromptText("Enter integer value...");

            // Add validation styling
            textField.textProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    if (!newValue.trim().isEmpty()) {
                        Integer.parseInt(newValue.trim());
                        textField.setStyle(""); // Clear any error styling
                    }
                } catch (NumberFormatException e) {
                    textField.setStyle("-fx-border-color: red;");
                }
            });

            textFields.put(varName, textField);

            VBox fieldContainer = new VBox(5);
            fieldContainer.getChildren().addAll(label, textField);
            content.getChildren().add(fieldContainer);
        }

        dialog.getDialogPane().setContent(content);

        // Focus on the first text field
        if (!textFields.isEmpty()) {
            Platform.runLater(() -> textFields.values().iterator().next().requestFocus());
        }

        // Enable/disable submit button based on input validation
        Node submitButtonNode = dialog.getDialogPane().lookupButton(submitButton);
        submitButtonNode.setDisable(true);

        // Add listeners to validate all fields
        for (TextField field : textFields.values()) {
            field.textProperty().addListener((observable, oldValue, newValue) -> {
                boolean allValid = textFields.values().stream().allMatch(tf -> {
                    String text = tf.getText().trim();
                    if (text.isEmpty()) return false;
                    try {
                        int value = Integer.parseInt(text);
                        return value >= 0;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                });
                submitButtonNode.setDisable(!allValid);
            });
        }

        // Convert the result when submit is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButton) {
                Map<String, String> result = new HashMap<>();
                boolean allValid = true;

                for (Map.Entry<String, TextField> entry : textFields.entrySet()) {
                    String value = entry.getValue().getText().trim();
                    if (value.isEmpty()) {
                        allValid = false;
                        break;
                    }
                    try {
                        int intValue = Integer.parseInt(value);
                        if (intValue < 0) {
                            allValid = false;
                            break;
                        }
                        result.put(entry.getKey(), value);
                    } catch (NumberFormatException e) {
                        allValid = false;
                        break;
                    }
                }

                return allValid ? result : null;
            }
            return null;
        });

        Optional<Map<String, String>> result = dialog.showAndWait();
        return result.orElse(null);
    }

    public void startRegularExecution() {
        if (!programLoaded) {
            showError("No program loaded. Please load a program first.");
            return;
        }

        if (!variablesEntered) {
            showError("Arguments not entered. Please load a program and enter arguments first.");
            return;
        }

        try {
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
        executionVariableController.showWorkVariables(executionResult.workVariablesValues());

    }

    public void clearLoadedProgram() {
        programLoaded = false;
        variablesEntered = false;
        maxExpandLevel = 0;
        currentExpandLevel = 0;
        loadedProgram = null;
        programVariables.clear();
        runtimeArguments.clear();
        systemController.clearLoadedProgram();

        // Clear UI components
        ProgramFunctionController.updateProgramState(false, 0, 0);
        instructionsTableController.clearInstructions();
        derivedInstructionsTableController.clearInstructions();
        cyclesController.setNumOfCycles(0);
        executionVariableController.showWorkVariables(null);   // blank panel
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

    public boolean areVariablesEntered() {
        return variablesEntered;
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

    public Map<String, String> getProgramVariables() {
        return new HashMap<>(programVariables);
    }

    public List<Integer> getRuntimeArguments() {
        return new ArrayList<>(runtimeArguments);
    }

    // Setters for FXML injection
    public void setCyclesController(CyclesController cyclesController) {
        this.cyclesController = cyclesController;
    }

    public void setFileHandlerController(FileHandlerController fileHandlerController) {
        this.fileHandlerController = fileHandlerController;
    }

    public void displayDerivedFromMap(Map<InstructionDTO, Integer> instructionDTOIntegerMap) {
        derivedInstructionsTableController.setDerivedInstructions(instructionDTOIntegerMap);
    }
}