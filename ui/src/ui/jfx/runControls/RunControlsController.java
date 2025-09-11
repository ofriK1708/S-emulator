package ui.jfx.runControls;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import ui.jfx.ProgramRunType;

import java.util.function.Consumer;

public class RunControlsController {

    private Consumer<ProgramRunType> runCallback;
    private Runnable setCallback;
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

    public void initComponent(Consumer<ProgramRunType> runCallback, Runnable setCallback) {
        this.runCallback = runCallback;
        this.setCallback = setCallback;
    }

    @FXML
    void onSetPress(ActionEvent event) {
        setCallback.run();
    }

    @FXML
    void onDebugChosen(ActionEvent event) {

    }

    @FXML
    void onRunChosen(ActionEvent event) {

    }

    @FXML
    void run(ActionEvent event) {
        runCallback.accept(ProgramRunType.REGULAR);
    }

}
