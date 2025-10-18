package ui.jfx.statistics;

import dto.ui.VariableDTO;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Controller for the ShowRunView.fxml - displays detailed information about a selected execution.
 * This is a display-only dialog. Rerun functionality is handled by HistoryStatsController.
 */
public class ShowRunController {

    // FXML injected components
    @FXML private Label titleLabel;
    @FXML private Label executionInfoLabel;
    @FXML private Label expandLevelLabel;
    @FXML private Label cyclesLabel;
    @FXML private Label resultLabel;
    @FXML private Label argumentsLabel;
    @FXML private TableView<VariableDTO> variablesTable;
    @FXML private TableColumn<VariableDTO, String> variableNameColumn;
    @FXML private TableColumn<VariableDTO, Number> variableValueColumn;
    @FXML private Button closeButton;

    // Data storage
    private @Nullable ExecutionStatisticsDTO currentExecution;
    private @Nullable Map<String, Integer> finalVariableStates;

    /**
     * Initialize the FXML components and set up table columns.
     * This method is automatically called by JavaFX after FXML loading.
     */
    @FXML
    public void initialize() {
        // Configure table columns to display variable data
        variableNameColumn.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().name().get()));

        variableValueColumn.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().value().get()));

        System.out.println("ShowRunController initialized successfully");
    }

    /**
     * Initialize the controller with execution data.
     * This method is called when opening the dialog.
     *
     * @param executionStats The selected execution statistics from the history table
     * @param variableStates Map of variable names to their final values from this execution
     */
    public void initializeWithData(
            @NotNull ExecutionStatisticsDTO executionStats,
            @NotNull Map<String, Integer> variableStates) {

        this.currentExecution = executionStats;
        this.finalVariableStates = variableStates;

        // Populate the header information labels
        updateHeaderLabels(executionStats);

        // Populate the arguments section
        updateArgumentsDisplay(executionStats.arguments());

        // Populate the variables table with final states
        updateVariablesTable(variableStates);

        System.out.println("ShowRunController initialized with execution #" +
                executionStats.executionNumber());
    }

    /**
     * Update the header labels with execution information.
     *
     * @param executionStats The execution statistics to display
     */
    private void updateHeaderLabels(@NotNull ExecutionStatisticsDTO executionStats) {
        executionInfoLabel.setText("Execution #" + executionStats.executionNumber());
        expandLevelLabel.setText("Expand Level: " + executionStats.expandLevel());
        cyclesLabel.setText("Cycles: " + executionStats.cyclesUsed());
        resultLabel.setText("Result: " + executionStats.output());
    }

    /**
     * Update the arguments display section.
     *
     * @param arguments Map of argument names to values used in this execution
     */
    private void updateArgumentsDisplay(@NotNull Map<String, Integer> arguments) {
        if (arguments.isEmpty()) {
            argumentsLabel.setText("No arguments required for this execution");
        } else {
            StringBuilder argsText = new StringBuilder();
            boolean first = true;
            for (Map.Entry<String, Integer> entry : arguments.entrySet()) {
                if (!first) {
                    argsText.append(", ");
                }
                argsText.append(entry.getKey()).append(" = ").append(entry.getValue());
                first = false;
            }
            argumentsLabel.setText(argsText.toString());
        }
    }

    /**
     * Populate the variables table with final variable states.
     * Converts the Map<String, Integer> to VariableDTO objects for table display.
     *
     * @param variableStates Map of variable names to their final values
     */
    private void updateVariablesTable(@NotNull Map<String, Integer> variableStates) {
        ObservableList<VariableDTO> variablesList = FXCollections.observableArrayList();

        // Convert map entries to VariableDTO objects for table display
        for (Map.Entry<String, Integer> entry : variableStates.entrySet()) {
            VariableDTO variableDTO = new VariableDTO(
                    new SimpleStringProperty(entry.getKey()),
                    new SimpleIntegerProperty(entry.getValue()),
                    // No change detection needed for this view - all variables shown as unchanged
                    new javafx.beans.property.SimpleBooleanProperty(false)
            );
            variablesList.add(variableDTO);
        }

        // Sort variables by name for consistent display
        variablesList.sort((v1, v2) -> v1.name().get().compareToIgnoreCase(v2.name().get()));

        variablesTable.setItems(variablesList);

        System.out.println("Variables table populated with " + variablesList.size() + " variables");
    }

    /**
     * Handle the Close button click.
     * Closes the current dialog window.
     */
    @FXML
    private void handleClose() {
        try {
            // Get the current stage (window) and close it
            Stage currentStage = (Stage) closeButton.getScene().getWindow();
            currentStage.close();

            System.out.println("Show dialog closed");

        } catch (Exception e) {
            System.err.println("Error closing Show dialog: " + e.getMessage());
        }
    }
}