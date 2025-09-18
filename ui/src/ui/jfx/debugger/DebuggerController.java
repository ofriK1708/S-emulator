package ui.jfx.debugger;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import ui.jfx.AppController;
import ui.utils.UIUtils;


public class DebuggerController {
    private AppController appController;

    @FXML private Button resume;
    @FXML private Button startDebugExecution;
    @FXML private Button startRegularExecution;
    @FXML private Button stepBackward;
    @FXML private Button stepOver;
    @FXML private Button stop;
    private boolean debugSessionActive = false;

    @FXML
    private void initialize() {
        System.out.println("DebuggerController initialized.");
        updateExecutionState(false, false);
    }

    public void setAppController(AppController appController) {
        this.appController = appController;
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

    // Start debug execution

    @FXML
    private void handleStartDebugExecution() {
        if (appController == null) {
            return;
        }
        if (!appController.isProgramLoaded()) {
            showErrorAlert();
            return;
        }

        appController.startDebugExecution();
    }
    //start step over
    @FXML
    private void handleStepOver() {
        if (appController == null || !debugSessionActive) {
            return;
        }
        appController.debugStep();
    }
    //start step backward
    @FXML
    private void handleStepBackward() {
        if (appController == null || !debugSessionActive) {
            return;
        }
        appController.debugStepBackward();
    }
    // Resume normal execution
    @FXML
    private void handleResume() {
        if (appController == null || !debugSessionActive) {
            return;
        }
        appController.debugResume();
        debugSessionActive = false;
        updateExecutionState(false, true);
    }
    // Stop execution
    @FXML
    private void handleStop() {
        if (appController == null || !debugSessionActive) {
            return;
        }
        appController.stopDebugSession();
        debugSessionActive = false;
        updateExecutionState(false, true);
    }

    public void updateExecutionState(boolean isExecuting, boolean programLoaded) {
        // Start buttons enabled only when not executing and program is loaded
        if (startRegularExecution != null) {
            startRegularExecution.setDisable(!programLoaded || isExecuting);
        }
        if (startDebugExecution != null) {
            startDebugExecution.setDisable(!programLoaded || isExecuting);
        }
        // Debug controls enabled only when debug session is active
        if (stop != null) {
            stop.setDisable(!debugSessionActive);
        }
        if (resume != null) {
            resume.setDisable(!debugSessionActive);
        }
        if (stepOver != null) {
            stepOver.setDisable(!debugSessionActive);
        }
        if (stepBackward != null) {
            stepBackward.setDisable(!debugSessionActive);
        }
    }

    private void showErrorAlert() {
        UIUtils.showError("No program loaded. Please load a program first.");
    }

    // Get arguments from user (same logic as console getUserArguments) - placeholder
    private void getArgumentsFromUser() {
        // Not implemented in this change set
    }
    // Add this method to debugger controller to notify when debug session start and end
    public void notifyDebugSessionEnded() {
        debugSessionActive = false;
        updateExecutionState(false, appController != null && appController.isProgramLoaded());
    }
    public void notifyDebugSessionStarted() {
        debugSessionActive = true;
        updateExecutionState(true, true);
    }

}