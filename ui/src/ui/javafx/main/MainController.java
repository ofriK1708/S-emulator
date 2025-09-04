package ui.javafx.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ui.javafx.fileloader.FileLoaderController;
import ui.javafx.cycles.CyclesController;
import system.controller.controller.SystemController;

/**
 * Main Controller - Senior level architecture
 * Manages communication between FileLoader and Cycles components
 */
public class MainController extends Application implements ComponentEventListener {

    // Components
    private FileLoaderController fileLoaderController;
    private CyclesController cyclesController;

    // Layout containers
    private BorderPane mainLayout;
    private VBox headerContainer;
    private VBox leftSideContainer;

    @Override
    public void start(Stage primaryStage) {
        try {
            initializeArchitecture();
            setupLayout();
            connectComponents();

            Scene scene = new Scene(mainLayout, 1000, 700);
            primaryStage.setTitle("Senior JavaFX Architecture - FileLoader & Cycles");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            handleInitializationError(e);
        }
    }

    /**
     * Initialize the main architecture components
     */
    private void initializeArchitecture() throws Exception {
        // Initialize main layout
        mainLayout = new BorderPane();
        headerContainer = new VBox();
        leftSideContainer = new VBox();

        // Load FileLoader component
        FXMLLoader fileLoaderFxml = new FXMLLoader(
                getClass().getResource("/ui/javafx/fileloader/FileLoader.fxml")
        );
        headerContainer.getChildren().add(fileLoaderFxml.load());
        fileLoaderController = fileLoaderFxml.getController();

        // Load Cycles component
        FXMLLoader cyclesFxml = new FXMLLoader(
                getClass().getResource("/ui/javafx/cycles/Cycles.fxml")
        );
        leftSideContainer.getChildren().add(cyclesFxml.load());
        cyclesController = cyclesFxml.getController();
    }

    /**
     * Setup the main layout structure
     */
    private void setupLayout() {
        // Configure header container
        headerContainer.setAlignment(Pos.TOP_LEFT);

        // Configure left side container for cycles
        leftSideContainer.setAlignment(Pos.CENTER_LEFT);
        leftSideContainer.setSpacing(20);
        leftSideContainer.setStyle("-fx-padding: 20px;");

        // Set components in BorderPane
        mainLayout.setTop(headerContainer);
        mainLayout.setLeft(leftSideContainer);

        // Main layout styling
        mainLayout.setStyle("-fx-background-color: #f8f9fa;");
    }

    /**
     * Connect components and establish communication
     */
    private void connectComponents() {
        // Register this main controller as event listener
        fileLoaderController.setEventListener(this);
        cyclesController.setEventListener(this);

        // Pass SystemController reference between components
        fileLoaderController.setMainController(this);
        cyclesController.setMainController(this);
    }

    // ===== COMPONENT EVENT HANDLING =====

    @Override
    public void onFileLoaded(SystemController systemController) {
        try {
            // Update cycles component when file is loaded
            int maxCycles = systemController.getMaxExpandLevel();
            cyclesController.updateCycles(maxCycles);

            System.out.println("MainController: File loaded, max cycles: " + maxCycles);

        } catch (Exception e) {
            handleComponentError("Failed to update cycles after file load", e);
        }
    }

    @Override
    public void onFileCleaned() {
        try {
            // Reset cycles component when file is cleaned
            cyclesController.resetCycles();

            System.out.println("MainController: File cleaned, cycles reset");

        } catch (Exception e) {
            handleComponentError("Failed to reset cycles after clean", e);
        }
    }

    @Override
    public void onCyclesChanged(int newCycleCount) {
        // Handle cycles component changes if needed
        System.out.println("MainController: Cycles changed to: " + newCycleCount);
    }

    // ===== COMPONENT ACCESS METHODS =====

    /**
     * Get FileLoader controller instance
     */
    public FileLoaderController getFileLoaderController() {
        return fileLoaderController;
    }

    /**
     * Get Cycles controller instance
     */
    public CyclesController getCyclesController() {
        return cyclesController;
    }

    // ===== ERROR HANDLING =====

    private void handleInitializationError(Exception e) {
        System.err.println("Failed to initialize main controller: " + e.getMessage());
        e.printStackTrace();
    }

    private void handleComponentError(String message, Exception e) {
        System.err.println("Component error: " + message + " - " + e.getMessage());
        e.printStackTrace();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
