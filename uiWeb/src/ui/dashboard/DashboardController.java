package ui.dashboard;

import dto.engine.ExecutionResultStatisticsDTO;
import dto.engine.FunctionMetadata;
import dto.engine.ProgramMetadata;
import dto.server.UserDTO;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import system.controller.EngineController;
import system.controller.HttpEngineController;
import ui.dashboard.functions.FunctionsPanelController;
import ui.dashboard.header.DashboardHeaderController;
import ui.dashboard.history.table.HistoryPanelController;
import ui.dashboard.programs.ProgramsPanelController;
import ui.dashboard.users.UsersPanelController;
import ui.execution.ExecutionController;
import ui.refresher.FunctionTableRefresher;
import ui.refresher.ProgramsTableRefresher;
import ui.refresher.UserTableRefresher;
import ui.task.program.ProgramTaskController;

import java.io.File;
import java.net.URL;
import java.util.Timer;

import static ui.utils.UIUtils.*;
import static ui.utils.clientConstants.*;

/**
 * Main controller for the Dashboard screen.
 * Coordinates all sub-panels and manages navigation to execution screen.
 */
public class DashboardController {

    // Shared properties
    private final StringProperty selectedUser = new SimpleStringProperty(null);
    private final IntegerProperty availableCredits = new SimpleIntegerProperty(0);
    private final BooleanProperty userSelected = new SimpleBooleanProperty(false);
    private final BooleanProperty fileLoaded = new SimpleBooleanProperty(false);
    private final BooleanProperty programLoaded = new SimpleBooleanProperty(false);
    private final StringProperty loggedInUserName = new SimpleStringProperty("Guest User");

    private final ListProperty<ProgramMetadata> programsMetadataListProperty =
            new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<FunctionMetadata> functionsMetadataListProperty =
            new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<UserDTO> usersList = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<ExecutionResultStatisticsDTO> userHistoryListProperty =
            new SimpleListProperty<>(FXCollections.observableArrayList());
    private String originalUser = "";

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
    private FXMLLoader executionLoader;

    // Engine controller for Dashboard data preview
    private final EngineController engineController;

    public DashboardController() {
        // Initialize engine controller for Dashboard data preview
        this.engineController = new HttpEngineController();
    }

    /**
     * Set the logged-in username.
     *
     * @param username The logged-in username
     */
    public void setLoggedInUser(@NotNull String username) {
        this.loggedInUserName.set(username);
        this.originalUser = username;
        System.out.println("Dashboard: Logged in user set to - " + username);
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
                if ((oldVal == null && newVal != null) || (oldVal != null && !oldVal.equals(newVal))) {
                    userSelected.set(newVal != null && !newVal.isEmpty());
                    System.out.println("Dashboard: User selected - " + newVal);

                    if (newVal != null) {
                        loadUserHistory(newVal);
                    }
                } else {
                    System.out.println("old val is " + oldVal + " new val is " + newVal);
                }
            });

            // Initialize sub-controllers
            initializeSubControllers();
            initializeExecutionScreen();

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
                new UserTableRefresher(engineController, usersList);
        Timer programsTimer = new Timer();
        Timer functionsTimer = new Timer();
        Timer usersTimer = new Timer();
        programsTimer.schedule(programsTableRefresher, PROGRAMS_TABLE_REFRESH_RATE, PROGRAMS_TABLE_REFRESH_RATE);
        functionsTimer.schedule(functionTableRefresher, FUNCTIONS_TABLE_REFRESH_RATE, FUNCTIONS_TABLE_REFRESH_RATE);
        usersTimer.schedule(userTableRefresher, 0, USERS_TABLE_REFRESH_RATE);

    }

    private void initializeExecutionScreen() {
        try {
            executionLoader = loadExecutionLoader();
            Parent executionRoot = executionLoader.load();
            executionScene = new Scene(executionRoot, 1400, 800);
        } catch (Exception e) {
            System.err.println("Dashboard: Error initializing Execution screen - " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeSubControllers() {
        // Header: file loading and credits
        headerSectionController.initComponent(
                loggedInUserName,
                availableCredits,
                this::handleFileLoadFromDashboard,
                this::handleChargeCredits
        );

        // Programs panel: set engine and execution callback
        programsPanelController.initComponent(this::handleLoadProgramToExecution,
                programsMetadataListProperty// Navigation happens here
        );

        // Functions panel: set engine and execution callback
        functionsPanelController.initComponent(
                functionsMetadataListProperty,
                this::handleLoadProgramOrFunctionToExecution  // Navigation happens here
        );

        // History panel: user statistics
        historyPanelController.initComponent(userHistoryListProperty, this::onRerunRequested);

        usersPanelController.initComponent(usersList, () -> selectedUser.set(originalUser), selectedUser);
    }

    /**
     * Handle file loading from Dashboard.
     * CRITICAL: This now ONLY loads the file - NO navigation to execution screen.
     * Navigation happens only when execute buttons are pressed.
     */
    private void handleFileLoadFromDashboard(@NotNull File file) {
        try {
            System.out.println("Dashboard: Loading file " + file.getName());

            FXMLLoader loader = new FXMLLoader(getClass().getResource(TASK_PATH));
            Parent root = loader.load();
            ProgramTaskController programTaskController = loader.getController();

            Stage loadingStage = createTaskLoadingStage("uploading File: " + file.getName(), root);

            loadingStage.setOnShown(event -> programTaskController.initializeAndRunUploadTaskThread(
                    file.toPath(), engineController,
                    (isSucceeded) -> {
                        if (isSucceeded) {
                            fileLoaded.set(true);
                            programLoaded.set(true);
                            showSuccess("File loaded successfully: " + file.getName());
                        } else {
                            fileLoaded.set(false);
                            programLoaded.set(false);
                            showError("File loading failed: " + file.getName());
                        }
                        loadingStage.close();
                    }));

            loadingStage.show();
            // Store the file path for later use by Execution screen


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
    private void handleLoadProgramOrFunctionToExecution(@NotNull String programName) {
        try {
            System.out.println("Dashboard: Executing program '" + programName + "'");

            configureExecutionController(programName);
            setStageAndShow();
            executionController.loadProgramToExecution(programName);

        } catch (Exception e) {
            System.err.println("Dashboard: Error executing program - " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleLoadProgramToExecution(@NotNull String programName, float avgCreditCost) {
        if (availableCredits.get() < avgCreditCost) {
            showError("Insufficient credits to execute the program. you have " + availableCredits.get() +
                    " but average cost is " + avgCreditCost + " Please charge more credits.");
            System.out.println("Dashboard: Insufficient credits to execute program '" + programName + "'");
            return;
        }
        handleLoadProgramOrFunctionToExecution(programName);
    }

    /**
     * Load the Execution scene and AppController (lazy initialization)
     */
    private FXMLLoader loadExecutionLoader() {
        System.out.println("Dashboard: Loading Execution scene...");

        FXMLLoader executionLoader = new FXMLLoader();
        URL url = getClass().getResource(EXECUTION_PATH);
        if (url == null) {
            throw new IllegalStateException("Execution.fxml not found");
        }
        executionLoader.setLocation(url);
        return executionLoader;
    }

    private void onRerunRequested(@NotNull ExecutionResultStatisticsDTO selectedExecutionResultStatisticsDTO) {
        try {
            String programName = selectedExecutionResultStatisticsDTO.displayName();
            System.out.println("Dashboard: Re-running program '" + programName + "'");

            configureExecutionController(programName);
            setStageAndShow();
            executionController.loadProgramToExecution(programName, selectedExecutionResultStatisticsDTO);

        } catch (Exception e) {
            System.err.println("Dashboard: Error re-running program - " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void setStageAndShow() {

        Stage loadingStage = new Stage();
        loadingStage.initModality(Modality.APPLICATION_MODAL);
        loadingStage.setTitle("Loading Execution Scene");
        loadingStage.setScene(executionScene);
        executionController.setStage(loadingStage);
        loadingStage.show();
    }

    private void configureExecutionController(String programName) {
        // Get AppController and configure it
        executionController = executionLoader.getController();
        if (executionController == null) {
            throw new IllegalStateException("Dashboard: ExecutionController is null!");
        }
        executionController.setUserName(loggedInUserName.get());
        executionController.setScreenTitle("S-Emulator - Execution: " + programName);
        executionController.setAvailableCredits(availableCredits);

        // Set up return-to-dashboard callback
        executionController.setReturnToDashboardCallback(this::transitionToDashboardScreen);
    }

    /**
     * Transition from Execution back to Dashboard screen
     */
    private void transitionToDashboardScreen() {
        if (dashboardScene != null && primaryStage != null) {
            primaryStage.setScene(dashboardScene);
            primaryStage.setTitle("S-Emulator - Dashboard");
            checkIfShouldFetchUserHistory();

            System.out.println("Dashboard: Switched back to Dashboard screen");
        } else {
            System.err.println("Dashboard: Cannot transition - dashboard scene not available");
        }
    }

    private void checkIfShouldFetchUserHistory() {
        System.out.println("Dashboard: Checking if user history needs refresh for user '" + originalUser + "'");
        System.out.println("originalUser: " + originalUser + ", selectedUser: " + selectedUser.get());
        if (originalUser.equals(selectedUser.get()) || selectedUser.get() == null) {
            System.out.println("Dashboard: Refreshing user history for '" + originalUser + "'");
            loadUserHistory(originalUser);
        }
    }

    /**
     * Handle credits charging
     */
    private void handleChargeCredits(int newAmount) {
        availableCredits.set(newAmount);
        engineController.setUserCreditsAsync(originalUser, newAmount, (response) -> {
            if (response.isSuccess()) {
                Platform.runLater(() -> showSuccess("Credits updated successfully to " + newAmount));
                System.out.println("Dashboard: Credits updated successfully to " + newAmount);
            } else {
                Platform.runLater(() -> showError("Failed to update credits: " + response.message()));
                System.err.println("Dashboard: Error updating credits - " + response.message());
            }
        });
        System.out.println("Dashboard: Credits charged. New balance: " + availableCredits.get());
    }

    /**
     * Load user execution history
     */
    private void loadUserHistory(@NotNull String username) {
        System.out.println("Dashboard: Loading history for user '" + username + "'");
        engineController.FetchUserExecutionHistoryAsync(username, (response) -> {
            if (response.isSuccess()) {
                Platform.runLater(() -> userHistoryListProperty.setAll(response.getSafeUserStatisticsDTOList()));
                System.out.println("Dashboard: User history loaded successfully for '" + username + "'");
            } else {
                Platform.runLater(() -> showError("Failed to load user history: " + response.message()));
                System.err.println("Dashboard: Error loading user history - " + response.message());
            }
        });
    }
}