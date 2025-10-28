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
    public ToggleGroup archType;
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
                              @NotNull BooleanProperty programLoaded, @NotNull BooleanProperty argumentsEntered) {
        this.runCallback = runCallback;
        this.setCallback = setCallback;

        setRun.disableProperty().bind(programLoaded.not());
        runType.disableProperty().bind(programLoaded.not().or(argumentsEntered.not()));
        debugType.disableProperty().bind(programLoaded.not().or(argumentsEntered.not()));
        argumentsEntered.addListener((change, oldValue, newValue) -> {
            if (!newValue) {
                runTypeChosen.set(false);
                programRunType = null;
                debugModeActive.set(false);
                runTypes.selectToggle(null);
                archType.selectToggle(null);
            }
        });
        archI.disableProperty().bind(programLoaded.not().or(argumentsEntered.not()));
        archII.disableProperty().bind(programLoaded.not().or(argumentsEntered.not()));
        archIII.disableProperty().bind(programLoaded.not().or(argumentsEntered.not()));
        archIV.disableProperty().bind(programLoaded.not().or(argumentsEntered.not()));
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

    @FXML
    public void selectArchI(ActionEvent actionEvent) {
        mangeArchSelection(ArchitectureType.ARCHITECTURE_I, archI);
    }

    @FXML
    public void selectArchII(ActionEvent actionEvent) {
        mangeArchSelection(ArchitectureType.ARCHITECTURE_II, archII);
    }

    @FXML
    public void selectArchIII(ActionEvent actionEvent) {
        mangeArchSelection(ArchitectureType.ARCHITECTURE_III, archIII);
    }

    @FXML
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

    public void setArchitectureType(@NotNull ArchitectureType architectureType) {
        this.architectureType = architectureType;
        switch (architectureType) {
            case ARCHITECTURE_I -> archI.setSelected(true);
            case ARCHITECTURE_II -> archII.setSelected(true);
            case ARCHITECTURE_III -> archIII.setSelected(true);
            case ARCHITECTURE_IV -> archIV.setSelected(true);
        }
        archTypeChosen.set(true);
    }

}
