package ui.jfx.statistics;

import dto.engine.ExecutionStatisticsDTO;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;

import static ui.utils.UIUtils.showError;
import static ui.utils.UIUtils.showInfo;

/**
 * Controller for the History/Statistics table.
 * Displays execution history and provides Show Details functionality.
 * Re-run functionality is handled exclusively through the Show dialog.
 */
public class HistoryStatsController {

    @FXML
    private TableColumn<ExecutionStatisticsDTO, Number> executionNumberColumn;
    @FXML
    private TableColumn<ExecutionStatisticsDTO, Number> expandLevelColumn;
    @FXML
    private TableColumn<ExecutionStatisticsDTO, Number> resultColumn;
    @FXML
    private TableColumn<ExecutionStatisticsDTO, Number> cyclesUsedColumn;
    @FXML
    private TableColumn<ExecutionStatisticsDTO, String> argumentColumn;
    @FXML
    private TableView<ExecutionStatisticsDTO> statisticsTable;

    // Action button for viewing details
    @FXML
    private Button showButton;
    @FXML
    private Button rerunButton;

    // Callback for functionality - provided by AppController
    private @Nullable Function<ExecutionStatisticsDTO, Map<String, Integer>> variableStatesProvider;

    @FXML
    public void initialize() {
        // Initialize table columns with proper bindings
        executionNumberColumn.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().executionNumber()));
        expandLevelColumn.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().expandLevel()));
        resultColumn.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().result()));
        cyclesUsedColumn.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().cyclesUsed()));
        argumentColumn.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(formatArguments(cellData.getValue().arguments())));

        showButton.disableProperty().bind(
                statisticsTable.getSelectionModel().selectedItemProperty().isNull()
        );
        rerunButton.disableProperty().bind(
                statisticsTable.getSelectionModel().selectedItemProperty().isNull()
        );

    }

    /**
     * Initialize the component with statistics data and variable states provider.
     * Simplified initialization - Re-run functionality is handled by the Show dialog.
     *
     * @param statisticsData Observable list of execution statistics for the table
     * @param variableStatesProvider Function to retrieve final variable states for a given execution
     */
    public void initComponent(
            ListProperty<ExecutionStatisticsDTO> statisticsData,
            @NotNull Function<ExecutionStatisticsDTO, Map<String, Integer>> variableStatesProvider) {

        this.variableStatesProvider = variableStatesProvider;
        statisticsTable.itemsProperty().bind(statisticsData);

        System.out.println("HistoryStatsController initialized with show functionality");
    }

    private @NotNull String formatArguments(@NotNull Map<String, Integer> arguments) {
        if (arguments.isEmpty()) {
            return "None";
        }
        StringBuilder argsBuilder = new StringBuilder();
        for (Map.Entry<String, Integer> entry : arguments.entrySet()) {
            if (!argsBuilder.isEmpty()) {
                argsBuilder.append(", ");
            }
            argsBuilder.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return argsBuilder.isEmpty() ? "None" : argsBuilder.toString();
    }

    /**
     * Handle Show button click.
     * Opens the ShowRunView dialog with detailed execution information.
     * Re-run functionality is available within the Show dialog.
     */
    @FXML
    private void handleShow() {
        ExecutionStatisticsDTO selectedExecution = statisticsTable.getSelectionModel().getSelectedItem();
        if (selectedExecution == null) {
            return;
        }

        if (variableStatesProvider == null) {
            showInfo("Show functionality not fully initialized - missing variable states provider");
            return;
        }

        try {
            System.out.println("Show clicked for execution #" + selectedExecution.executionNumber());

            // Retrieve the final variable states for this execution
            Map<String, Integer> finalVariableStates = variableStatesProvider.apply(selectedExecution);

            // Open the Show dialog with all required data
            // Re-run functionality is handled within the Show dialog
            ui.utils.UIUtils.openShowRunDialog(
                    selectedExecution,
                    finalVariableStates
            );

        } catch (Exception e) {
            System.err.println("Error opening Show dialog: " + e.getMessage());
            showInfo("Error displaying execution details: " + e.getMessage());
        }
    }
    /**
     * Handle Rerun button click.
     * Triggers rerun preparation with the selected execution's parameters.
     * This opens the Set Run dialog with pre-populated arguments.
     */
    @FXML
    private void handleRerun() {
        ExecutionStatisticsDTO selectedExecution = statisticsTable.getSelectionModel().getSelectedItem();
        if (selectedExecution == null) {
            return;
        }

        try {
            System.out.println("Rerun triggered from history table for execution #" +
                    selectedExecution.executionNumber());

            // Extract execution parameters
            int expandLevel = selectedExecution.expandLevel();
            Map<String, Integer> arguments = selectedExecution.arguments();

            // Use UIUtils to prepare the rerun (this will open the Set Run dialog)
            ui.utils.UIUtils.executeRerunFromShowDialog(expandLevel, arguments);

        } catch (Exception e) {
            System.err.println("Error during rerun from history table: " + e.getMessage());
            showError("Error during rerun: " + e.getMessage());
        }
    }
}