package ui.jfx.program.function;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import ui.jfx.AppController;

import java.util.Optional;

public class ProgramFunctionController {

    private AppController appController;

    @FXML private Button collapseButton;
    @FXML private Button collapseButton1; // Program\function selector
    @FXML private Label degreeInfoLabel;   // Current / Maximum degree
    @FXML private Button expandButton;
    @FXML private Button changeButton;     // New Change button

    @FXML
    private Button HighSelectionButton;

    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    @FXML
    void initialize() {
        System.out.println("ProgramFunctionController initialized");
        // Start with disabled state until program is loaded
        updateProgramState(false, 0, 0);
    }

    // Collapse to basic program (level 0) - same as console displayLoadedProgram
    @FXML
    void handleCollapse(ActionEvent event) {
       // System.out.println("Collapse button pressed");
        if (appController != null && appController.isProgramLoaded()) {
            int currentLevel = appController.getCurrentExpandLevel();
            int newLevel = currentLevel - 1;

            if (newLevel >= 0) {
                appController.expandProgramToLevel(newLevel);
            } else {
                showInvalidChoice(0, appController.getMaxExpandLevel());
            }
        }
        event.consume();
    }


    // Expand program - same logic as console expandProgram
    @FXML
    void handleExpand(ActionEvent event) {
        System.out.println("Expand button pressed");
        if (appController != null && appController.isProgramLoaded()) {
            int currentLevel = appController.getCurrentExpandLevel();
            int maxLevel = appController.getMaxExpandLevel();
            int newLevel = currentLevel + 1;

            if (newLevel <= maxLevel) {
                appController.expandProgramToLevel(newLevel);
            } else {
                showInvalidChoice(0, maxLevel);
            }
        }
        event.consume();
    }
    // Change: open input dialog for manual level entry
    @FXML
    void handleChange(ActionEvent event) {
        System.out.println("Change button pressed");
        if (changeButton != null) {
            changeButton.setDisable(!appController.isProgramLoaded());
        }
        if (appController != null && appController.isProgramLoaded()) {
            int maxLevel = appController.getMaxExpandLevel();
            int currentLevel = appController.getCurrentExpandLevel();

            // Create input dialog for manual level entry
            TextInputDialog dialog = new TextInputDialog(String.valueOf(currentLevel));
            dialog.setTitle("Change Program Level");
            dialog.setHeaderText("Please enter the expand level you would like to change to:");
            dialog.setContentText("Please enter a number between 0 and " + maxLevel + ":");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                try {
                    int choice = Integer.parseInt(result.get().trim());
                    if (choice >= 0 && choice <= maxLevel) {
                        appController.expandProgramToLevel(choice);
                    } else {
                        showInvalidChoice(0, maxLevel);
                    }
                } catch (NumberFormatException e) {
                    showInvalidChoice(0, maxLevel);
                }
            }
        }
        event.consume();
    }


    // Update UI based on program state and current expand level
    public void updateProgramState(boolean programLoaded, int currentLevel, int maxLevel) {
        System.out.println("Updating program state - Loaded: " + programLoaded +
                ", Current: " + currentLevel + ", Max: " + maxLevel);

        // Update degree info labels with real data
        if (degreeInfoLabel != null) {
            degreeInfoLabel.setText("Current: " + currentLevel + "\nMaximum: " + maxLevel);
        }

        // Enable/disable buttons based on program state
        if (collapseButton != null) {
            collapseButton.setDisable(!programLoaded || currentLevel == 0);
        }

        if (expandButton != null) {
            expandButton.setDisable(!programLoaded || currentLevel == maxLevel || maxLevel == 0);
        }

        if (collapseButton1 != null) {
            collapseButton1.setDisable(!programLoaded);
        }
        if (HighSelectionButton != null) {
            HighSelectionButton.setDisable(!programLoaded);
        }
    }

    private void showInvalidChoice(int min, int max) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Invalid Choice");
        alert.setContentText("Invalid choice. Please enter a number between " + min + " and " + max + ".");
        alert.showAndWait();
    }
}
