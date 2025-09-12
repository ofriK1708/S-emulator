package ui.jfx.statistics;

import dto.engine.ExecutionStatisticsDTO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

import java.util.Map;

public class HistoryStatsController {

    @FXML
    private VBox historyListContainer;
    @FXML
    private VBox historyLogContainer;
    @FXML
    private ScrollPane historyScrollPane;
    @FXML
    public TableColumn<StatRow, String> statNameColumn;
    @FXML
    public TableColumn<StatRow, String> statValueColumn;
    @FXML
    private TableView<StatRow> statisticsTable;
    @FXML
    private VBox statsTableContainer;

    private final ObservableList<StatRow> statisticsData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Initialize table columns
        statNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        statValueColumn.setCellValueFactory(cellData -> cellData.getValue().valueProperty());
        statisticsTable.setItems(statisticsData);

        // Clear placeholder text from history container
        historyListContainer.getChildren().clear();
    }

    /**
     * Updates the statistics display with the latest execution data
     * @param executionStats The execution statistics DTO containing the latest run data
     */
    public void updateStatistics(ExecutionStatisticsDTO executionStats) {
        // Update statistics table with latest execution details
        updateStatisticsTable(executionStats);

        // Add new entry to execution history
        addToExecutionHistory(executionStats);
    }

    /**
     * Updates the statistics table with details from the latest execution
     */
    private void updateStatisticsTable(ExecutionStatisticsDTO executionStats) {
        statisticsData.clear();

        // Add execution details to statistics table
        statisticsData.add(new StatRow("Execution Number", String.valueOf(executionStats.executionNumber())));
        statisticsData.add(new StatRow("Expand Level", String.valueOf(executionStats.expandLevel())));
        statisticsData.add(new StatRow("Result", String.valueOf(executionStats.result())));
        statisticsData.add(new StatRow("Cycles Used", String.valueOf(executionStats.cyclesUsed())));

        // Add arguments to statistics table
        if (!executionStats.arguments().isEmpty()) {
            StringBuilder argsBuilder = new StringBuilder();
            for (Map.Entry<String, Integer> entry : executionStats.arguments().entrySet()) {
                if (argsBuilder.length() > 0) {
                    argsBuilder.append(", ");
                }
                argsBuilder.append(entry.getKey()).append("=").append(entry.getValue());
            }
            statisticsData.add(new StatRow("Arguments", argsBuilder.toString()));
        } else {
            statisticsData.add(new StatRow("Arguments", "None"));
        }
    }

    /**
     * Adds a summary entry to the execution history log
     */
    private void addToExecutionHistory(ExecutionStatisticsDTO executionStats) {
        // Create summary text for this execution
        String summaryText = String.format("Execution #%d: result=%d, cycles=%d",
                executionStats.executionNumber(),
                executionStats.result(),
                executionStats.cyclesUsed());

        // Create a label for this execution entry
        Label historyEntry = new Label(summaryText);
        historyEntry.getStyleClass().add("history-entry");

        // Add to history container
        historyListContainer.getChildren().add(historyEntry);

        // Auto-scroll to bottom to show latest execution
        historyScrollPane.layout();
        historyScrollPane.setVvalue(1.0);
    }

    /**
     * Clears both the statistics table and execution history
     */
    public void clearHistory() {
        statisticsData.clear();
        historyListContainer.getChildren().clear();

        // Add placeholder text back
        Label placeholder = new Label("Execution history will appear here");
        placeholder.getStyleClass().add("placeholder-text");
        historyListContainer.getChildren().add(placeholder);
    }

    public static class StatRow {
        private final javafx.beans.property.SimpleStringProperty name;
        private final javafx.beans.property.SimpleStringProperty value;

        public StatRow(String name, String value) {
            this.name = new javafx.beans.property.SimpleStringProperty(name);
            this.value = new javafx.beans.property.SimpleStringProperty(value);
        }

        public String getName() {
            return name.get();
        }

        public javafx.beans.property.StringProperty nameProperty() {
            return name;
        }

        public String getValue() {
            return value.get();
        }

        public javafx.beans.property.StringProperty valueProperty() {
            return value;
        }
    }
}
