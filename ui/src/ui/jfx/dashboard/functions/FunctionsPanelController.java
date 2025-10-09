package ui.jfx.dashboard.functions;

import dto.ui.FunctionDTO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Controller for the Functions Panel.
 * Displays available functions with their metadata in a table.
 */
public class FunctionsPanelController {

    @FXML private TableView<FunctionDTO> functionsTableView;
    @FXML private TableColumn<FunctionDTO, String> functionNameColumn;
    @FXML private TableColumn<FunctionDTO, String> programNameColumn;
    @FXML private TableColumn<FunctionDTO, String> uploadedByColumn;
    @FXML private TableColumn<FunctionDTO, Number> instructionCountColumn;
    @FXML private TableColumn<FunctionDTO, Number> maxExpandLevelColumn;
    @FXML private Button executeFunctionButton;

    private final ObservableList<FunctionDTO> functionsList = FXCollections.observableArrayList();
    private Consumer<String> executeFunctionCallback;

    @FXML
    public void initialize() {
        setupTableColumns();
        loadMockData();

        // Enable/disable execute button based on selection
        executeFunctionButton.disableProperty().bind(
                functionsTableView.getSelectionModel().selectedItemProperty().isNull()
        );

        System.out.println("FunctionsPanelController initialized");
    }

    private void setupTableColumns() {
        functionNameColumn.setCellValueFactory(new PropertyValueFactory<>("functionName"));
        programNameColumn.setCellValueFactory(new PropertyValueFactory<>("programName"));
        uploadedByColumn.setCellValueFactory(new PropertyValueFactory<>("uploadedBy"));
        instructionCountColumn.setCellValueFactory(new PropertyValueFactory<>("instructionCount"));
        maxExpandLevelColumn.setCellValueFactory(new PropertyValueFactory<>("maxExpandLevel"));

        functionsTableView.setItems(functionsList);
    }

    private void loadMockData() {
        // Mock data for demonstration
        functionsList.addAll(
                new FunctionDTO("Add", "MathProgram", "Alice", 15, 2),
                new FunctionDTO("Multiply", "MathProgram", "Bob", 22, 3),
                new FunctionDTO("Factorial", "RecursionDemo", "Alice", 18, 4),
                new FunctionDTO("Fibonacci", "RecursionDemo", "Charlie", 25, 5),
                new FunctionDTO("Power", "MathProgram", "Bob", 20, 3)
        );
    }

    @FXML
    private void handleExecuteFunction() {
        FunctionDTO selected = functionsTableView.getSelectionModel().getSelectedItem();
        if (selected != null && executeFunctionCallback != null) {
            executeFunctionCallback.accept(selected.getFunctionName());
            System.out.println("Execute function requested: " + selected.getFunctionName());
        }
    }

    public void initComponent(Consumer<String> executeFunctionCallback) {
        this.executeFunctionCallback = executeFunctionCallback;
    }

    public void setFunctions(@NotNull ObservableList<FunctionDTO> functions) {
        functionsList.setAll(functions);
    }

    public void clearFunctions() {
        functionsList.clear();
    }
}