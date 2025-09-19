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
import org.jetbrains.annotations.NotNull;
import ui.utils.UIUtils;

import java.util.HashMap;
import java.util.Map;

public class VariableInputDialogController {
    @FXML
    private VBox fieldsContainer;
    @FXML
    private Button acceptButton;
    @FXML
    private Button cancelButton;

    private final Map<String, TextField> textFields = new HashMap<>();
    private Map<String, Integer> programArguments;
    private final BooleanProperty allValid = new SimpleBooleanProperty(true);
    private final Map<String, Boolean> validationStates = new HashMap<>();

    public void initialiseController(Map<String, Integer> programArguments) {
        this.programArguments = programArguments;
        acceptButton.disableProperty().bind(allValid.not());
        buildFields();
    }

    private void buildFields() {
        fieldsContainer.getChildren().clear();
        textFields.clear();
        for (Map.Entry<String, Integer> entry : programArguments.entrySet()) {
            Label label = new Label(entry.getKey() + " (integer value):");
            TextField field = new TextField();
            field.setPromptText("Enter positive integer value, current value is: {" + entry.getValue() + "}");
            textFields.put(entry.getKey(), field);
            fieldsContainer.getChildren().add(new VBox(label, field));
            field.textProperty().addListener((obs, oldVal, newVal) -> {
                boolean valid = validate(newVal);
                validationStates.put(entry.getKey(), valid);
                updateFieldValidationStyle(field, valid);
                allValid.set(!validationStates.containsValue(false));

            });
        }
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
