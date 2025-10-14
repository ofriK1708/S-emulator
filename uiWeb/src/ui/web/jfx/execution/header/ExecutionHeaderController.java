package ui.web.jfx.execution.header;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.jetbrains.annotations.NotNull;

/**
 * Controller for the Execution screen header bar.
 * Displays user name, screen title, available credits, and back button.
 */
public class ExecutionHeaderController {

    @FXML
    private Button backToDashboardButton;
    @FXML
    private Label userNameLabel;
    @FXML
    private Label screenTitleLabel;
    @FXML
    private Label creditsValueLabel;

    private Runnable onBackToDashboard;

    /**
     * Initialize the header with bindings to properties and callback
     *
     * @param userName          Property for current user name
     * @param screenTitle       Property for screen title (dynamic)
     * @param availableCredits  Property for available credits
     * @param onBackToDashboard Callback to return to Dashboard
     */
    public void initComponent(
            @NotNull StringProperty userName,
            @NotNull StringProperty screenTitle,
            @NotNull IntegerProperty availableCredits,
            @NotNull Runnable onBackToDashboard) {

        this.onBackToDashboard = onBackToDashboard;

        // Bind UI elements to properties
        userNameLabel.textProperty().bind(userName);
        screenTitleLabel.textProperty().bind(screenTitle);
        creditsValueLabel.textProperty().bind(availableCredits.asString());

        System.out.println("ExecutionHeaderController initialized");
    }

    /**
     * Alternative init without back button callback
     * (maintains backward compatibility)
     */
    public void initComponent(
            @NotNull StringProperty userName,
            @NotNull StringProperty screenTitle,
            @NotNull IntegerProperty availableCredits) {

        userNameLabel.textProperty().bind(userName);
        screenTitleLabel.textProperty().bind(screenTitle);
        creditsValueLabel.textProperty().bind(availableCredits.asString());

        // Hide back button if no callback provided
        if (backToDashboardButton != null) {
            backToDashboardButton.setVisible(false);
            backToDashboardButton.setManaged(false);
        }

        System.out.println("ExecutionHeaderController initialized (no back button)");
    }

    /**
     * Handle back to dashboard button click
     */
    @FXML
    private void handleBackToDashboard() {
        if (onBackToDashboard != null) {
            System.out.println("ExecutionHeader: Back to Dashboard clicked");
            onBackToDashboard.run();
        } else {
            System.err.println("ExecutionHeader: No back callback set");
        }
    }

    /**
     * Set user name directly (alternative to binding)
     */
    public void setUserName(String userName) {
        if (userNameLabel != null) {
            userNameLabel.setText(userName != null ? userName : "Guest User");
        }
    }

    /**
     * Set screen title directly (alternative to binding)
     */
    public void setScreenTitle(String title) {
        if (screenTitleLabel != null) {
            screenTitleLabel.setText(title != null ? title : "S-Emulator");
        }
    }

    /**
     * Set credits value directly (alternative to binding)
     */
    public void setCredits(int credits) {
        if (creditsValueLabel != null) {
            creditsValueLabel.setText(String.valueOf(credits));
        }
    }

    /**
     * Show or hide the back button
     */
    public void setBackButtonVisible(boolean visible) {
        if (backToDashboardButton != null) {
            backToDashboardButton.setVisible(visible);
            backToDashboardButton.setManaged(visible);
        }
    }
}