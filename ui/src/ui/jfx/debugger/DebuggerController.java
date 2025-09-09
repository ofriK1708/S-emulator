package ui.jfx.debugger;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import ui.jfx.AppController;



public class DebuggerController {
    private AppController appController;

    @FXML private Button resume;
    @FXML private Button startDebugExecution;
    @FXML private Button startRegularExecution;
    @FXML private Button stepBackward;
    @FXML private Button stepOver;
    @FXML private Button stop;

    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    @FXML
    private void initialize() {
        System.out.println("DebuggerController initialized.");
        updateExecutionState(false, false);
    }

    // Start regular execution (populate instructions and cycles now)
    @FXML
    private void handleStartRegularExecution() {
        if (appController == null) {
            return;
        }
        if (!appController.isProgramLoaded()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("No program loaded. Please load a program first.");
            alert.showAndWait();
            return;
        }
        appController.startRegularExecution();
        updateExecutionState(true, true);
    }

    // Start debug execution (reserved for future use)
    @FXML
    private void handleStartDebugExecution() {
        // Not implemented in this change set
    }

    // Stop execution
    @FXML
    private void handleStop() {
        // Not implemented in this change set
        updateExecutionState(false, appController != null && appController.isProgramLoaded());
    }

    // Resume execution (for future step debugging)
    @FXML
    private void handleResume() {
        // Not implemented in this change set
    }

    // Step over (for future step debugging)
    @FXML
    private void handleStepOver() {
        // Not implemented in this change set
    }

    // Step backward (for future step debugging)
    @FXML
    private void handleStepBackward() {
        // Not implemented in this change set
    }

    // Update button states based on execution state
    public void updateExecutionState(boolean isExecuting, boolean programLoaded) {
        // Start buttons enabled only when not executing and program is loaded
        if (startRegularExecution != null) {
            startRegularExecution.setDisable(!programLoaded || isExecuting);
        }
        if (startDebugExecution != null) {
            startDebugExecution.setDisable(!programLoaded || isExecuting);
        }
        // Stop enabled only when executing
        if (stop != null) {
            stop.setDisable(!isExecuting);
        }
        // Step/Resume enabled only when executing (placeholders)
        if (resume != null) {
            resume.setDisable(!isExecuting);
        }
        if (stepOver != null) {
            stepOver.setDisable(!isExecuting);
        }
        if (stepBackward != null) {
            stepBackward.setDisable(!isExecuting);
        }
    }

    // Get arguments from user (same logic as console getUserArguments) - placeholder
    private void getArgumentsFromUser() {
        // Not implemented in this change set
    }
}