package ui.jfx.VariableInputDialog;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ui.utils.UIUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class VariableInputDialogController {
    @FXML
    private VBox fieldsContainer;
    @FXML
    private Button acceptButton;
    @FXML
    private Button cancelButton;

    private final Map<String, TextField> textFields = new HashMap<>();
    private Map<String, Integer> programArguments;
    private Set<String> requiredArguments;
    private final BooleanProperty allValid = new SimpleBooleanProperty(false); // Start as false

    public void initialiseController(Set<String> requiredArguments, Map<String, Integer> programArguments) {
        this.requiredArguments = requiredArguments;
        this.programArguments = programArguments;
        acceptButton.disableProperty().bind(allValid.not());
        buildFields();
    }

    private void buildFields() {
        fieldsContainer.getChildren().clear();
        textFields.clear();
        for (String var : requiredArguments) {
            Label label = new Label(var + " (integer value):");
            TextField field = new TextField();
            field.setPromptText("Enter positive integer value...");

            // Add listener that validates ALL fields when ANY field changes
            field.textProperty().addListener((obs, oldVal, newVal) -> {
                // Only allow digits
                if (!newVal.matches("\\d*")) {
                    field.setText(oldVal);
                }
                validateAllFields(); // Validate ALL fields, not just one
            });

            fieldsContainer.getChildren().add(new VBox(label, field));
            textFields.put(var, field);
        }
        validateAllFields(); // Initial validation
    }

    // Fixed validation method - checks each field individually
    private void validateAllFields() {
        boolean allFieldsValid = true;

        for (TextField tf : textFields.values()) {
            String text = tf.getText(); // Get text from THIS field, not newVal

            // Clear previous styling
            tf.getStyleClass().removeAll("error-field");
            tf.setTooltip(null);

            if (text == null || text.trim().isEmpty()) {
                // Empty field is invalid
                tf.getStyleClass().add("error-field");
                tf.setTooltip(new Tooltip("Value required"));
                allFieldsValid = false;
            } else if (!UIUtils.isValidProgramArgument(text.trim())) {
                // Invalid number
                tf.getStyleClass().add("error-field");
                tf.setTooltip(new Tooltip("Input is not a number or not a positive number"));
                allFieldsValid = false;
            }
        }

        allValid.set(allFieldsValid);
    }

    @FXML
    private void onAccept() {
        // Final validation before accepting
        if (!allValid.get()) {
            System.out.println("Cannot accept - validation failed");
            return;
        }

        for (Map.Entry<String, TextField> entry : textFields.entrySet()) {
            String text = entry.getValue().getText();
            if (text == null || text.trim().isEmpty()) {
                programArguments.put(entry.getKey(), 0);
            } else {
                try {
                    programArguments.put(entry.getKey(), Integer.parseInt(text.trim()));
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing: '" + text + "' for " + entry.getKey());
                    return; // Don't close if parsing fails
                }
            }
        }
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
