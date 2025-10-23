package ui.web.jfx.execution.header;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.jetbrains.annotations.NotNull;

/**
 * Controller for the Execution screen header bar.
 * Displays user name, screen title, and available credits.
 * Note: Back button has been moved to the debugger section.
 */
public class ExecutionHeaderController {

    @FXML
    private Label userNameLabel;
    @FXML
    private Label screenTitleLabel;
    @FXML
    private Label creditsValueLabel;

    /**
     * Initialize the header with bindings to properties
     *
     * @param userName          Property for current user name
     * @param screenTitle       Property for screen title (dynamic)
     * @param availableCredits  Property for available credits
     */
    public void initComponent(
            @NotNull StringProperty userName,
            @NotNull StringProperty screenTitle,
            @NotNull IntegerProperty availableCredits) {

        // Bind UI elements to properties
        userNameLabel.textProperty().bind(userName);
        screenTitleLabel.textProperty().bind(screenTitle);
        creditsValueLabel.textProperty().bind(availableCredits.asString());

        System.out.println("ExecutionHeaderController initialized");
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
}