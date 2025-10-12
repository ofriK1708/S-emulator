package uiweb.jfx.dashboard.programs;

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

    private final ObservableList<ProgramDashboardDTO> programsList = FXCollections.observableArrayList();
    @FXML
    private TableView<ProgramDashboardDTO> programsTableView;
    @FXML
    private TableColumn<ProgramDashboardDTO, String> programNameColumn;
    @FXML
    private TableColumn<ProgramDashboardDTO, String> uploadedByColumn;
    @FXML
    private TableColumn<ProgramDashboardDTO, Number> instructionCountColumn;
    @FXML
    private TableColumn<ProgramDashboardDTO, Number> maxExpandLevelColumn;
    @FXML
    private TableColumn<ProgramDashboardDTO, Number> totalRunsColumn;
    @FXML
    private TableColumn<ProgramDashboardDTO, Number> avgCreditCostColumn;
    @FXML
    private Button executeProgramButton;
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
    }

    @FXML
    private void handleExecuteProgram() {
        ProgramDashboardDTO selectedProgram = programsTableView.getSelectionModel().getSelectedItem();
        if (selectedProgram != null && executeProgramCallback != null) {
            executeProgramCallback.accept(selectedProgram.programName().get());
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