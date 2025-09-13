package ui.jfx.statistics;

import dto.engine.ExecutionStatisticsDTO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

import java.util.Map;

public class HistoryStatsController {

    @FXML
    private TableColumn<ExecutionRow, String> executionNumberColumn;
    @FXML
    private TableColumn<ExecutionRow, String> expandLevelColumn;
    @FXML
    private TableColumn<ExecutionRow, String> resultColumn;
    @FXML
    private TableColumn<ExecutionRow, String> cyclesUsedColumn;
    @FXML
    private TableColumn<ExecutionRow, String> argumentColumn;
    @FXML
    private TableView<ExecutionRow> statisticsTable;
    @FXML
    private VBox statsTableContainer;

    private final ObservableList<ExecutionRow> statisticsData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Initialize table columns with proper bindings
        executionNumberColumn.setCellValueFactory(cellData -> cellData.getValue().executionNumberProperty());
        expandLevelColumn.setCellValueFactory(cellData -> cellData.getValue().expandLevelProperty());
        resultColumn.setCellValueFactory(cellData -> cellData.getValue().resultProperty());
        cyclesUsedColumn.setCellValueFactory(cellData -> cellData.getValue().cyclesUsedProperty());
        argumentColumn.setCellValueFactory(cellData -> cellData.getValue().argumentProperty());

        statisticsTable.setItems(statisticsData);
    }

    /**
     * Updates the statistics display with the latest execution data
     * @param executionStats The execution statistics DTO containing the latest run data
     */
    public void updateStatistics(ExecutionStatisticsDTO executionStats) {
        // Only update statistics table since we removed execution history
        updateStatisticsTable(executionStats);
    }

    /**
     * Updates the statistics table with details from the latest execution
     */
    private void updateStatisticsTable(ExecutionStatisticsDTO executionStats) {
        // Format arguments string
        String argumentsStr;
        if (!executionStats.arguments().isEmpty()) {
            StringBuilder argsBuilder = new StringBuilder();
            for (Map.Entry<String, Integer> entry : executionStats.arguments().entrySet()) {
                if (argsBuilder.length() > 0) {
                    argsBuilder.append(", ");
                }
                argsBuilder.append(entry.getKey()).append("=").append(entry.getValue());
            }
            argumentsStr = argsBuilder.toString();
        } else {
            argumentsStr = "None";
        }

        // Add new ExecutionRow to the table (keeping all executions, not clearing)
        ExecutionRow row = new ExecutionRow(
                String.valueOf(executionStats.executionNumber()),
                String.valueOf(executionStats.expandLevel()),
                String.valueOf(executionStats.result()),
                String.valueOf(executionStats.cyclesUsed()),
                argumentsStr
        );

        statisticsData.add(row);
    }

    public void clearHistory() {
        statisticsData.clear();
    }

    public static class ExecutionRow {
        private final javafx.beans.property.SimpleStringProperty executionNumber;
        private final javafx.beans.property.SimpleStringProperty expandLevel;
        private final javafx.beans.property.SimpleStringProperty result;
        private final javafx.beans.property.SimpleStringProperty cyclesUsed;
        private final javafx.beans.property.SimpleStringProperty argument;

        public ExecutionRow(String executionNumber, String expandLevel, String result, String cyclesUsed, String argument) {
            this.executionNumber = new javafx.beans.property.SimpleStringProperty(executionNumber);
            this.expandLevel = new javafx.beans.property.SimpleStringProperty(expandLevel);
            this.result = new javafx.beans.property.SimpleStringProperty(result);
            this.cyclesUsed = new javafx.beans.property.SimpleStringProperty(cyclesUsed);
            this.argument = new javafx.beans.property.SimpleStringProperty(argument);
        }

        public String getExecutionNumber() {
            return executionNumber.get();
        }

        public javafx.beans.property.StringProperty executionNumberProperty() {
            return executionNumber;
        }

        public String getExpandLevel() {
            return expandLevel.get();
        }

        public javafx.beans.property.StringProperty expandLevelProperty() {
            return expandLevel;
        }

        public String getResult() {
            return result.get();
        }

        public javafx.beans.property.StringProperty resultProperty() {
            return result;
        }

        public String getCyclesUsed() {
            return cyclesUsed.get();
        }

        public javafx.beans.property.StringProperty cyclesUsedProperty() {
            return cyclesUsed;
        }

        public String getArgument() {
            return argument.get();
        }

        public javafx.beans.property.StringProperty argumentProperty() {
            return argument;
        }
    }
}
