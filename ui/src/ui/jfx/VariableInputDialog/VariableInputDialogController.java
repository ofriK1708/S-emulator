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
            fieldsContainer.getChildren().add(new VBox(label, field));
            textFields.put(var, field);
            field.textProperty().addListener((obs, oldVal, newVal) -> validate(newVal));
        }
    }

    private void validate(String newVal) {
        String errorMsg = "Input is not a number or not a positive number";
        for (TextField tf : textFields.values()) {
            if (!newVal.isEmpty() && !UIUtils.isValidProgramArgument(newVal)) {
                if (!tf.getStyleClass().contains("error-field")) {
                    tf.getStyleClass().add("error-field");
                }
                tf.setTooltip(new Tooltip(errorMsg));
                allValid.set(false);
            } else {
                tf.getStyleClass().removeAll("error-field");
                tf.setTooltip(null);
            }
        }
    }

    @FXML
    private void onAccept() {
        for (Map.Entry<String, TextField> entry : textFields.entrySet()) {
            if(entry.getValue().getText().isEmpty()){
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
