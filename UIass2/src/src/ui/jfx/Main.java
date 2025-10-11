package src.ui.jfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import ui.jfx.AppController;
import ui.jfx.dashboard.DashboardController;

import java.io.File;
import java.net.URL;

public class Main extends Application {
    private Stage primaryStage;
    private Scene dashboardScene;
    private Scene executionScene;

    public static void main(String[] args) {
        Thread.currentThread().setName("Main");
        launch(args);
    }

    @Override
    public void start(@NotNull Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("S-emulator");

        // Load Dashboard as the first screen
        loadDashboardScene();

        primaryStage.setScene(dashboardScene);
        primaryStage.show();
    }

    /**
     * Loads the Dashboard scene and sets up navigation to Execution screen
     */
    private void loadDashboardScene() throws Exception {
        FXMLLoader dashboardLoader = new FXMLLoader();
        URL dashboardResource = getClass().getResource("dashboard/Dashboard.fxml");
        dashboardLoader.setLocation(dashboardResource);

        if (dashboardResource == null) {
            throw new IllegalStateException("Dashboard.fxml not found");
        }

        Parent dashboardRoot = dashboardLoader.load(dashboardResource.openStream());
        dashboardScene = new Scene(dashboardRoot, 1200, 700);

        // Get Dashboard controller and set navigation callback
        DashboardController dashboardController = dashboardLoader.getController();
        dashboardController.setNavigateToExecutionCallback(this::navigateToExecution);

        System.out.println("Dashboard scene loaded successfully");
    }

    /**
     * Loads the Execution scene (lazy loading - only when needed)
     */
    private void loadExecutionScene() throws Exception {
        FXMLLoader executionLoader = new FXMLLoader();
        URL executionResource = getClass().getResource("app.fxml");
        executionLoader.setLocation(executionResource);

        if (executionResource == null) {
            throw new IllegalStateException("app.fxml not found");
        }

        Parent executionRoot = executionLoader.load(executionResource.openStream());
        executionScene = new Scene(executionRoot, 850, 500);

        // Get AppController and pass the scene reference for theme switching
        AppController appController = executionLoader.getController();
        appController.setScene(executionScene);

        System.out.println("Execution scene loaded successfully");
    }

    /**
     * Navigates from Dashboard to Execution screen with a file loaded
     */
    private void navigateToExecution(@NotNull File file) {
        try {
            if (executionScene == null) {
                loadExecutionScene();
            }

            // Switch to execution scene
            primaryStage.setScene(executionScene);
            primaryStage.setTitle("S-emulator - Execution");

            // Get AppController and load the file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("app.fxml"));
            AppController appController = loader.getController();
            if (appController != null) {
                appController.loadProgramFromFile(file);
            }

            System.out.println("Navigated to Execution screen with file: " + file.getName());

        } catch (Exception e) {
            System.err.println("Error navigating to Execution screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Returns from Execution screen back to Dashboard
     */
    private void returnToDashboard() {
        try {
            primaryStage.setScene(dashboardScene);
            primaryStage.setTitle("S-emulator - Dashboard");
            System.out.println("Returned to Dashboard");

        } catch (Exception e) {
            System.err.println("Error returning to Dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }
}