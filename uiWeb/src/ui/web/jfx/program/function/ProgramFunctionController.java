package ui.web.jfx.program.function;

import javafx.beans.property.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.controlsfx.control.ToggleSwitch;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ui.web.utils.UIUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class ProgramFunctionController {

    final IntegerProperty currentLevel = new SimpleIntegerProperty();
    final IntegerProperty maxLevel = new SimpleIntegerProperty();
    private final StringProperty mainFunctionName = new SimpleStringProperty();

    @FXML
    public RadioButton autoPaneMode;
    public ToggleSwitch animationToggle;
    @FXML
    public ToggleGroup paneMode;
    @FXML
    public MenuButton highlightSelectionButton;
    @FXML
    public RadioButton manualPaneMode;

    Consumer<Integer> onExpandLevelChangeCallback;
    Consumer<String> onVariableSelectionCallback;
    Consumer<PaneMode> onPaneModeChangeCallback;

    @FXML
    private Button collapseButton;
    @FXML
    private Label degreeInfoLabel;
    @FXML
    private Button expandButton;
    @FXML
    private Button chooseLevelButton;
    @FXML
    private MenuButton programFunctionSelect;
    @FXML
    private HBox root;

    private Consumer<String> onFunctionSelectedCallback;

    @FXML
    void initialize() {
        System.out.println("ProgramFunctionController initialized");
    }

    public void initComponent(@NotNull Consumer<Integer> onExpandLevelChangeCallback,
                              @NotNull Consumer<PaneMode> onPaneModeChangeCallback,
                              @NotNull IntegerProperty currentExpandLevelProperty,
                              @NotNull IntegerProperty MaxExpandLevelProperty,
                              @NotNull BooleanProperty programLoadedProperty,
                              @NotNull Consumer<String> OnVariableSelectionCallback,
                              @NotNull ListProperty<String> programVariables,
                              @NotNull StringProperty mainFunctionName,
                              @NotNull MapProperty<String, String> allSubFunctions,
                              @NotNull Consumer<String> onFunctionSelectedCallback,
                              @NotNull BooleanProperty isAnimationsOn) {
        this.onExpandLevelChangeCallback = onExpandLevelChangeCallback;
        this.onVariableSelectionCallback = OnVariableSelectionCallback;
        this.onPaneModeChangeCallback = onPaneModeChangeCallback;

        collapseButton.disableProperty().bind(
                programLoadedProperty.not()
                        .or(currentExpandLevelProperty.isEqualTo(0)));
        expandButton.disableProperty().bind(
                programLoadedProperty.not()
                        .or(currentExpandLevelProperty.isEqualTo(MaxExpandLevelProperty)));
        programFunctionSelect.disableProperty().bind(
                programLoadedProperty.not());
        chooseLevelButton.disableProperty().bind(
                programLoadedProperty.not());

        highlightSelectionButton.disableProperty().bind(
                programLoadedProperty.not());

        degreeInfoLabel.textProperty().bind(
                currentExpandLevelProperty.asString("Current: %d")
                        .concat("\n")
                        .concat(MaxExpandLevelProperty.asString("Maximum: %d"))
        );
        currentLevel.bind(currentExpandLevelProperty);
        maxLevel.bind(MaxExpandLevelProperty);
        programVariables.addListener((obs, oldList, newList) ->
                updateVariableDropdown(newList)
        );
        this.mainFunctionName.bind(mainFunctionName);
        this.onFunctionSelectedCallback = onFunctionSelectedCallback;
        allSubFunctions.addListener((obs, oldFuncMap, newFuncMap) ->
                populateFunctionsMenu(newFuncMap));

        isAnimationsOn.bind(animationToggle.selectedProperty());
    }

    /**
     * Updates the dropdown menu with available execution workVariables
     */
    public void updateVariableDropdown(@Nullable List<String> variables) {
        highlightSelectionButton.getItems().clear();

        if (variables == null || variables.isEmpty()) {
            MenuItem noVariablesItem = new MenuItem("No workVariables available");
            noVariablesItem.setDisable(true);
            highlightSelectionButton.getItems().add(noVariablesItem);
            return;
        }

        // Add "Clear Highlighting" option
        MenuItem clearItem = new MenuItem("Clear Highlighting");
        clearItem.setOnAction(e -> onVariableSelectionCallback.accept(null));

        highlightSelectionButton.getItems().add(clearItem);
        highlightSelectionButton.getItems().add(new SeparatorMenuItem());

        // Add each variable as a menu item
        for (String variableName : variables) {
            MenuItem variableItem = new MenuItem(variableName);
            variableItem.setOnAction(e -> {
                onVariableSelectionCallback.accept(variableName);
                System.out.println("Variable selected for highlighting: " + variableName);
            });
            highlightSelectionButton.getItems().add(variableItem);
        }
    }

    private void populateFunctionsMenu(@NotNull Map<String, String> allSubFunctions) {
        programFunctionSelect.getItems().clear();

        if (allSubFunctions.isEmpty()) {
            MenuItem noFunctionsItem = new MenuItem("No functions available");
            noFunctionsItem.setDisable(true);
            programFunctionSelect.getItems().add(noFunctionsItem);
            return;
        }
        MenuItem mainFunctionItem = new MenuItem(mainFunctionName.get());
        mainFunctionItem.setOnAction(e -> onFunctionSelectedCallback.accept(mainFunctionName.get()));

        programFunctionSelect.getItems().add(mainFunctionItem);

        if (!allSubFunctions.isEmpty()) {
            programFunctionSelect.getItems().add(new SeparatorMenuItem());

            for (Map.Entry<String, String> entry : allSubFunctions.entrySet()) {
                MenuItem subFunctionItem = new MenuItem(entry.getValue());
                subFunctionItem.setOnAction(e -> onFunctionSelectedCallback.accept(entry.getKey()));
                programFunctionSelect.getItems().add(subFunctionItem);
            }
        }
    }

    @FXML
    private void onFunctionSelected(ActionEvent event) {
    }

    @FXML
    private void handleCollapse(ActionEvent event) {
        onExpandLevelChangeCallback.accept(currentLevel.get() - 1);
    }

    @FXML
    private void handleExpand(ActionEvent event) {
        onExpandLevelChangeCallback.accept(currentLevel.get() + 1);
    }

    @FXML
    private void handleChooseExpandLevel(ActionEvent event) {
        System.out.println("Change button pressed");

        TextInputDialog dialog = new TextInputDialog("current level: " + currentLevel.get());
        dialog.setTitle("Change Program Level");
        dialog.setHeaderText("Please enter the expand level you would like to change to:");
        dialog.setContentText("Please enter a number between 0 and " + maxLevel.get() + ":");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            if (UIUtils.isValidProgramArgument(result.get())) {
                onExpandLevelChangeCallback.accept(Integer.parseInt(result.get().trim()));
            } else {
                showInvalidChoice(maxLevel.get());
            }
        }
    }

    @FXML
    private void handleAutoModeSelected(ActionEvent event) {
        if (autoPaneMode.isSelected()) {
            System.out.println("Auto mode selected");
            onPaneModeChangeCallback.accept(PaneMode.AUTO);
        }
    }

    @FXML
    private void handleManualModeSelected(ActionEvent event) {
        if (manualPaneMode.isSelected()) {
            System.out.println("Manual mode selected");
            onPaneModeChangeCallback.accept(PaneMode.MANUAL);
        }
    }

    private void showInvalidChoice(int max) {
        UIUtils.showError("Please enter a number between " + 0 + " and " + max);
    }
}