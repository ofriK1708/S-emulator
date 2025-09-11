package ui.jfx.runControls;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import ui.jfx.AppController;

public class RunControlsController {

    @FXML
    private ToggleButton debugType;

    @FXML
    private Button run;

    @FXML
    private ToggleButton runType;

    @FXML
    private ToggleGroup runTypes;

    @FXML
    private Button setRun;

    private AppController appController;

    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    @FXML
    void onSetPress(ActionEvent event) {
        if (appController != null) {
            // Trigger the input variables display
            appController.onSetRunPressed();
        }
    }
    @FXML
    void onResumePress(ActionEvent event) {
        if (appController != null) {
            appController.onResumePressed();
        }
    }

    @FXML
    void onStepOverPress(ActionEvent event) {
        if (appController != null) {
            appController.onStepOverPressed();
        }
    }
    @FXML
    void onStopPress(ActionEvent event) {
        if (appController != null) {
            appController.onStopPressed();
        }
    }
    @FXML
    void onRunPress(ActionEvent event) {
        if (appController != null) {
            appController.onRunPressed();
        }
    }

}
