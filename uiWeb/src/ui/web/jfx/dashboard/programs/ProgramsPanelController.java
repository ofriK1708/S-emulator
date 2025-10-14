package ui.web.jfx.dashboard.programs;

import dto.engine.ExecutionStatisticsDTO;
import dto.engine.ProgramDTO;
import dto.ui.ProgramDashboardDTO;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.jetbrains.annotations.NotNull;
import system.controller.EngineController;

import java.util.function.Consumer;

import static com.sun.javafx.util.Utils.sum;

/**
 * Controller for the Programs Panel.
 * Displays available programs with their metadata and execution statistics.
 */
public class ProgramsPanelController {

    private final ObservableList<ProgramDashboardDTO> programsList = FXCollections.observableArrayList();

    @FXML
    private TableView<ProgramDashboardDTO> programsTableView;
    @FXML
    private TableColumn<ProgramDashboardDTO, String> programNameColumn;
    @FXML
    private TableColumn<ProgramDashboardDTO, String> uploadedByColumn;
    @FXML
    private TableColumn<ProgramDashboardDTO, Number> instructionCountColumn;
    @FXML
    private TableColumn<ProgramDashboardDTO, Number> maxExpandLevelColumn;
    @FXML
    private TableColumn<ProgramDashboardDTO, Number> totalRunsColumn;
    @FXML
    private TableColumn<ProgramDashboardDTO, Number> avgCreditCostColumn;
    @FXML
    private Button executeProgramButton;

    private Consumer<String> executeProgramCallback;
    private EngineController engineController;
    private BooleanProperty programLoadedProperty;

    @FXML
    public void initialize() {
        setupTableColumns();

        // Enable/disable execute button based on selection
        executeProgramButton.disableProperty().bind(
                programsTableView.getSelectionModel().selectedItemProperty().isNull()
        );

        System.out.println("ProgramsPanelController initialized");
    }

    private void setupTableColumns() {
        // Bind columns to ProgramDashboardDTO properties
        programNameColumn.setCellValueFactory(cellData ->
                cellData.getValue().programName());
        uploadedByColumn.setCellValueFactory(cellData ->
                cellData.getValue().uploadedBy());
        instructionCountColumn.setCellValueFactory(cellData ->
                cellData.getValue().instructionCount());
        maxExpandLevelColumn.setCellValueFactory(cellData ->
                cellData.getValue().maxExpandLevel());
        totalRunsColumn.setCellValueFactory(cellData ->
                cellData.getValue().totalRuns());
        avgCreditCostColumn.setCellValueFactory(cellData ->
                cellData.getValue().avgCreditCost());

        programsTableView.setItems(programsList);
    }

    /**
     * Initialize component with necessary dependencies
     */
    public void initComponent(@NotNull BooleanProperty programLoadedProperty,
                              @NotNull BooleanProperty fileLoadedProperty,
                              @NotNull Consumer<String> executeProgramCallback) {
        this.programLoadedProperty = programLoadedProperty;
        this.executeProgramCallback = executeProgramCallback;

        // Bind execute button to both program loaded and selection state
        executeProgramButton.disableProperty().bind(
                programLoadedProperty.not()
                        .or(programsTableView.getSelectionModel().selectedItemProperty().isNull())
        );

        // Listen for file load events to refresh program data
        fileLoadedProperty.addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                refreshProgramData();
            }
        });
    }

    /**
     * Refresh program data from engine controller
     */
    public void refreshProgramData() {
        if (engineController == null) {
            System.err.println("EngineController not set - cannot refresh program data");
            return;
        }

        try {
            // Get basic program from engine
            ProgramDTO basicProgram = engineController.getBasicProgram();

            if (basicProgram != null) {
                // Get execution statistics for the program
                int totalRuns = engineController.getAllExecutionStatistics().size();

                // Calculate average credit cost from execution statistics
                double avgCost = calculateAverageCreditCost(totalRuns);

                // Create dashboard DTO
                ProgramDashboardDTO dashboardDTO = createProgramDashboardDTO(
                        basicProgram,
                        totalRuns,
                        avgCost
                );

                // Update table
                programsList.clear();
                programsList.add(dashboardDTO);

                System.out.println("Programs panel refreshed with: " + basicProgram.ProgramName());
            }
        } catch (Exception e) {
            System.err.println("Error refreshing program data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Create ProgramDashboardDTO from ProgramDTO
     */
    private @NotNull ProgramDashboardDTO createProgramDashboardDTO(
            @NotNull ProgramDTO program,
            int totalRuns,
            double avgCreditCost) {

        return new ProgramDashboardDTO(
                new SimpleStringProperty(program.ProgramName()),
                new SimpleStringProperty("System"), // Default uploader
                new SimpleIntegerProperty(program.instructions().size()),
                new SimpleIntegerProperty(engineController.getMaxExpandLevel()),
                new SimpleIntegerProperty(totalRuns),
                new SimpleDoubleProperty(avgCreditCost)
        );
    }

    /**
     * Calculate average credit cost from execution statistics
     */
    private double calculateAverageCreditCost(int totalRuns) {
        if (totalRuns == 0) {
            return 0.0;
        }

        // Simple cost calculation: 1 credit per 1 cycles
        // This can be adjusted based on actual business logic
        try {
            int totalCycles = engineController.getAllExecutionStatistics().stream()
                    .mapToInt(ExecutionStatisticsDTO::cyclesUsed)
                    .sum();
            return (double) totalCycles / 1.0 / totalRuns;
        } catch (Exception e) {
            return 0.0;
        }
    }

    @FXML
    private void handleExecuteProgram() {
        ProgramDashboardDTO selectedProgram = programsTableView.getSelectionModel().getSelectedItem();
        if (selectedProgram != null && executeProgramCallback != null) {
            String programName = selectedProgram.programName().get();
            System.out.println("Execute program requested: " + programName);
            executeProgramCallback.accept(programName);
        }
    }

    /**
     * Set engine controller for data access
     */
    public void setEngineController(@NotNull EngineController engineController) {
        this.engineController = engineController;
    }

    /**
     * Manually set programs (for external updates)
     */
    public void setPrograms(@NotNull ObservableList<ProgramDashboardDTO> programs) {
        programsList.setAll(programs);
    }

    /**
     * Clear all programs from the table
     */
    public void clearPrograms() {
        programsList.clear();
    }
}