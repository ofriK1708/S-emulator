package ui.jfx.execution;

import dto.ui.VariableDTO;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import ui.jfx.AppController;
import ui.utils.UIUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Displays work variables in a table format after program execution.
 */
public class ExecutionVariableController {

    @FXML private TableView<VariableDTO> workVariablesTable;
    @FXML private TableColumn<VariableDTO, String> variableColumn;
    @FXML private TableColumn<VariableDTO, Number> valueColumn;

    @FXML
    private void initialize() {
        // Initialize table columns
        variableColumn.setCellValueFactory(cellData -> cellData.getValue().name());
        valueColumn.setCellValueFactory(cellData -> cellData.getValue().value());

        System.out.println("ExecutionVariableController initialized");
    }

    public void showWorkVariables(int programResult, Map<String, Integer> arguments, Map<String, Integer> workVars) {
        List<VariableDTO> variableDTOS = new ArrayList<>();
        variableDTOS.add(new VariableDTO(new SimpleStringProperty("y"), new SimpleIntegerProperty(programResult)));

        arguments.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(UIUtils.programNameComparator))
                .map(UIUtils::toVariableDTO)
                .forEach(variableDTOS::add);

        workVars.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(UIUtils.programNameComparator))
                .map(UIUtils::toVariableDTO)
                .forEach(variableDTOS::add);

        System.out.println("Work variables displayed in table: " + workVars.size() + " variables");
        setVariables(variableDTOS);
    }
    private void setVariables(List<VariableDTO> variableDTOS) {
        workVariablesTable.getItems().addAll(variableDTOS);
    }

    public void clearVariables() {
        workVariablesTable.getItems().clear();
    }
}
