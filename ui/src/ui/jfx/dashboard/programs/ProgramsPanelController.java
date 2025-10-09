package ui.jfx.dashboard.programs;

import dto.ui.ProgramDashboardDTO;
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
 * Controller for the Programs Panel.
 * Displays available programs with their metadata and execution statistics.
 */
public class ProgramsPanelController {

    @FXML private TableView<ProgramDashboardDTO> programsTableView;
    @FXML private TableColumn<ProgramDashboardDTO, String> programNameColumn;
    @FXML private TableColumn<ProgramDashboardDTO, String> uploadedByColumn;
    @FXML private TableColumn<ProgramDashboardDTO, Number> instructionCountColumn;
    @FXML private TableColumn<ProgramDashboardDTO, Number> maxExpandLevelColumn;
    @FXML private TableColumn<ProgramDashboardDTO, Number> totalRunsColumn;
    @FXML private TableColumn<ProgramDashboardDTO, Number> avgCreditCostColumn;
    @FXML private Button executeProgramButton;

    private final ObservableList<ProgramDashboardDTO> programsList = FXCollections.observableArrayList();
    private Consumer<String> executeProgramCallback;

    @FXML
    public void initialize() {
        setupTableColumns();
        loadMockData();

        // Enable/disable execute button based on selection
        executeProgramButton.disableProperty().bind(
                programsTableView.getSelectionModel().selectedItemProperty().isNull()
        );

        System.out.println("ProgramsPanelController initialized");
    }

    private void setupTableColumns() {
        programNameColumn.setCellValueFactory(new PropertyValueFactory<>("programName"));
        uploadedByColumn.setCellValueFactory(new PropertyValueFactory<>("uploadedBy"));
        instructionCountColumn.setCellValueFactory(new PropertyValueFactory<>("instructionCount"));
        maxExpandLevelColumn.setCellValueFactory(new PropertyValueFactory<>("maxExpandLevel"));
        totalRunsColumn.setCellValueFactory(new PropertyValueFactory<>("totalRuns"));
        avgCreditCostColumn.setCellValueFactory(new PropertyValueFactory<>("avgCreditCost"));

        programsTableView.setItems(programsList);
    }

    private void loadMockData() {
        // Mock data for demonstration
        programsList.addAll(
                new ProgramDashboardDTO("MathProgram", "Alice", 45, 3, 127, 2.5),
                new ProgramDashboardDTO("RecursionDemo", "Bob", 62, 5, 89, 3.8),
                new ProgramDashboardDTO("SimpleCalculator", "Charlie", 38, 2, 215, 1.9),
                new ProgramDashboardDTO("DataProcessor", "Alice", 78, 4, 54, 4.2),
                new ProgramDashboardDTO("SortingAlgorithm", "Bob", 95, 6, 31, 5.1)
        );
    }

    @FXML
    private void handleExecuteProgram() {
        ProgramDashboardDTO selected = programsTableView.getSelectionModel().getSelectedItem();
        if (selected != null && executeProgramCallback != null) {
            executeProgramCallback.accept(selected.getProgramName());
            System.out.println("Execute program requested: " + selected.getProgramName());
        }
    }

    public void initComponent(Consumer<String> executeProgramCallback) {
        this.executeProgramCallback = executeProgramCallback;
    }

    public void setPrograms(@NotNull ObservableList<ProgramDashboardDTO> programs) {
        programsList.setAll(programs);
    }

    public void clearPrograms() {
        programsList.clear();
    }
}