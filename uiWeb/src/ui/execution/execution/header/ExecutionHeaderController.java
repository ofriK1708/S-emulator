package ui.execution.execution.header;

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

}