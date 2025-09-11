package ui.jfx.variables;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import ui.utils.UIUtils;

import java.util.Map;

public class VariablesController {

    @FXML private TableView<VariableEntry> variablesTable;
    @FXML private TableColumn<VariableEntry, String> variableColumn;
    @FXML private TableColumn<VariableEntry, Number> valueColumn;

    private ObservableList<VariableEntry> variablesData = FXCollections.observableArrayList();
    private Map<String, Integer> currentVariables;

    public void onRunPressed() {
        refreshTable();
        System.out.println("Variables table updated after run pressed");
    }

    /**
     * Data model for TableView entries representing input variables
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
    public void initialize() {
        // Initialize table columns
        variableColumn.setCellValueFactory(cellData -> cellData.getValue().variableNameProperty());
        valueColumn.setCellValueFactory(cellData -> cellData.getValue().valueProperty());

        // Bind the data to the table
        variablesTable.setItems(variablesData);

        // Set column widths to be responsive
        variableColumn.prefWidthProperty().bind(variablesTable.widthProperty().multiply(0.6));
        valueColumn.prefWidthProperty().bind(variablesTable.widthProperty().multiply(0.4));

        clearVariables();
        System.out.println("VariablesController initialized with TableView");
    }

    /**
     * Set and display variables in the table
     */
    public void setVariables(Map<String, Integer> variables) {
        this.currentVariables = variables;
        refreshTable();
    }

    /**
     * Refresh the table with current variables - called by button bindings
     */
    /**
     * Refresh the table with current variables - called by button bindings
     */
    public void refreshTable() {
        variablesData.clear();

        // Add this null check to prevent the error
        if (currentVariables == null || currentVariables.isEmpty()) {
            System.out.println("No variables to display or currentVariables is null");
            return;
        }

        // Use ConsoleUI sorting logic and populate table
        currentVariables.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(UIUtils.programNameComparator))
                .forEach(entry -> {
                    VariableEntry variableEntry = new VariableEntry(entry.getKey(), entry.getValue());
                    variablesData.add(variableEntry);
                });

        System.out.println("Variables table refreshed with " + currentVariables.size() + " variables");
    }


    /**
     * Clear variables and reset table
     */
    public void clearVariables() {
        variablesData.clear();
        currentVariables = null;
    }

    /**
     * Show success message and refresh table
     */
    public void showSuccess(String message) {
        refreshTable();
    }

    /**
     * Show error message
     */
    public void showError(String message) {
      }

    /**
     * Update specific variable value and refresh table
     */
    public void updateVariable(String variableName, Integer newValue) {
        if (currentVariables != null) {
            currentVariables.put(variableName, newValue);
            refreshTable();
        }
    }

    /**
     * Get current variables map
     */
    public Map<String, Integer> getCurrentVariables() {
        return currentVariables;
    }

    /* ---------- Button Binding Methods ---------- */

    /**
     * Called when setRun button is pressed
     */
    public void onSetRunPressed() {
        refreshTable();
        System.out.println("Variables table updated after setRun pressed");
    }

    /**
     * Called when resume button is pressed
     */
    public void onResumePressed() {
        refreshTable();
        System.out.println("Variables table updated after resume pressed");
    }

    /**
     * Called when startDebugExecution button is pressed
     */
    public void onStartDebugExecutionPressed() {
        refreshTable();
        System.out.println("Variables table updated after startDebugExecution pressed");
    }

    /**
     * Called when startRegularExecution button is pressed
     */
    public void onStartRegularExecutionPressed() {
        refreshTable();
        System.out.println("Variables table updated after startRegularExecution pressed");
    }

    /**
     * Called when stepBackward button is pressed
     */
    public void onStepBackwardPressed() {
        refreshTable();
        System.out.println("Variables table updated after stepBackward pressed");
    }

    /**
     * Called when stepOver button is pressed
     */
    public void onStepOverPressed() {
        refreshTable();
        System.out.println("Variables table updated after stepOver pressed");
    }

    /**
     * Called when stop button is pressed
     */
    public void onStopPressed() {
        refreshTable();
        System.out.println("Variables table updated after stop pressed");
    }
}
