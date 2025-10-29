package ui.dashboard.functions;

import dto.engine.FunctionMetadata;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

/**
 * Controller for the Functions Panel.
 * Displays available functions with their metadata in a table.
 */
public class FunctionsPanelController {

    private final ObservableList<FunctionMetadata> functionsList = FXCollections.observableArrayList();

    @FXML
    public Button clearSelection;

    @FXML
    private TableView<FunctionMetadata> functionsTableView;
    @FXML
    private TableColumn<FunctionMetadata, String> functionNameColumn;
    @FXML
    private TableColumn<FunctionMetadata, String> programNameColumn;
    @FXML
    private TableColumn<FunctionMetadata, String> uploadedByColumn;
    @FXML
    private TableColumn<FunctionMetadata, Number> instructionCountColumn;
    @FXML
    private TableColumn<FunctionMetadata, Number> maxExpandLevelColumn;
    @FXML
    private Button executeFunctionButton;

    private BiConsumer<String, String> executeFunctionCallback;

    @FXML
    public void initialize() {
        setupTableColumns();

        // Enable/disable execute button based on selection
        executeFunctionButton.disableProperty().bind(
                functionsTableView.getSelectionModel().selectedItemProperty().isNull()
        );

        System.out.println("FunctionsPanelController initialized");
    }

    private void setupTableColumns() {
        // Bind columns to FunctionMetadata properties
        functionNameColumn.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue().displayName()));
        programNameColumn.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue().ProgramContext()));
        uploadedByColumn.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue().uploadedBy()));
        instructionCountColumn.setCellValueFactory(cellData ->
                new ReadOnlyIntegerWrapper(cellData.getValue().numOfInstructions()));
        maxExpandLevelColumn.setCellValueFactory(cellData ->
                new ReadOnlyIntegerWrapper(cellData.getValue().maxExpandLevel()));

        functionsTableView.setItems(functionsList);
    }

    /**
     * Initialize component with necessary dependencies
     */
    public void initComponent(@NotNull ListProperty<FunctionMetadata> functionsList,
                              @NotNull BiConsumer<String, String> executeFunctionCallback) {
        this.executeFunctionCallback = executeFunctionCallback;
        functionsTableView.itemsProperty().bind(functionsList);

        // Bind execute button to both program loaded and selection state
        executeFunctionButton.disableProperty().bind(
                functionsTableView.getSelectionModel().selectedItemProperty().isNull());
        clearSelection.disableProperty().bind(
                functionsTableView.getSelectionModel().selectedItemProperty().isNull());
    }

    @FXML
    private void handleExecuteFunction() {
        FunctionMetadata selected = functionsTableView.getSelectionModel().getSelectedItem();
        if (selected != null && executeFunctionCallback != null) {
            String internalFunctionName = selected.name();
            String displayFunctionName = selected.displayName();
            System.out.println("Execute function requested: " + internalFunctionName);
            executeFunctionCallback.accept(internalFunctionName, displayFunctionName);
        }
    }

    public void clearSelection(ActionEvent actionEvent) {
        functionsTableView.getSelectionModel().clearSelection();
    }

}