package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import ui.dashboard.DashboardController;
import ui.login.LoginController;

import java.net.URL;

import static ui.utils.clientConstants.DASHBOARD_PATH;
import static ui.utils.clientConstants.LOGIN_PATH;

/**
 * Main application entry point.
 * Shows Login dialog first, then loads Dashboard with authenticated user.
 */
public class Main extends Application {

    public static void main(String[] args) {
        Thread.currentThread().setName("Main");
        launch(args);
    }

    @Override
    public void start(@NotNull Stage primaryStage) throws Exception {
        // Step 1: Show login dialog and get username
        String username = showLoginDialog(primaryStage);

        if (username == null || username.isEmpty()) {
            System.out.println("Login cancelled - exiting application");
            return;
        }

        System.out.println("Login successful - User: " + username);

        // Step 2: Load Dashboard as the main screen
        FXMLLoader dashboardLoader = new FXMLLoader();
        URL dashboardResource = getClass().getResource(DASHBOARD_PATH);
        if (dashboardResource == null) {
            throw new IllegalStateException("Dashboard.fxml not found");
        }
        dashboardLoader.setLocation(dashboardResource);


        Parent dashboardRoot = dashboardLoader.load(dashboardResource.openStream());
        Scene dashboardScene = new Scene(dashboardRoot, 1200, 700);

        // Step 3: Get Dashboard controller and configure it with username
        DashboardController dashboardController = dashboardLoader.getController();
        dashboardController.setLoggedInUser(username);  // Set username BEFORE navigation setup
        dashboardController.setupNavigation(primaryStage, dashboardScene);

        primaryStage.setTitle("S-Emulator - Dashboard");
        primaryStage.setScene(dashboardScene);
        primaryStage.show();

        System.out.println("Application started - Dashboard loaded for user: " + username);
    }

    /**
     * Show modal login dialog and return the entered username.
     *
     * @param owner The parent stage
     * @return Username entered by user, or null if cancelled
     */
    private String showLoginDialog(@NotNull Stage owner) throws Exception {
        // Load Login FXML
        FXMLLoader loginLoader = new FXMLLoader();
        URL loginResource = getClass().getResource(LOGIN_PATH);
        if (loginResource == null) {
            throw new IllegalStateException("Login.fxml not found");
        }

        loginLoader.setLocation(loginResource);


        Parent loginRoot = loginLoader.load(loginResource.openStream());

        // Create modal dialog stage
        Stage loginStage = new Stage();
        loginStage.setTitle("S-Emulator - Login");
        loginStage.initModality(Modality.APPLICATION_MODAL);
        loginStage.initOwner(owner);

        Scene loginScene = new Scene(loginRoot, 400, 300);
        loginStage.setScene(loginScene);

        // Show and wait for dialog to close
        loginStage.showAndWait();

        // Get username from controller after dialog closes
        LoginController loginController = loginLoader.getController();
        return loginController.getEnteredUsername();
    }
}
