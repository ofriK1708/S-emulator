package ui.jfx.program.function;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ui.utils.UIUtils;

import java.util.Optional;
import java.util.function.Consumer;

public class ProgramFunctionController {

    Consumer<Integer> OnExpandLevelChangeCallback;
    @FXML
    public RadioButton autoPaneMode;
    IntegerProperty currentLevel = new SimpleIntegerProperty();
    IntegerProperty maxLevel = new SimpleIntegerProperty();

    @FXML
    private Button collapseButton;
    @FXML
    private Button ProgramFunctionSelect;
    @FXML
    private Label degreeInfoLabel;
    @FXML
    private Button expandButton;
    @FXML
    private Button chooseLevelButton;     // New Change button
    @FXML
    public ToggleGroup paneMode;
    @FXML
    private Button HighSelectionButton;
    @FXML
    public RadioButton manualPaneMode;
    Consumer<PaneMode> OnPaneModeChangeCallback;

    @FXML
    void initialize() {
        System.out.println("ProgramFunctionController initialized");
    }

    public void initComponent(Consumer<Integer> OnExpandLevelChangeCallback,
                              Consumer<PaneMode> OnPaneModeChangeCallback,
                              IntegerProperty currentExpandLevelProperty, IntegerProperty MaxExpandLevelProperty,
                              BooleanProperty programLoadedProperty) {

        this.OnExpandLevelChangeCallback = OnExpandLevelChangeCallback;
        this.OnPaneModeChangeCallback = OnPaneModeChangeCallback;
        collapseButton.disableProperty().bind(
                programLoadedProperty.not()
                        .or(currentExpandLevelProperty.isEqualTo(0)));
        expandButton.disableProperty().bind(
                programLoadedProperty.not()
                        .or(currentExpandLevelProperty.isEqualTo(MaxExpandLevelProperty)));
        ProgramFunctionSelect.disableProperty().bind(
                programLoadedProperty.not());
        chooseLevelButton.disableProperty().bind(
                programLoadedProperty.not());
        HighSelectionButton.disableProperty().bind(
                programLoadedProperty.not());
        degreeInfoLabel.textProperty().bind(
                currentExpandLevelProperty.asString("Current: %d")
                        .concat("\n")
                        .concat(MaxExpandLevelProperty.asString("Maximum: %d"))
        );
        currentLevel.bind(currentExpandLevelProperty);
        maxLevel.bind(MaxExpandLevelProperty);
    }

    // Collapse to basic program (level 0) - same as console displayLoadedProgram
    @FXML
    void handleCollapse(ActionEvent event) {
        OnExpandLevelChangeCallback.accept(currentLevel.get() - 1);
    }


    // Expand program - same logic as console expandProgram
    @FXML
    void handleExpand(ActionEvent event) {
        OnExpandLevelChangeCallback.accept(currentLevel.get() + 1);
    }

    // Change: open input dialog for manual level entry
    @FXML
    void handleChooseExpandLevel(ActionEvent event) {
        System.out.println("Change button pressed");

        // Create input dialog for manual level entry
        TextInputDialog dialog = new TextInputDialog(String.valueOf(currentLevel));
        dialog.setTitle("Change Program Level");
        dialog.setHeaderText("Please enter the expand level you would like to change to:");
        dialog.setContentText("Please enter a number between 0 and " + maxLevel + ":");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                int choice = Integer.parseInt(result.get().trim());
                if (choice >= 0 && choice <= maxLevel.get()) {
                    OnExpandLevelChangeCallback.accept(choice);
                } else {
                    showInvalidChoice(maxLevel.get());
                }
            } catch (NumberFormatException e) {
                showInvalidChoice(maxLevel.get());
            }
        }
    }

    @FXML
    private void handleAutoModeSelected(ActionEvent event) {
        if (autoPaneMode.isSelected()) {
            System.out.println("Auto mode selected");
            // Notify listener about the mode change
            OnPaneModeChangeCallback.accept(PaneMode.AUTO);
        }
    }

    @FXML
    private void handleManualModeSelected(ActionEvent event) {
        if (manualPaneMode.isSelected()) {
            System.out.println("Manual mode selected");
            // Notify listener about the mode change
            OnPaneModeChangeCallback.accept(PaneMode.MANUAL);
        }
    }

    private void showInvalidChoice(int max) {
        UIUtils.showError("Please enter a number between " + 0 + " and " + max);
    }
}
