package ui.web.jfx.dashboard;

import dto.engine.FunctionMetadata;
import dto.engine.ProgramMetadata;
import dto.server.SystemResponse;
import dto.server.UserDTO;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import system.controller.EngineController;
import system.controller.LocalEngineController;
import ui.refresher.FunctionTableRefresher;
import ui.refresher.ProgramsTableRefresher;
import ui.refresher.UserTableRefresher;
import ui.web.jfx.ExecutionController;
import ui.web.jfx.dashboard.functions.FunctionsPanelController;
import ui.web.jfx.dashboard.header.DashboardHeaderController;
import ui.web.jfx.dashboard.history.HistoryPanelController;
import ui.web.jfx.dashboard.programs.ProgramsPanelController;
import ui.web.jfx.dashboard.users.UsersPanelController;

import java.io.File;
import java.net.URL;
import java.util.Timer;
import java.util.function.Consumer;

import static ui.web.utils.UIUtils.showError;
import static ui.web.utils.UIUtils.showSuccess;
import static ui.web.utils.clientConstants.*;

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
    private final BooleanProperty programLoaded = new SimpleBooleanProperty(false);

    private final ListProperty<ProgramMetadata> programsMetadataListProperty = new SimpleListProperty<>();
    private final ListProperty<FunctionMetadata> functionsMetadataListProperty = new SimpleListProperty<>();
    private final ListProperty<UserDTO> usersMetadataListProperty = new SimpleListProperty<>();

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
    private ExecutionController executionController;

    // Engine controller for Dashboard data preview
    private final EngineController engineController;

    public DashboardController() {
        // Initialize engine controller for Dashboard data preview
        this.engineController = new LocalEngineController();
    }

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
            startRefreshers();
            System.out.println("DashboardController: Data refreshers started");

            // Bind user selection across panels
            selectedUser.addListener((obs, oldVal, newVal) -> {
                userSelected.set(newVal != null && !newVal.isEmpty());
                System.out.println("Dashboard: User selected - " + newVal);

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

    private void startRefreshers() {
        ProgramsTableRefresher programsTableRefresher =
                new ProgramsTableRefresher(engineController, programsMetadataListProperty);
        FunctionTableRefresher functionTableRefresher =
                new FunctionTableRefresher(engineController, functionsMetadataListProperty);
        UserTableRefresher userTableRefresher =
                new UserTableRefresher(engineController, usersMetadataListProperty);
        Timer programsTimer = new Timer();
        Timer functionsTimer = new Timer();
        Timer usersTimer = new Timer();
        programsTimer.schedule(programsTableRefresher, PROGRAMS_TABLE_REFRESH_RATE, PROGRAMS_TABLE_REFRESH_RATE);
        functionsTimer.schedule(functionTableRefresher, FUNCTIONS_TABLE_REFRESH_RATE, FUNCTIONS_TABLE_REFRESH_RATE);
        usersTimer.schedule(userTableRefresher, 0, USERS_TABLE_REFRESH_RATE);

    }

    private void initializeSubControllers() {
        // Header: file loading and credits
        headerSectionController.initComponent(
                currentFilePath,
                availableCredits,
                this::handleFileLoadFromDashboard,
                this::handleChargeCredits
        );

        // Programs panel: set engine and execution callback
        programsPanelController.initComponent(this::handleProgramExecution,
                programsMetadataListProperty// Navigation happens here
        );

        // Functions panel: set engine and execution callback
        functionsPanelController.initComponent(
                functionsMetadataListProperty,
                this::handleFunctionExecution  // Navigation happens here
        );

        // History panel: user statistics
        historyPanelController.initComponent(selectedUser);

        // Initial data load
        loadAvailableUsers();
    }

    /**
     * Handle file loading from Dashboard.
     * CRITICAL: This now ONLY loads the file - NO navigation to execution screen.
     * Navigation happens only when execute buttons are pressed.
     */
    private void handleFileLoadFromDashboard(@NotNull File file) {
        try {
            System.out.println("Dashboard: Loading file " + file.getName());

            // Load the file using Dashboard's engine controller for preview
            engineController.LoadProgramFromFileAsync(file.toPath(), (Consumer<SystemResponse>)
                    systemResponse -> {
                        if (systemResponse.isSuccess()) {
                            // Update programs and functions metadata lists
                            showSuccess(systemResponse.message());
                        } else {
                            showError(systemResponse.message());
                        }
                    });

            // Store the file path for later use by Execution screen
            currentFilePath.set(file.getAbsolutePath());
            fileLoaded.set(true);
            programLoaded.set(true);

            System.out.println("Dashboard: File loaded successfully - " + file.getName());
            System.out.println("Dashboard: Staying on Dashboard - awaiting execute button press");

        } catch (Exception e) {
            System.err.println("Dashboard: Error loading file - " + e.getMessage());
            e.printStackTrace();
            fileLoaded.set(false);
            programLoaded.set(false);
        }
    }

    /**
     * Handle program execution request from Programs panel.
     * CRITICAL: This is where navigation to Execution screen happens for programs.
     */
    private void handleProgramExecution(@NotNull String programName) {
        if (!programLoaded.get() || currentFilePath.get().isEmpty()) {
            System.err.println("Dashboard: Cannot execute - no program loaded");
            return;
        }

        try {
            System.out.println("Dashboard: Executing program '" + programName + "'");

            loadExecutionScene(programName);
            File programFile = new File(currentFilePath.get());
            executionController.loadProgramToExecution(programFile, () -> {
                // After file is loaded in AppController's engine, navigate
                transitionToExecutionScreen();
                System.out.println("Dashboard: Navigated to Execution screen for program: " + programName);
            });

        } catch (Exception e) {
            System.err.println("Dashboard: Error executing program - " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadExecutionScene(@NotNull String programName) throws Exception {
        // Load Execution scene if not already loaded
        if (executionScene == null || executionController == null) {
            loadExecutionScene();
        }

        // Configure execution screen with user info BEFORE loading file
        executionController.setUserName(selectedUser.get() != null ? selectedUser.get() : "Guest User");
        executionController.setScreenTitle("S-Emulator - Execution: " + programName);
        executionController.setAvailableCredits(availableCredits.get());

    }

    /**
     * Handle function execution request from Functions panel.
     * CRITICAL: This is where navigation to Execution screen happens for functions.
     */
    private void handleFunctionExecution(@NotNull String functionName) {
        if (!programLoaded.get() || currentFilePath.get().isEmpty()) {
            System.err.println("Dashboard: Cannot execute - no program loaded");
            return;
        }

        try {
            System.out.println("Dashboard: Executing function '" + functionName + "'");

            // Load Execution scene if not already loaded
            File programFile = loadExecutionScene(functionName);
            executionController.loadProgramToExecution(programFile, () -> {
                // After file is loaded, switch to the function
                try {
                    executionController.switchLoadedProgram(functionName);
                } catch (Exception e) {
                    System.err.println("Dashboard: Error switching to function: " + e.getMessage());
                }

                // Navigate to execution screen
                transitionToExecutionScreen();
                System.out.println("Dashboard: Navigated to Execution screen for function: " + functionName);
            });

        } catch (Exception e) {
            System.err.println("Dashboard: Error executing function - " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load the Execution scene and AppController (lazy initialization)
     */
    private void loadExecutionScene() throws Exception {
        System.out.println("Dashboard: Loading Execution scene...");

        FXMLLoader executionLoader = new FXMLLoader();
        URL url = getClass().getResource("/ui/web/jfx/execution.fxml");
        assert url != null;
        executionLoader.setLocation(url);

        Parent executionRoot = executionLoader.load();
        executionScene = new Scene(executionRoot, 1400, 800);

        // Get AppController and configure it
        executionController = executionLoader.getController();
        executionController.setScene(executionScene);

        // Set up return-to-dashboard callback
        executionController.setReturnToDashboardCallback(this::transitionToDashboardScreen);

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

            // Refresh dashboard panels when returning
            refreshDashboardPanels();

            System.out.println("Dashboard: Switched back to Dashboard screen");
        } else {
            System.err.println("Dashboard: Cannot transition - dashboard scene not available");
        }
    }

    /**
     * Handle credits charging
     */
    private void handleChargeCredits(int amount) {
        int current = availableCredits.get();
        availableCredits.set(current + amount);
        System.out.println("Dashboard: Credits charged. New balance: " + availableCredits.get());
    }

    /**
     * Load available users from server
     */
    private void loadAvailableUsers() {
        // TODO: Integrate with user management server
    }

    /**
     * Load user execution history
     */
    private void loadUserHistory(@NotNull String username) {
        // TODO: Integrate with history management server
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
        programLoaded.set(false);
        usersPanelController.clearSelection();
        programsPanelController.clearPrograms();
        functionsPanelController.clearFunctions();
        historyPanelController.clearHistory();
        System.out.println("Dashboard: Cleared all state");
    }
}