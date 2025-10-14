package ui.web.jfx.VariableInputDialog;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import ui.web.utils.UIUtils;

import java.util.HashMap;
import java.util.Map;

public class VariableInputDialogController {
    private final Map<String, TextField> textFields = new HashMap<>();
    private final BooleanProperty allValid = new SimpleBooleanProperty(true);
    private final Map<String, Boolean> validationStates = new HashMap<>();
    @FXML
    private VBox fieldsContainer;
    @FXML
    private Button acceptButton;
    @FXML
    private Button cancelButton;
    private Map<String, Integer> programArguments;

    /**
     * Initialize the controller with program arguments.
     * This method now properly handles pre-filled values from re-run scenarios.
     *
     * @param programArguments Map of argument names to their values (may be pre-filled)
     */
    public void initialiseController(Map<String, Integer> programArguments) {
        this.programArguments = programArguments;
        acceptButton.disableProperty().bind(allValid.not());
        buildFields();
    }

    /**
     * Build input fields for each program argument.
     * Enhanced to show pre-filled values when arguments are already set from re-run.
     */
    private void buildFields() {
        fieldsContainer.getChildren().clear();
        textFields.clear();
        validationStates.clear();

        for (Map.Entry<String, Integer> entry : programArguments.entrySet()) {
            Label label = new Label(entry.getKey() + " (integer value):");
            TextField field = new TextField();

            // ENHANCEMENT: Pre-fill field with existing value if it's not 0 (indicating it was set from re-run)
            Integer currentValue = entry.getValue();
            if (currentValue != null && currentValue != 0) {
                field.setText(currentValue.toString());
                System.out.println("Pre-filling argument " + entry.getKey() + " with value: " + currentValue);
            }

            // Update prompt text to show the current value more clearly
            if (currentValue != null && currentValue != 0) {
                field.setPromptText("Pre-filled value: " + currentValue + " (edit if needed)");
            } else {
                field.setPromptText("Enter positive integer value");
            }

            textFields.put(entry.getKey(), field);
            fieldsContainer.getChildren().add(new VBox(label, field));

            // Initialize validation state - pre-filled values should be valid
            boolean initiallyValid = field.getText().isEmpty() || validate(field.getText());
            validationStates.put(entry.getKey(), initiallyValid);
            updateFieldValidationStyle(field, initiallyValid);

            // Add validation listener
            field.textProperty().addListener((obs, oldVal, newVal) -> {
                boolean valid = validate(newVal);
                validationStates.put(entry.getKey(), valid);
                updateFieldValidationStyle(field, valid);
                // Update overall validity - no fields should have false validation state
                allValid.set(!validationStates.containsValue(false));
            });
        }

        // Update initial overall validity state
        allValid.set(!validationStates.containsValue(false));

        System.out.println("Built " + textFields.size() + " input fields with validation states: " + validationStates);
    }

    private boolean validate(@NotNull String newVal) {
        return newVal.isEmpty() || UIUtils.isValidProgramArgument(newVal);
    }

    private void updateFieldValidationStyle(@NotNull TextField field, boolean valid) {
        String errorMsg = "Input is not a number or not a positive number";
        if (!valid) {
            if (!field.getStyleClass().contains("error-field")) {
                field.getStyleClass().add("error-field");
            }
            field.setTooltip(new Tooltip(errorMsg));
        } else {
            field.getStyleClass().removeAll("error-field");
            field.setTooltip(null);
        }
    }

    @FXML
    private void onAccept() {
        // Update program arguments with user input
        for (Map.Entry<String, TextField> entry : textFields.entrySet()) {
            String argumentName = entry.getKey();
            String inputText = entry.getValue().getText();

            if (inputText.isEmpty()) {
                // If field is empty, keep existing value or set to 0
                Integer existingValue = programArguments.get(argumentName);
                if (existingValue == null) {
                    programArguments.put(argumentName, 0);
                }
                // If existing value is already set (from pre-fill), keep it
            } else {
                // Parse and set the new value
                int newValue = Integer.parseInt(inputText);
                programArguments.put(argumentName, newValue);
                System.out.println("Updated argument " + argumentName + " to: " + newValue);
            }
        }

        System.out.println("Final program arguments after input dialog: " + programArguments);
        close();
    }

    @FXML
    private void onCancel() {
        close();
    }

    private void close() {
        ((Stage) fieldsContainer.getScene().getWindow()).close();
    }
}