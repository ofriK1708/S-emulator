package ui.jfx.variables;

import dto.ui.VariableDTO;
import javafx.beans.property.ListProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Displays input variables in a table format before program execution.
 */
public class VariablesTableController {
    @FXML
    private TableView<VariableDTO> variablesTable;
    @FXML
    private TableColumn<VariableDTO, String> variableColumn;
    @FXML
    private TableColumn<VariableDTO, Number> valueColumn;

    @FXML
    private void initialize() {
        // Initialize table columns
        variableColumn.setCellValueFactory(cellData -> cellData.getValue().name());
        valueColumn.setCellValueFactory(cellData -> cellData.getValue().value());
        System.out.println("VariablesController initialized with TableView");
    }

    public void initAllVarsTable(ListProperty<VariableDTO> variables) {
        variablesTable.itemsProperty().bind(variables);

    }

    public void initArgsTable(ListProperty<VariableDTO> args) {
        variablesTable.itemsProperty().bind(args);
        variableColumn.setText("Arguments");
    }
}
