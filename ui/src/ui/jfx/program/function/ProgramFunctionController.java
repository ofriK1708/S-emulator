package ui.jfx.program.function;

import javafx.beans.property.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.controlsfx.control.ToggleSwitch;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ui.utils.UIUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class ProgramFunctionController {

    final IntegerProperty currentLevel = new SimpleIntegerProperty();

    @FXML
    public RadioButton autoPaneMode;
    final IntegerProperty maxLevel = new SimpleIntegerProperty();
    public ToggleSwitch animationToggle;
    @FXML
    private Button collapseButton;
    private final StringProperty mainFunctionName = new SimpleStringProperty();
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
    @FXML
    private MenuButton programFunctionSelect;
    @FXML
    public ToggleGroup themes;
    Consumer<Integer> onExpandLevelChangeCallback;
    Consumer<String> onVariableSelectionCallback; // NEW: Callback for variable selection
    Consumer<PaneMode> onPaneModeChangeCallback;
    Consumer<Theme> onThemeSelectCallback;
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
                              @NotNull Consumer<Theme> onThemeSelectCallback,
                              @NotNull BooleanProperty isAnimationsOn) { // NEW: Add variable selection
        // callback
        this.onExpandLevelChangeCallback = onExpandLevelChangeCallback;
        this.onVariableSelectionCallback = OnVariableSelectionCallback;
        this.onThemeSelectCallback = onThemeSelectCallback;

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
        this.mainFunctionName.bind(mainFunctionName);
        this.onFunctionSelectedCallback = onFunctionSelectedCallback;
        allSubFunctions.addListener((obs,
                                     oldFuncMap, newFuncMap) ->
                populateFunctionsMenu(newFuncMap));
        themes.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null) {
                RadioMenuItem selectThemeButton = (RadioMenuItem) newToggle;
                String themeName = selectThemeButton.getText();
                onThemeSelect(themeName);
            }
        });
        isAnimationsOn.bind(animationToggle.selectedProperty());
    }


    /**
     * Updates the dropdown menu with available execution workVariables
     *
     * @param variables List of VariableDTO objects from ExecutionVariableController
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
        clearItem.setOnAction(e -> onVariableSelectionCallback.accept(null)); // null means clear highlighting

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

    private void onThemeSelect(String themeName) {
        Theme selectedTheme;
        switch (themeName.toLowerCase()) {
            case "light (default)":
                selectedTheme = Theme.LIGHT;
                break;
            case "dark":
                selectedTheme = Theme.DARK;
                break;
            case "mac is the best (for aviad ;) )":
                selectedTheme = Theme.MAC_IS_THE_BEST;
                break;
            case "windows is the worst (for aviad ;) )":
                selectedTheme = Theme.WINDOWS_IS_THE_WORST;
                break;
            default:
                System.out.println("Unknown theme selected: " + themeName);
                return;
        }
        onThemeSelectCallback.accept(selectedTheme);
    }

    private void populateFunctionsMenu(@NotNull Map<String, String> allSubFunctions) {

        // Add main function
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

        // Add sub-functions if available
        if (!allSubFunctions.isEmpty()) {
            programFunctionSelect.getItems().add(new SeparatorMenuItem()); // Separator for clarity

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

        // Create input dialog for manual level entry
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
            // Notify listener about the mode change
            onPaneModeChangeCallback.accept(PaneMode.AUTO);
        }
    }

    @FXML
    private void handleManualModeSelected(ActionEvent event) {
        if (manualPaneMode.isSelected()) {
            System.out.println("Manual mode selected");
            // Notify listener about the mode change
            onPaneModeChangeCallback.accept(PaneMode.MANUAL);
        }
    }

    private void showInvalidChoice(int max) {
        UIUtils.showError("Please enter a number between " + 0 + " and " + max);
    }
}
