package ui.dashboard.chargeCredits;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;

public class ChargeCreditsDialogController {

    @FXML
    private Label finalAmountLabel;
    @FXML
    private TextField customAmountField;
    @FXML
    private Button setButton;
    @FXML
    private Label errorLabel;
    @FXML
    private Button confirmButton;
    @FXML
    private Button cancelButton;

    private int originalAmount;
    private int finalAmount;

    public void init(int currentAmount, Stage stage) {
        this.originalAmount = currentAmount;
        this.finalAmount = currentAmount;
        updateFinalAmountLabel();

        // Set the close request handler
        stage.setOnCloseRequest(event -> {
            event.consume(); // Consume the event to override default closing
            onCancel();      // Call the cancel logic
        });
    }

    private void updateFinalAmountLabel() {
        finalAmountLabel.setText(String.valueOf(finalAmount));
    }

    @FXML
    private void add1000() {
        finalAmount += 1000;
        updateFinalAmountLabel();
    }

    @FXML
    private void add10000() {
        finalAmount += 10000;
        updateFinalAmountLabel();
    }

    @FXML
    private void add100000() {
        finalAmount += 100000;
        updateFinalAmountLabel();
    }

    @FXML
    private void add1000000() {
        finalAmount += 1000000;
        updateFinalAmountLabel();
    }

    @FXML
    private void setCustomAmount() {
        try {
            int customAmount = Integer.parseInt(customAmountField.getText());
            if (customAmount >= 0) {
                finalAmount = customAmount;
                updateFinalAmountLabel();
                hideError();
            } else {
                showError();
            }
        } catch (NumberFormatException e) {
            showError();
        }
    }

    private void showError() {
        errorLabel.setText("Invalid amount. Please enter a non-negative integer.");
        errorLabel.setVisible(true);
        customAmountField.getStyleClass().add("error");
        Tooltip tooltip = new Tooltip("Invalid amount. Please enter a non-negative integer.");
        customAmountField.setTooltip(tooltip);
    }

    private void hideError() {
        errorLabel.setVisible(false);
        customAmountField.getStyleClass().remove("error");
        customAmountField.setTooltip(null);
    }

    @FXML
    private void onConfirm() {
        closeWithResult(finalAmount);
    }

    @FXML
    private void onCancel() {
        closeWithResult(originalAmount);
    }

    private void closeWithResult(Integer result) {
        Stage stage = (Stage) confirmButton.getScene().getWindow();
        stage.setUserData(result);
        stage.close();
    }
}
