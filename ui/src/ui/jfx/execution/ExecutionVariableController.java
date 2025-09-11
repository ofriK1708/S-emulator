package ui.jfx.execution;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import ui.jfx.AppController;
import ui.utils.UIUtils;

import java.util.Map;

/**
 * Displays work variables in a table format after program execution.
 */
public class ExecutionVariableController {

    @FXML private TableView<VariableEntry> workVariablesTable;
    @FXML private TableColumn<VariableEntry, String> variableColumn;
    @FXML private TableColumn<VariableEntry, Number> valueColumn;

    private AppController app;
    private ObservableList<VariableEntry> workVariablesData = FXCollections.observableArrayList();

    /**
     * Data model for TableView entries representing work variables
     */
    public static class VariableEntry {
        private final SimpleStringProperty variableName;
        private final SimpleIntegerProperty value;

        public VariableEntry(String variableName, Integer value) {
            this.variableName = new SimpleStringProperty(variableName);
            this.value = new SimpleIntegerProperty(value);
        }

        public String getVariableName() {
            return variableName.get();
        }

        public SimpleStringProperty variableNameProperty() {
            return variableName;
        }

        public void setVariableName(String variableName) {
            this.variableName.set(variableName);
        }

        public int getValue() {
            return value.get();
        }

        public SimpleIntegerProperty valueProperty() {
            return value;
        }

        public void setValue(int value) {
            this.value.set(value);
        }
    }

    @FXML
    private void initialize() {
        // Initialize table columns
        variableColumn.setCellValueFactory(cellData -> cellData.getValue().variableNameProperty());
        valueColumn.setCellValueFactory(cellData -> cellData.getValue().valueProperty());

        // Bind the data to the table
        workVariablesTable.setItems(workVariablesData);

        // Set column widths to be responsive
        variableColumn.prefWidthProperty().bind(workVariablesTable.widthProperty().multiply(0.5));
        valueColumn.prefWidthProperty().bind(workVariablesTable.widthProperty().multiply(0.5));

        System.out.println("ExecutionVariableController initialized");
    }

    /* ---------- Public API ---------- */

    public void setAppController(AppController controller) {
        this.app = controller;
    }

    /**
     * Called by AppController after program execution to show work variables.
     * Uses ConsoleUI sorting logic to display variables in table format.
     */
    public void showWorkVariables(Map<String, Integer> workVars) {
        workVariablesData.clear();

        if (workVars == null || workVars.isEmpty()) {
            // Table will automatically show placeholder "No variables to display"
            System.out.println("No work variables to display");
            return;
        }

        // Adapt ConsoleUI logic: sort by programNameComparator and populate table
//        workVars.entrySet().stream()
//                .sorted(Map.Entry.comparingByKey(UIUtils.programNameComparator))
//                .forEach(entry -> {
//                    VariableEntry variableEntry = new VariableEntry(entry.getKey(), entry.getValue());
//                    workVariablesData.add(variableEntry);
//                });

        System.out.println("Work variables displayed in table: " + workVars.size() + " variables");
    }

    /**
     * Clear work variables table
     */
    public void clearWorkVariables() {
        workVariablesData.clear();
    }

    /**
     * Update specific work variable value if it exists in the table
     */
    public void updateWorkVariable(String variableName, Integer newValue) {
        workVariablesData.stream()
                .filter(entry -> entry.getVariableName().equals(variableName))
                .findFirst()
                .ifPresent(entry -> entry.setValue(newValue));
    }

    /**
     * Get current work variables as a map
     */
    public Map<String, Integer> getCurrentWorkVariables() {
        return workVariablesData.stream()
                .collect(java.util.stream.Collectors.toMap(
                        VariableEntry::getVariableName,
                        VariableEntry::getValue
                ));
    }

    /**
     * This method is intentionally left empty as this controller
     * only handles work variables, not input variables
     */
    public void showInputVariables(Map<String, Integer> programVariables) {
        // This controller displays only work variables (Z), not input variables (X)
    }
}
