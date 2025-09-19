package ui.jfx.statistics;

import dto.engine.ExecutionStatisticsDTO;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

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
    }

    public void initComponent(ListProperty<ExecutionStatisticsDTO> statisticsData) {
        statisticsTable.itemsProperty().bind(statisticsData);
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


}
