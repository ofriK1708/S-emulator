package ui.jfx.variables;

import dto.ui.VariableDTO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import ui.utils.UIUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Displays input variables in a table format before program execution.
 */
public class VariablesController {

    @FXML private TableView<VariableDTO> variablesTable;
    @FXML private TableColumn<VariableDTO, String> variableColumn;
    @FXML private TableColumn<VariableDTO, Number> valueColumn;

    private Map<String, Integer> currentVariables;

    @FXML
    private void initialize() {
        // Initialize table columns
        variableColumn.setCellValueFactory(cellData -> cellData.getValue().name());
        valueColumn.setCellValueFactory(cellData -> cellData.getValue().value());

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
     * Clear variables and reset table
     */
    public void clearVariables() {
        variablesTable.getItems().clear();
        currentVariables = null;
    }

    /**
     * Refresh the table with current variables
     */
    private void refreshTable() {
        variablesTable.getItems().clear();

        // Add null check to prevent errors
        if (currentVariables == null || currentVariables.isEmpty()) {
            System.out.println("No variables to display or currentVariables is null");
            return;
        }

        // Create list of VariableDTO objects sorted by name
        List<VariableDTO> variableDTOS = new ArrayList<>();
        currentVariables.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(UIUtils.programNameComparator))
                .map(UIUtils::toVariableDTO)
                .forEach(variableDTOS::add);

        // Add all variables to table
        variablesTable.getItems().addAll(variableDTOS);
        System.out.println("Variables table refreshed with " + currentVariables.size() + " variables");
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
        // Error handling implementation if needed
    }

    /* ---------- Button Binding Methods ---------- */

    /**
     * Called when run button is pressed
     */
    public void onRunPressed() {
        refreshTable();
        System.out.println("Variables table updated after run pressed");
    }

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
