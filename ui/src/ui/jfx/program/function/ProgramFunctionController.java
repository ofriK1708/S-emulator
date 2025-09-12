package ui.jfx.program.function;

import dto.ui.VariableDTO;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ProgramFunctionController {

    Consumer<Integer> OnExpandLevelChangeCallback;
    Consumer<String> OnVariableSelectionCallback; // NEW: Callback for variable selection
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
    private Button chooseLevelButton;
    @FXML
    private MenuButton HighSelectionButton; // Changed from Button to MenuButton

    @FXML
    void initialize() {
        System.out.println("ProgramFunctionController initialized");

        // Initialize the MenuButton
        HighSelectionButton.setText("Highlight Variable");
        HighSelectionButton.getItems().clear();
    }

    public void initComponent(Consumer<Integer> OnExpandLevelChangeCallback,
                              IntegerProperty currentExpandLevelProperty,
                              IntegerProperty MaxExpandLevelProperty,
                              BooleanProperty programLoadedProperty,
                              BooleanProperty programRanAtLeastOnceProperty, // NEW: Add this parameter
                              Consumer<String> OnVariableSelectionCallback) { // NEW: Add variable selection callback

        this.OnExpandLevelChangeCallback = OnExpandLevelChangeCallback;
        this.OnVariableSelectionCallback = OnVariableSelectionCallback;

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
        HighSelectionButton.disableProperty().bind(
                programLoadedProperty.not()
                        .or(programRanAtLeastOnceProperty.not()));

        degreeInfoLabel.textProperty().bind(
                currentExpandLevelProperty.asString("Current: %d")
                        .concat("\n")
                        .concat(MaxExpandLevelProperty.asString("Maximum: %d"))
        );
        currentLevel.bind(currentExpandLevelProperty);
        maxLevel.bind(MaxExpandLevelProperty);
    }

    /**
     * Updates the dropdown menu with available execution variables
     * @param variables List of VariableDTO objects from ExecutionVariableController
     */
    public void updateVariableDropdown(List<VariableDTO> variables) {
        HighSelectionButton.getItems().clear();

        if (variables == null || variables.isEmpty()) {
            MenuItem noVariablesItem = new MenuItem("No variables available");
            noVariablesItem.setDisable(true);
            HighSelectionButton.getItems().add(noVariablesItem);
            return;
        }

        // Add "Clear Highlighting" option
        MenuItem clearItem = new MenuItem("Clear Highlighting");
        clearItem.setOnAction(e -> {
            if (OnVariableSelectionCallback != null) {
                OnVariableSelectionCallback.accept(null); // null means clear highlighting
            }
        });
        HighSelectionButton.getItems().add(clearItem);
        HighSelectionButton.getItems().add(new SeparatorMenuItem());

        // Add each variable as a menu item
        for (VariableDTO variable : variables) {
            String variableName = variable.name().get();
            int variableValue = variable.value().get();

            MenuItem variableItem = new MenuItem(variableName + " = " + variableValue);
            variableItem.setOnAction(e -> {
                if (OnVariableSelectionCallback != null) {
                    OnVariableSelectionCallback.accept(variableName);
                    System.out.println("Variable selected for highlighting: " + variableName);
                }
            });
            HighSelectionButton.getItems().add(variableItem);
        }
    }

    // Collapse to basic program (level 0)
    @FXML
    void handleCollapse(ActionEvent event) {
        OnExpandLevelChangeCallback.accept(currentLevel.get() - 1);
    }

    // Expand program
    @FXML
    void handleExpand(ActionEvent event) {
        OnExpandLevelChangeCallback.accept(currentLevel.get() + 1);
    }

    // Change: open input dialog for manual level entry
    @FXML
    void handleChooseExpandLevel(ActionEvent event) {
        System.out.println("Change button pressed");

        TextInputDialog dialog = new TextInputDialog(String.valueOf(currentLevel.get()));
        dialog.setTitle("Change Program Level");
        dialog.setHeaderText("Please enter the expand level you would like to change to:");
        dialog.setContentText("Please enter a number between 0 and " + maxLevel.get() + ":");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                int choice = Integer.parseInt(result.get().trim());
                if (choice >= 0 && choice <= maxLevel.get()) {
                    OnExpandLevelChangeCallback.accept(choice);
                } else {
                    showInvalidChoice(0, maxLevel.get());
                }
            } catch (NumberFormatException e) {
                showInvalidChoice(0, maxLevel.get());
            }
        }
    }

    private void showInvalidChoice(int min, int max) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid Choice");
        alert.setContentText("Invalid choice. Please enter a number between " + min + " and " + max + ".");
        alert.showAndWait();
    }
}
