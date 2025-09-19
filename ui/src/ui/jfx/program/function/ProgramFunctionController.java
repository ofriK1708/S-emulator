package ui.jfx.program.function;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ui.utils.UIUtils;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ProgramFunctionController {

    Consumer<Integer> OnExpandLevelChangeCallback;
    @FXML
    public RadioButton autoPaneMode;
    Consumer<String> OnVariableSelectionCallback; // NEW: Callback for variable selection
    final IntegerProperty currentLevel = new SimpleIntegerProperty();
    final IntegerProperty maxLevel = new SimpleIntegerProperty();

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
    public MenuButton highlightSelectionButton; // Changed from Button to MenuButton
    @FXML
    public RadioButton manualPaneMode;

    Consumer<PaneMode> OnPaneModeChangeCallback;

    @FXML
    void initialize() {
        System.out.println("ProgramFunctionController initialized");
    }

    public void initComponent(Consumer<Integer> OnExpandLevelChangeCallback,
                              Consumer<PaneMode> OnPaneModeChangeCallback,
                              @NotNull IntegerProperty currentExpandLevelProperty, @NotNull IntegerProperty MaxExpandLevelProperty,
                              @NotNull BooleanProperty programLoadedProperty,
                              Consumer<String> OnVariableSelectionCallback,
                              @NotNull ListProperty<String> programVariables) { // NEW: Add variable selection callback
        this.OnExpandLevelChangeCallback = OnExpandLevelChangeCallback;
        this.OnVariableSelectionCallback = OnVariableSelectionCallback;

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

        // NEW: Bind HighSelectionButton to programRanAtLeastOnce
        highlightSelectionButton.disableProperty().bind(
                programLoadedProperty.not());

        degreeInfoLabel.textProperty().bind(
                currentExpandLevelProperty.asString("Current: %d")
                        .concat("\n")
                        .concat(MaxExpandLevelProperty.asString("Maximum: %d"))
        );
        currentLevel.bind(currentExpandLevelProperty);
        maxLevel.bind(MaxExpandLevelProperty);
        programVariables.addListener((obs,
                                      oldList, newList) ->
                updateVariableDropdown(newList)
        );
    }


    /**
     * Updates the dropdown menu with available execution variables
     *
     * @param variables List of VariableDTO objects from ExecutionVariableController
     */
    public void updateVariableDropdown(@Nullable List<String> variables) {
        highlightSelectionButton.getItems().clear();

        if (variables == null || variables.isEmpty()) {
            MenuItem noVariablesItem = new MenuItem("No variables available");
            noVariablesItem.setDisable(true);
            highlightSelectionButton.getItems().add(noVariablesItem);
            return;
        }

        // Add "Clear Highlighting" option
        MenuItem clearItem = new MenuItem("Clear Highlighting");
        clearItem.setOnAction(e -> OnVariableSelectionCallback.accept(null)); // null means clear highlighting});

        highlightSelectionButton.getItems().add(clearItem);
        highlightSelectionButton.getItems().add(new SeparatorMenuItem());

        // Add each variable as a menu item
        for (String variableName : variables) {
            MenuItem variableItem = new MenuItem(variableName);
            variableItem.setOnAction(e -> {
                OnVariableSelectionCallback.accept(variableName);
                System.out.println("Variable selected for highlighting: " + variableName);
            });
            highlightSelectionButton.getItems().add(variableItem);
        }
    }

    @FXML
    void handleCollapse(ActionEvent event) {
        OnExpandLevelChangeCallback.accept(currentLevel.get() - 1);
    }

    @FXML
    void handleExpand(ActionEvent event) {
        OnExpandLevelChangeCallback.accept(currentLevel.get() + 1);
    }

    @FXML
    void handleChooseExpandLevel(ActionEvent event) {
        System.out.println("Change button pressed");

        // Create input dialog for manual level entry
        TextInputDialog dialog = new TextInputDialog("current level: " + currentLevel.get());
        dialog.setTitle("Change Program Level");
        dialog.setHeaderText("Please enter the expand level you would like to change to:");
        dialog.setContentText("Please enter a number between 0 and " + maxLevel.get() + ":");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            if (UIUtils.isValidProgramArgument(result.get())) {
                OnExpandLevelChangeCallback.accept(Integer.parseInt(result.get().trim()));
            } else {
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
