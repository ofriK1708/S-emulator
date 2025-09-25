package ui.jfx.variables;

import dto.ui.VariableDTO;
import javafx.beans.property.ListProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import org.jetbrains.annotations.NotNull;

/**
 * Displays variables in a table format with change highlighting for debug mode.
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

        // Set up row factory for change highlighting
        setupChangeHighlighting();

        System.out.println("VariablesController initialized with TableView and change highlighting");
    }

    /**
     * Sets up the row factory to highlight changed variables in debug mode
     */
    private void setupChangeHighlighting() {
        variablesTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(VariableDTO item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("changed-variable", "unchanged-variable");
                if (item != null && !empty) {
                    if (item.hasChanged().get())
                        getStyleClass().add("changed-variable");
                    else getStyleClass().add("unchanged-variable");

                    item.hasChanged().addListener((obs, oldV, newV) -> {
                        getStyleClass().removeAll("changed-variable", "unchanged-variable");
                        getStyleClass().add(newV ? "changed-variable" : "unchanged-variable");
                    });
                }
            }
        });
    }

    public void initAllVarsTable(@NotNull ListProperty<VariableDTO> variables) {
        variablesTable.itemsProperty().bind(variables);

        // Add change listener to refresh highlighting when list changes
        variables.addListener((observable, oldList, newList) -> {
            // Force table refresh to apply new highlighting
            variablesTable.refresh();
        });
    }

    public void initArgsTable(@NotNull ListProperty<VariableDTO> args) {
        variablesTable.itemsProperty().bind(args);
        variableColumn.setText("Arguments");

        // Add change listener to refresh highlighting when list changes
        args.addListener((observable, oldList, newList) -> {
            // Force table refresh to apply new highlighting
            variablesTable.refresh();
        });
    }
}