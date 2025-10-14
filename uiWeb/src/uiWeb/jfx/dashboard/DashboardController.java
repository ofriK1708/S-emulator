package uiWeb.jfx.dashboard;

import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import uiWeb.jfx.AppController;
import uiWeb.jfx.dashboard.functions.FunctionsPanelController;
import uiWeb.jfx.dashboard.header.DashboardHeaderController;
import uiWeb.jfx.dashboard.history.HistoryPanelController;
import uiWeb.jfx.dashboard.programs.ProgramsPanelController;
import uiWeb.jfx.dashboard.users.UsersPanelController;

import java.io.File;
import java.net.URL;

/**
 * Main controller for the Dashboard screen.
 * Coordinates all sub-panels and manages navigation to execution screen.
 */
public class DashboardController {

    // Shared properties
    private final StringProperty selectedUser = new SimpleStringProperty(null);
    private final StringProperty currentFilePath = new SimpleStringProperty("");
    private final IntegerProperty availableCredits = new SimpleIntegerProperty(0);
    private final BooleanProperty userSelected = new SimpleBooleanProperty(false);
    private final BooleanProperty fileLoaded = new SimpleBooleanProperty(false);
    @FXML
    private HBox headerSection;
    @FXML
    private DashboardHeaderController headerSectionController;
    @FXML
    private AnchorPane usersPanel;
    @FXML
    private UsersPanelController usersPanelController;
    @FXML
    private AnchorPane programsPanel;
    @FXML
    private ProgramsPanelController programsPanelController;
    @FXML
    private AnchorPane functionsPanel;
    @FXML
    private FunctionsPanelController functionsPanelController;
    @FXML
    private AnchorPane historyPanel;
    @FXML
    private HistoryPanelController historyPanelController;
    // Navigation references
    private Stage primaryStage;
    private Scene dashboardScene;
    private Scene executionScene;
    private AppController appController;

    public DashboardController() {
        // Empty constructor for FXML
    }

    /**
     * Setup navigation between Dashboard and Execution screens.
     * Called from Main.java after Dashboard is loaded.
     */
    public void setupNavigation(@NotNull Stage primaryStage, @NotNull Scene dashboardScene) {
        this.primaryStage = primaryStage;
        this.dashboardScene = dashboardScene;
        System.out.println("DashboardController: Navigation configured");
    }

    @FXML
    public void initialize() {
        if (headerSectionController != null && usersPanelController != null &&
                programsPanelController != null && functionsPanelController != null &&
                historyPanelController != null) {

            System.out.println("DashboardController: All sub-controllers injected successfully");

            // Bind user selection across panels
            selectedUser.addListener((obs, oldVal, newVal) -> {
                userSelected.set(newVal != null && !newVal.isEmpty());
                System.out.println("Dashboard: User selected - " + newVal);

                // Refresh history when user changes
                if (newVal != null) {
                    loadUserHistory(newVal);
                }
            });

            // Initialize sub-controllers
            initializeSubControllers();

        } else {
            System.err.println("DashboardController: One or more sub-controllers are null!");
            throw new IllegalStateException("FXML injection failed for Dashboard sub-controllers");
        }
    }

    private void initializeSubControllers() {
        // Header: file loading and credits
        // CRITICAL: Pass handleFileLoadFromDashboard which triggers Execution transition
        headerSectionController.initComponent(
                currentFilePath,
                availableCredits,
                this::handleFileLoadFromDashboard,  // This will trigger the file loader + transition
                this::handleChargeCredits
        );

//        // Users panel: selection and management
//        usersPanelController.initComponent(
//                selectedUser,
//                this::loadAvailableUsers
//        );
//
//        // Programs panel: program selection and execution (disabled - no direct execution from Dashboard)
//        programsPanelController.initComponent(
//                userSelected,
//                fileLoaded,
//                this::showProgramExecutionMessage
//        );
//
//        // Functions panel: function selection and execution (disabled - no direct execution from Dashboard)
//        functionsPanelController.initComponent(
//                userSelected,
//                fileLoaded,
//                this::showFunctionExecutionMessage
//        );

        // History panel: user statistics
        historyPanelController.initComponent(
                selectedUser
        );

        // Initial data load
        loadAvailableUsers();
    }

    /**
     * CRITICAL METHOD: Handles file loading from Dashboard.
     * This method:
     * 1. Loads the Execution scene if not already loaded
     * 2. Uses AppController's existing file loader mechanism
     * 3. Automatically transitions to Execution screen after successful load
     */
    private void handleFileLoadFromDashboard(@NotNull File file) {
        try {
            System.out.println("Dashboard: Loading file " + file.getName() + " and preparing transition to Execution");

            // Step 1: Load Execution scene if not already loaded
            if (executionScene == null || appController == null) {
                loadExecutionScene();
            }

            // Step 2: Use AppController's existing file loader with transition callback
            appController.loadProgramFromFileExternal(file, () -> {
                // This callback executes AFTER successful file load
                transitionToExecutionScreen();
                currentFilePath.set(file.getAbsolutePath());
                fileLoaded.set(true);
                System.out.println("Dashboard: File loaded and transitioned to Execution screen");
            });

        } catch (Exception e) {
            System.err.println("Dashboard: Error loading file - " + e.getMessage());
            e.printStackTrace();
            fileLoaded.set(false);
        }
    }

    /**
     * Load the Execution scene and AppController (lazy initialization)
     */
    private void loadExecutionScene() throws Exception {
        System.out.println("Dashboard: Loading Execution scene...");

        FXMLLoader executionLoader = new FXMLLoader();
        URL url = getClass().getResource("/uiWeb/jfx/execution.fxml");
        assert url != null;
        executionLoader.setLocation(url);

        Parent executionRoot = executionLoader.load();
        executionScene = new Scene(executionRoot, 1400, 800);

        // Get AppController and configure it
        appController = executionLoader.getController();
        appController.setScene(executionScene);

        // Set up return-to-dashboard callback
        appController.setReturnToDashboardCallback(this::transitionToDashboardScreen);

        System.out.println("Dashboard: Execution scene loaded successfully");
    }

    /**
     * Transition from Dashboard to Execution screen
     */
    private void transitionToExecutionScreen() {
        if (executionScene != null && primaryStage != null) {
            primaryStage.setScene(executionScene);
            primaryStage.setTitle("S-Emulator - Execution");
            System.out.println("Dashboard: Switched to Execution screen");
        } else {
            System.err.println("Dashboard: Cannot transition - execution scene not loaded");
        }
    }

    /**
     * Transition from Execution back to Dashboard screen
     */
    private void transitionToDashboardScreen() {
        if (dashboardScene != null && primaryStage != null) {
            primaryStage.setScene(dashboardScene);
            primaryStage.setTitle("S-Emulator - Dashboard");
            System.out.println("Dashboard: Switched back to Dashboard screen");
        } else {
            System.err.println("Dashboard: Cannot transition - dashboard scene not available");
        }
    }

    /**
     * Handle credits charging
     */
    private void handleChargeCredits(int amount) {
        // TODO: Integrate with credit management system
        int current = availableCredits.get();
        availableCredits.set(current + amount);
        System.out.println("Dashboard: Credits charged. New balance: " + availableCredits.get());
    }

    /**
     * Show message that program execution requires loading file first
     */
    private void showProgramExecutionMessage(@NotNull String programName) {
        System.out.println("Dashboard: Program execution '" + programName +
                "' - User must load file from Dashboard first");
    }

    /**
     * Show message that function execution requires loading file first
     */
    private void showFunctionExecutionMessage(@NotNull String functionName) {
        System.out.println("Dashboard: Function execution '" + functionName +
                "' - User must load file from Dashboard first");
    }

    /**
     * Load available users from system
     */
    private void loadAvailableUsers() {
        // TODO: Integrate with user management system
        // For now, return mock data
    }

    /**
     * Load user execution history
     */
    private void loadUserHistory(@NotNull String username) {
        // TODO: Integrate with history management system
        System.out.println("Dashboard: Loading history for user '" + username + "'");
        historyPanelController.refreshHistory();
    }

    /**
     * Clear all dashboard state (when logging out or resetting)
     */
    public void clearDashboard() {
        selectedUser.set(null);
        currentFilePath.set("");
        fileLoaded.set(false);
        usersPanelController.clearSelection();
        programsPanelController.clearPrograms();
        functionsPanelController.clearFunctions();
        historyPanelController.clearHistory();
        System.out.println("Dashboard: Cleared all state");
    }
}