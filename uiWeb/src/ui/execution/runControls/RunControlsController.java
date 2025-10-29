package ui.execution.runControls;

import engine.utils.ArchitectureType;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
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
import java.util.function.Consumer;

public class RunControlsController {

    @FXML
    public Button run;

    private final @NotNull BooleanProperty debugModeActive = new SimpleBooleanProperty(false);
    @FXML
    public ToggleGroup archType;
    @Nullable ProgramRunType programRunType;
    @FXML
    private ToggleButton debugType;
    @FXML
    private ToggleButton runType;
    @FXML
    private ToggleGroup runTypes;
    @FXML
    private Button setRun;
    @FXML
    private ToggleButton archI;
    @FXML
    private ToggleButton archII;

    final BooleanProperty runTypeChosen = new SimpleBooleanProperty(false);
    @FXML
    private ToggleButton archIII;
    @FXML
    private ToggleButton archIV;
    final BooleanProperty archTypeChosen = new SimpleBooleanProperty(false);
    private BiConsumer<ProgramRunType, ArchitectureType> runCallback;
    private Runnable setCallback;
    private @NotNull ArchitectureType architectureType = ArchitectureType.ARCHITECTURE_I;
    private @NotNull Consumer<@Nullable ArchitectureType> onArchitectureTypeChangeCallback = architectureType -> {
        System.out.println("Architecture type changed to: " + architectureType);
    };
    private ObjectProperty<ArchitectureType> minimumArchitectureTypeNeeded;
    private final BooleanProperty minimumArchitectureTypeNeededChosen = new SimpleBooleanProperty(false);

    public void initComponent(BiConsumer<ProgramRunType, ArchitectureType> runCallback, Runnable setCallback,
                              @NotNull BooleanProperty programLoaded, @NotNull BooleanProperty argumentsEntered,
                              @NotNull ObjectProperty<ArchitectureType> minimumArchitectureTypeNeeded,
                              @NotNull Consumer<@Nullable ArchitectureType> onArchitectureTypeChangeCallback) {
        this.runCallback = runCallback;
        this.setCallback = setCallback;
        this.onArchitectureTypeChangeCallback = onArchitectureTypeChangeCallback;
        this.minimumArchitectureTypeNeeded = minimumArchitectureTypeNeeded;

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
        run.disableProperty().bind(
                runTypeChosen.not()
                        .or(archTypeChosen.not())
                        .or(minimumArchitectureTypeNeededChosen.not()));
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

    /**
     * Manages architecture selection based on toggle button state.
     * if it was selected before its means user want to deselect it.
     * Therefore, we set archTypeChosen to false.
     *
     * @param architectureType the architecture type associated with the toggle button
     * @param toggleButton     the toggle button that was interacted with
     */
    public void mangeArchSelection(ArchitectureType architectureType, ToggleButton toggleButton) {
        if (toggleButton.isSelected()) {
            this.architectureType = architectureType;
            archTypeChosen.set(true);
            minimumArchitectureTypeNeededChosen.set(architectureType.compareTo(
                    minimumArchitectureTypeNeeded.get()) >= 0);
            onArchitectureTypeChangeCallback.accept(architectureType);
        } else {
            archTypeChosen.set(false);
            onArchitectureTypeChangeCallback.accept(null);
        }
    }

    /**
     * Sets the architecture type programmatically and updates the UI accordingly.
     *
     * @param architectureType the architecture type to set
     */
    public void setArchitectureType(@NotNull ArchitectureType architectureType) {
        this.architectureType = architectureType;
        switch (architectureType) {
            case ARCHITECTURE_I -> archI.setSelected(true);
            case ARCHITECTURE_II -> archII.setSelected(true);
            case ARCHITECTURE_III -> archIII.setSelected(true);
            case ARCHITECTURE_IV -> archIV.setSelected(true);
        }
        archTypeChosen.set(true);
        minimumArchitectureTypeNeededChosen.set(architectureType.compareTo(
                minimumArchitectureTypeNeeded.get()) >= 0);
    }

}
