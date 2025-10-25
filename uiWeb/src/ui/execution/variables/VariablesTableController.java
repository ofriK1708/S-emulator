package ui.execution.variables;

import dto.ui.VariableDTO;
import javafx.animation.FadeTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import org.jetbrains.annotations.NotNull;
import ui.utils.AnimatedTableRow;
import ui.utils.UIUtils;

/**
 * Displays workVariables in a table format with change highlighting for debug mode.
 */
public class VariablesTableController {
    private final BooleanProperty isAnimationsEnabled = new SimpleBooleanProperty(true);
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

        System.out.println("VariablesController initialized with TableView and change highlighting");
    }

    /**
     * Sets up the row factory to highlight changed workVariables in debug mode
     */

    public void initAllVarsTable(@NotNull ListProperty<VariableDTO> variables,
                                 @NotNull BooleanProperty animationsEnabled) {
        variablesTable.itemsProperty().bind(variables);
        variablesTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(VariableDTO item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("changed-variable", "unchanged-variable");
                if (item != null && !empty) {
                    if (item.hasChanged().get()) {
                        getStyleClass().add("changed-variable");
                        variablesTable.scrollTo(getIndex());
                        handleVariablesAnimation(this, true);
                    } else {
                        getStyleClass().remove("unchanged-variable");
                        handleVariablesAnimation(this, false);
                    }
                }
            }
        });


        // Add change listener to refresh highlighting when list changes
        variables.addListener((observable,
                               oldList, newList) ->
                scrollToFirstChangedVariable());
        isAnimationsEnabled.bind(animationsEnabled);


    }

    public void initArgsTable(@NotNull ListProperty<VariableDTO> args,
                              @NotNull BooleanProperty animationsEnabled) {
        variablesTable.itemsProperty().bind(args);
        variableColumn.setText("Arguments");
        variablesTable.setRowFactory(tv ->
                new AnimatedTableRow<>(animationsEnabled, 500, false));
    }

    private void handleVariablesAnimation(@NotNull TableRow<VariableDTO> row, boolean hasChanged) {
        FadeTransition ft = (FadeTransition) row.getProperties().get("highlightFade");
        if (ft != null) {
            ft.stop();
            row.setOpacity(1.0);
            row.getProperties().remove("highlightFade");
        }

        if (!hasChanged) {
            return;
        }
        UIUtils.checkIfShouldAnimate(row, isAnimationsEnabled.get());
    }

    private void scrollToFirstChangedVariable() {
        for (int i = 0; i < variablesTable.getItems().size(); i++) {
            if (variablesTable.getItems().get(i).hasChanged().get()) {
                int scrollToIndex = Math.max(0, i - 2); // -2 for header padding
                variablesTable.scrollTo(scrollToIndex);
                //variablesTable.getSelectionModel().select(i);
                break;
            }
        }
    }

    // for the animated rows with highlighting
    private static class HighlightingAnimatedTableRow extends AnimatedTableRow<VariableDTO> {
        private final TableView<VariableDTO> tableView;
        private final VariablesTableController controller;

        public HighlightingAnimatedTableRow(BooleanProperty animationsEnabled, int delay, boolean onlyAnimateOnce,
                                            TableView<VariableDTO> tableView, VariablesTableController controller) {
            super(animationsEnabled, delay, onlyAnimateOnce);
            this.tableView = tableView;
            this.controller = controller;
        }
    }
}