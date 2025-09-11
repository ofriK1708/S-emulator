package ui.jfx.program.function;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;

import java.util.Optional;
import java.util.function.Consumer;

public class ProgramFunctionController {

    Consumer<Integer> OnExpandLevelChangeCallback;
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
    private Button HighSelectionButton;

    @FXML
    void initialize() {
        System.out.println("ProgramFunctionController initialized");
    }

    public void initComponent(Consumer<Integer> OnExpandLevelChangeCallback,
                              IntegerProperty currentExpandLevelProperty, IntegerProperty MaxExpandLevelProperty,
                              BooleanProperty programLoadedProperty) {
        this.OnExpandLevelChangeCallback = OnExpandLevelChangeCallback;
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
                    showInvalidChoice(0, maxLevel.get());
                }
            } catch (NumberFormatException e) {
                showInvalidChoice(0, maxLevel.get());
            }
        }
    }


    private void showInvalidChoice(int min, int max) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Invalid Choice");
        alert.setContentText("Invalid choice. Please enter a number between " + min + " and " + max + ".");
        alert.showAndWait();
    }
}
