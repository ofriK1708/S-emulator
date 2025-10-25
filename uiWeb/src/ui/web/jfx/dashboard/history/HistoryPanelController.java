package ui.web.jfx.dashboard.history;

import dto.engine.ExecutionResultStatisticsDTO;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Controller for the History Panel.
 * Displays execution history records for the selected user.
 */
public class HistoryPanelController {

    @FXML
    private TableView<ExecutionResultStatisticsDTO> historyTableView;
    @FXML
    private TableColumn<ExecutionResultStatisticsDTO, Number> runNumberColumn;
    @FXML
    private TableColumn<ExecutionResultStatisticsDTO, String> programTypeColumn;
    @FXML
    private TableColumn<ExecutionResultStatisticsDTO, String> programDisplayNameColumn;
    @FXML
    private TableColumn<ExecutionResultStatisticsDTO, String> architectureTypeColumn;
    @FXML
    private TableColumn<ExecutionResultStatisticsDTO, Number> executionLevelColumn;
    @FXML
    private TableColumn<ExecutionResultStatisticsDTO, Number> outputColumn;
    @FXML
    private TableColumn<ExecutionResultStatisticsDTO, Number> cycleCountColumn;

    private StringProperty selectedUser;

    @FXML
    public void initialize() {
        setupTableColumns();

        System.out.println("HistoryPanelController initialized");
    }

    private void setupTableColumns() {
        runNumberColumn.setCellValueFactory(callData ->
                new SimpleIntegerProperty(callData.getValue().runNumber()));

        programTypeColumn.setCellValueFactory(cellData -> {
            String programType = cellData.getValue().isMainProgram() ? "Main Program" : "Sub-function";
            return new SimpleStringProperty(programType);
        });

        programDisplayNameColumn.setCellValueFactory(callData ->
                new SimpleStringProperty(callData.getValue().displayName()));

        architectureTypeColumn.setCellValueFactory(callData ->
                new SimpleStringProperty(callData.getValue().architectureType().name()));

        executionLevelColumn.setCellValueFactory(callData ->
                new SimpleIntegerProperty(callData.getValue().expandLevel()));

        outputColumn.setCellValueFactory(callData ->
                new SimpleIntegerProperty(callData.getValue().output()));

        cycleCountColumn.setCellValueFactory(callData ->
                new SimpleIntegerProperty(callData.getValue().cycleCount()));

    }

    public void initComponent(ListProperty<ExecutionResultStatisticsDTO> userStats) {
        historyTableView.itemsProperty().bind(userStats);
    }

}