package ui.jfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import ui.jfx.dashboard.DashboardController;

import java.net.URL;

/**
 * Main application entry point.
 * Loads Dashboard as the first screen.
 */
public class Main extends Application {

    public static void main(String[] args) {
        Thread.currentThread().setName("Main");
        launch(args);
    }

    @Override
    public void start(@NotNull Stage primaryStage) throws Exception {
        // Load Dashboard as the initial screen
        FXMLLoader dashboardLoader = new FXMLLoader();
        URL dashboardResource = getClass().getResource("dashboard/Dashboard.fxml");
        dashboardLoader.setLocation(dashboardResource);

        if (dashboardResource == null) {
            throw new IllegalStateException("Dashboard.fxml not found");
        }

        Parent dashboardRoot = dashboardLoader.load(dashboardResource.openStream());
        Scene dashboardScene = new Scene(dashboardRoot, 1200, 700);

        // Get Dashboard controller and set up navigation
        DashboardController dashboardController = dashboardLoader.getController();
        dashboardController.setupNavigation(primaryStage, dashboardScene);

        primaryStage.setTitle("S-Emulator - Dashboard");
        primaryStage.setScene(dashboardScene);
        primaryStage.show();

        System.out.println("Application started - Dashboard loaded");
    }
}