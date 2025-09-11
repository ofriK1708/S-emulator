package ui.jfx.runControls;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

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

    @FXML
    void onSetPress(ActionEvent event) {

    }

}
