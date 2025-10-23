package ui.web.jfx.login;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.jetbrains.annotations.Nullable;

/**
 * Controller for the Login screen.
 * Handles username input before accessing the Dashboard.
 */
public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private Button loginButton;

    private String enteredUsername = null;

    @FXML
    public void initialize() {
        usernameField.setOnAction(event -> handleLogin());
        loginButton.setOnAction(event -> handleLogin());
        System.out.println("LoginController initialized");
    }

    /**
     * Handle the login action when Continue button is pressed.
     */
    private void handleLogin() {
        String username = usernameField.getText().trim();

        if (username.isEmpty()) {
            System.out.println("Login: Username cannot be empty");
            usernameField.setStyle("-fx-border-color: #d32f2f; -fx-border-width: 2px;");
            return;
        }

        enteredUsername = username;
        System.out.println("Login: User entered username - " + username);

        closeLoginWindow();
    }

    /**
     * Get the entered username.
     *
     * @return The username entered by the user, or null if not set.
     */
    @Nullable
    public String getEnteredUsername() {
        return enteredUsername;
    }

    /**
     * Close the login window.
     */
    private void closeLoginWindow() {
        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.close();
    }
}