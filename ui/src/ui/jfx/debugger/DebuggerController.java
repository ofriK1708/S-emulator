package ui.jfx.debugger;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import jfx.AppController;

public class DebuggerController {

    private AppController appController;

    @FXML
    private Button resume;

    @FXML
    private Button startDebugExecution;

    @FXML
    private Button startRegularExecution;

    @FXML
    private Button stepBackward;

    @FXML
    private Button stepOver;

    @FXML
    private Button stop;

    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    @FXML
    public void initialize() {
        // Set up button actions
        startRegularExecution.setOnAction(event -> handleStartRegularExecution());
        // You can add other button handlers here as needed
    }

    private void handleStartRegularExecution() {
        if (appController != null) {
            appController.startRegularExecution();
        }
    }
}