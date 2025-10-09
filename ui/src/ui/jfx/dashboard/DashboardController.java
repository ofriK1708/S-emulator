package ui.jfx.dashboard;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import org.jetbrains.annotations.NotNull;
import ui.jfx.dashboard.functions.FunctionsPanelController;
import ui.jfx.dashboard.header.DashboardHeaderController;
import ui.jfx.dashboard.history.HistoryPanelController;
import ui.jfx.dashboard.programs.ProgramsPanelController;
import ui.jfx.dashboard.users.UsersPanelController;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

/**
 * Main controller for the Dashboard screen.
 * Coordinates all sub-panels and manages navigation to execution screen.
 */
public class DashboardController {

    @FXML private HBox headerSection;
    @FXML private DashboardHeaderController headerSectionController;

    // SKIP UsersPanel for now as per requirements
    @FXML private AnchorPane usersPanel;
    @FXML private UsersPanelController usersPanelController;

    @FXML private AnchorPane programsPanel;
    @FXML private ProgramsPanelController programsPanelController;

    @FXML private AnchorPane functionsPanel;
    @FXML private FunctionsPanelController functionsPanelController;

    @FXML private AnchorPane historyPanel;
    @FXML private HistoryPanelController historyPanelController;

    // Shared properties
    private final StringProperty selectedUser = new SimpleStringProperty(null);
    private final StringProperty currentFilePath = new SimpleStringProperty("");
    private final IntegerProperty availableCredits = new SimpleIntegerProperty(0);
    private final BooleanProperty userSelected = new SimpleBooleanProperty(false);
    private final BooleanProperty fileLoaded = new SimpleBooleanProperty(false);

    // Navigation callback to switch to execution screen
    private Consumer<File> navigateToExecution;

    public DashboardController() {
        // Empty constructor for FXML
    }

    @FXML
    public void initialize() {
        if (headerSectionController != null && programsPanelController != null &&
                functionsPanelController != null && historyPanelController != null) {

            System.out.println("DashboardController: Core sub-controllers injected successfully");

            // Initialize sub-controllers
            initializeSubControllers();

        } else {
            System.err.println("DashboardController: One or more core sub-controllers are null!");
            throw new IllegalStateException("FXML injection failed for Dashboard sub-controllers");
        }
    }

  private void initializeSubControllers() {
//        // Header: file loading and credits
//        headerSectionController.initComponent(
//                currentFilePath,
//                availableCredits,
//                this::handleFileLoad,
//                this::handleChargeCredits
//        );
//
//        // Users panel can be activated later when needed
//        if (usersPanelController != null) {
//            // Placeholder - skip for now
//            System.out.println("UsersPanel: Skipped (not implemented)");
//
//            // For now, automatically set a default "user" so other features work
//            selectedUser.set("DefaultUser");
//            userSelected.set(true);
//        }
//
//        // Programs panel: program selection and execution
//        programsPanelController.initComponent(
//                userSelected,
//                fileLoaded,
//                this::executeProgram
//        );
//
//        // Functions panel: function selection and execution
//        functionsPanelController.initComponent(
//                userSelected,
//                fileLoaded,
//                this::executeFunction
//        );
//
//        // History panel: user statistics
//        historyPanelController.initComponent(selectedUser);
//
//        System.out.println("DashboardController: Initialization complete");
    }

    /**
     * Public method to set navigation callback from Main.java.
     * This allows Dashboard to navigate to the Execution screen.
     */
    public void setNavigateToExecutionCallback(@NotNull Consumer<File> callback) {
        this.navigateToExecution = callback;
        System.out.println("Dashboard: Navigation callback registered");
    }

    /**
     * Handle file load from header section
     */
    private void handleFileLoad(@NotNull File file) {
        try {
            if (!file.exists() || !file.isFile()) {
                System.err.println("Dashboard: Invalid file - " + file.getAbsolutePath());
                return;
            }

            currentFilePath.set(file.getAbsolutePath());
            fileLoaded.set(true);

            // Load programs and functions from file
            loadProgramsFromFile(file);
            loadFunctionsFromFile(file);

            System.out.println("Dashboard: File loaded - " + file.getName());

        } catch (Exception e) {
            System.err.println("Dashboard: Error loading file - " + e.getMessage());
            fileLoaded.set(false);
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
     * Execute selected program and navigate to execution screen
     */
    private void executeProgram(@NotNull String programName) {
        if (!fileLoaded.get()) {
            System.err.println("Dashboard: No file loaded");
            return;
        }

        System.out.println("Dashboard: Executing program '" + programName + "'");

        // Navigate to execution screen with loaded file
        if (navigateToExecution != null) {
            File file = new File(currentFilePath.get());
            navigateToExecution.accept(file);
        } else {
            System.err.println("Dashboard: Navigation callback not set!");
        }
    }

    /**
     * Execute selected function and navigate to execution screen
     */
    private void executeFunction(@NotNull String functionName) {
        if (!fileLoaded.get()) {
            System.err.println("Dashboard: No file loaded");
            return;
        }

        System.out.println("Dashboard: Executing function '" + functionName + "'");

        // Navigate to execution screen with loaded file
        if (navigateToExecution != null) {
            File file = new File(currentFilePath.get());
            navigateToExecution.accept(file);
        } else {
            System.err.println("Dashboard: Navigation callback not set!");
        }
    }

    /**
     * Load programs from file
     */
    private void loadProgramsFromFile(@NotNull File file) {
    }

    /**
     * Load functions from file
     */
    private void loadFunctionsFromFile(@NotNull File file) {

    }

    /**
     * Clear all dashboard state (when logging out or resetting)
     */
    public void clearDashboard() {
        currentFilePath.set("");
        fileLoaded.set(false);
        programsPanelController.clearPrograms();
        functionsPanelController.clearFunctions();
        historyPanelController.clearHistory();
        System.out.println("Dashboard: Cleared all state");
    }
}