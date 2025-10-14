package ui.web.jfx.dashboard.history;

import dto.ui.HistoryRecordDTO;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.jetbrains.annotations.NotNull;

/**
 * Controller for the History Panel.
 * Displays execution history records for the selected user.
 */
public class HistoryPanelController {

    private final ObservableList<HistoryRecordDTO> historyList = FXCollections.observableArrayList();
    @FXML
    private TableView<HistoryRecordDTO> historyTableView;
    @FXML
    private TableColumn<HistoryRecordDTO, Number> runIdColumn;
    @FXML
    private TableColumn<HistoryRecordDTO, String> executionTypeColumn;
    @FXML
    private TableColumn<HistoryRecordDTO, String> programFunctionNameColumn;
    @FXML
    private TableColumn<HistoryRecordDTO, String> architectureTypeColumn;
    @FXML
    private TableColumn<HistoryRecordDTO, Number> executionLevelColumn;
    @FXML
    private TableColumn<HistoryRecordDTO, Number> finalYValueColumn;
    @FXML
    private TableColumn<HistoryRecordDTO, Number> cycleCountColumn;
    private StringProperty selectedUser;

    @FXML
    public void initialize() {
        setupTableColumns();
        loadMockData();

        System.out.println("HistoryPanelController initialized");
    }

    private void setupTableColumns() {
        runIdColumn.setCellValueFactory(new PropertyValueFactory<>("runId"));
        executionTypeColumn.setCellValueFactory(new PropertyValueFactory<>("executionType"));
        programFunctionNameColumn.setCellValueFactory(new PropertyValueFactory<>("programFunctionName"));
        architectureTypeColumn.setCellValueFactory(new PropertyValueFactory<>("architectureType"));
        executionLevelColumn.setCellValueFactory(new PropertyValueFactory<>("executionLevel"));
        finalYValueColumn.setCellValueFactory(new PropertyValueFactory<>("finalYValue"));
        cycleCountColumn.setCellValueFactory(new PropertyValueFactory<>("cycleCount"));

        historyTableView.setItems(historyList);
    }

    private void loadMockData() {
    }

    public void initComponent(StringProperty selectedUser) {
        this.selectedUser = selectedUser;

        // Listen for user changes to reload history
        if (selectedUser != null) {
            selectedUser.addListener((obs, oldVal, newVal) -> {
                if (newVal != null && !newVal.isEmpty()) {
                    loadHistoryForUser(newVal);
                }
            });
        }
    }

    private void loadHistoryForUser(@NotNull String username) {
        // This would normally fetch data from a service/repository
        // For now, just refresh with mock data
        System.out.println("Loading history for user: " + username);
        refreshHistory();
    }

    public void refreshHistory() {
        // Reload history data
        historyList.clear();
        loadMockData();
        System.out.println("History refreshed");
    }

    public void clearHistory() {
        historyList.clear();
        System.out.println("History cleared");
    }

    public void addHistoryRecord(@NotNull HistoryRecordDTO record) {
        historyList.add(record);
    }

    public void setHistoryRecords(@NotNull ObservableList<HistoryRecordDTO> records) {
        historyList.setAll(records);
    }
}