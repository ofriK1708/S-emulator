package jfx.variables;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.Map;

public class VariablesController {

    @FXML
    private VBox variablesContainer;

    @FXML
    private Label statusLabel;

    public VariablesController() {
    }

    @FXML
    public void initialize() {
        clearVariables();
    }

    public void setVariables(Map<String, String> variables) {
        clearVariables();

        if (variables == null || variables.isEmpty()) {
            statusLabel.setText("No variables defined");
            return;
        }

        statusLabel.setText("Program Variables:");

        for (Map.Entry<String, String> entry : variables.entrySet()) {
            Label variableLabel = new Label(entry.getKey() + ": " + entry.getValue());
            variableLabel.getStyleClass().add("variable-item");
            variablesContainer.getChildren().add(variableLabel);
        }
    }

    public void clearVariables() {
        variablesContainer.getChildren().clear();
        statusLabel.setText("No program loaded");
    }

    public void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().add("success-message");
    }
}