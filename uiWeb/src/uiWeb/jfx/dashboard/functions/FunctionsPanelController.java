package uiweb.jfx.dashboard.functions;

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

    private final ObservableList<FunctionDTO> functionsList = FXCollections.observableArrayList();
    @FXML
    private TableView<FunctionDTO> functionsTableView;
    @FXML
    private TableColumn<FunctionDTO, String> functionNameColumn;
    @FXML
    private TableColumn<FunctionDTO, String> programNameColumn;
    @FXML
    private TableColumn<FunctionDTO, String> uploadedByColumn;
    @FXML
    private TableColumn<FunctionDTO, Number> instructionCountColumn;
    @FXML
    private TableColumn<FunctionDTO, Number> maxExpandLevelColumn;
    @FXML
    private Button executeFunctionButton;
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
    }

    @FXML
    private void handleExecuteFunction() {
        FunctionDTO selected = functionsTableView.getSelectionModel().getSelectedItem();
        if (selected != null && executeFunctionCallback != null) {
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