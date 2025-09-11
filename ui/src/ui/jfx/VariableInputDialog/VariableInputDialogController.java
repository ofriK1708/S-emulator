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
    private final BooleanProperty allValid = new SimpleBooleanProperty(true);
    private Map<String, Boolean> validationStates = new HashMap<>();

    public void initialiseController(Set<String> requiredArguments, Map<String, Integer> programArguments) {
        this.requiredArguments = requiredArguments;
        this.programArguments = programArguments;
        acceptButton.disableProperty().bind(allValid.not());
        buildFields();
    }

    private void buildFields() {
        fieldsContainer.getChildren().clear();
        textFields.clear();
        for (String arg : requiredArguments) {
            Label label = new Label(arg + " (integer value):");
            TextField field = new TextField();
            field.setPromptText("Enter positive integer value...");
            textFields.put(arg, field);
            fieldsContainer.getChildren().add(new VBox(label, field));
            field.textProperty().addListener((obs, oldVal, newVal) -> {
                boolean valid = validate(newVal);
                validationStates.put(arg, valid);
                updateFieldValidationStyle(field, valid);
                allValid.set(!validationStates.containsValue(false));

            });
        }
    }

    private boolean validate(String newVal) {
        return newVal.isEmpty() || UIUtils.isValidProgramArgument(newVal);
    }

    private void updateFieldValidationStyle(TextField field, boolean valid) {
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
        for (Map.Entry<String, TextField> entry : textFields.entrySet()) {
            if (entry.getValue().getText().isEmpty()) {
                programArguments.put(entry.getKey(), 0);
            } else {
                programArguments.put(entry.getKey(), Integer.parseInt(entry.getValue().getText()));
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
