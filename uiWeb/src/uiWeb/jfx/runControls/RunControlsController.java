package uiweb.jfx.runControls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uiweb.jfx.ProgramRunType;

import java.util.function.Consumer;

public class RunControlsController {

    final BooleanProperty runTypeChosen = new SimpleBooleanProperty(false);
    private final @NotNull BooleanProperty debugModeActive = new SimpleBooleanProperty(false);
    @FXML
    public Button run;
    @Nullable ProgramRunType programRunType;
    private Consumer<ProgramRunType> runCallback;
    private Runnable setCallback;
    @FXML
    private ToggleButton debugType;
    @FXML
    private ToggleButton runType;

    @FXML
    private ToggleGroup runTypes;

    @FXML
    private Button setRun;

    public void initComponent(Consumer<ProgramRunType> runCallback, Runnable setCallback,
                              @NotNull BooleanProperty programLoaded, @NotNull BooleanProperty variablesEntered) {
        this.runCallback = runCallback;
        this.setCallback = setCallback;

        setRun.disableProperty().bind(programLoaded.not());
        runType.disableProperty().bind(programLoaded.not().or(variablesEntered.not()));
        debugType.disableProperty().bind(programLoaded.not().or(variablesEntered.not()));
        variablesEntered.addListener((change, oldValue, newValue) -> {
            if (!newValue) {
                runTypeChosen.set(false);
                programRunType = null;
                debugModeActive.set(false);
                runTypes.selectToggle(null);
            }
        });
        run.disableProperty().bind(runTypeChosen.not());
    }

    @FXML
    void onSetPress(ActionEvent event) {
        setCallback.run();
    }

    // Change debug chosen handler:
    @FXML
    void onDebugChosen(ActionEvent event) {
        if (debugType.isSelected()) {
            runTypeChosen.set(true);
            programRunType = ProgramRunType.DEBUG;
            debugModeActive.set(true);
        } else {
            runTypeChosen.set(false);
            programRunType = null;
            debugModeActive.set(false);
        }
    }


    @FXML
    void onRunChosen(ActionEvent event) {
        if (runType.isSelected()) {
            runTypeChosen.set(true);
            programRunType = ProgramRunType.REGULAR;
        } else {
            runTypeChosen.set(false);
            programRunType = null;
        }
    }

    @FXML
    void run(ActionEvent event) {
        runCallback.accept(programRunType);
    }

}
