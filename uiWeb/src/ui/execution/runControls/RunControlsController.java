package ui.execution.runControls;

import engine.utils.ArchitectureType;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ui.execution.ProgramRunType;

import java.util.function.BiConsumer;

public class RunControlsController {

    @FXML
    public Button run;

    final BooleanProperty runTypeChosen = new SimpleBooleanProperty(false);

    @FXML
    private ToggleButton debugType;

    @FXML
    private ToggleButton runType;

    @FXML
    private ToggleGroup runTypes;

    @FXML
    private Button setRun;
    final BooleanProperty archTypeChosen = new SimpleBooleanProperty(false);
    private final @NotNull BooleanProperty debugModeActive = new SimpleBooleanProperty(false);
    @FXML
    public ToggleGroup ArchType;
    @Nullable ProgramRunType programRunType;
    @FXML
    private ToggleButton archI;
    @FXML
    private ToggleButton archII;
    @FXML
    private ToggleButton archIII;
    @FXML
    private ToggleButton archIV;
    private BiConsumer<ProgramRunType, ArchitectureType> runCallback;
    private Runnable setCallback;
    private @NotNull ArchitectureType architectureType = ArchitectureType.ARCHITECTURE_I;

    public void initComponent(BiConsumer<ProgramRunType, ArchitectureType> runCallback, Runnable setCallback,
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
        run.disableProperty().bind(runTypeChosen.not().or(archTypeChosen.not()));
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
        runCallback.accept(programRunType, architectureType);
    }

    public void selectArchI(ActionEvent actionEvent) {
        mangeArchSelection(ArchitectureType.ARCHITECTURE_I, archI);
    }

    public void selectArchII(ActionEvent actionEvent) {
        mangeArchSelection(ArchitectureType.ARCHITECTURE_II, archII);
    }

    public void selectArchIII(ActionEvent actionEvent) {
        mangeArchSelection(ArchitectureType.ARCHITECTURE_III, archIII);
    }

    public void selectArchIV(ActionEvent actionEvent) {
        mangeArchSelection(ArchitectureType.ARCHITECTURE_IV, archIV);
    }

    public void mangeArchSelection(ArchitectureType architectureType, ToggleButton toggleButton) {
        if (toggleButton.isSelected()) {
            this.architectureType = architectureType;
            archTypeChosen.set(true);
        } else {
            archTypeChosen.set(false);
        }
    }

}
