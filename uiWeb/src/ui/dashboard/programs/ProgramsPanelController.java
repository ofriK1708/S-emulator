package ui.dashboard.programs;

import dto.engine.ProgramMetadata;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

/**
 * Controller for the Programs Panel.
 * Displays available programs with their metadata and execution statistics.
 */
public class ProgramsPanelController {

    private final ObservableList<ProgramMetadata> programsList = FXCollections.observableArrayList();

    @FXML
    private TableView<ProgramMetadata> programsTableView;
    @FXML
    private TableColumn<ProgramMetadata, String> programNameColumn;
    @FXML
    private TableColumn<ProgramMetadata, String> uploadedByColumn;
    @FXML
    private TableColumn<ProgramMetadata, Number> instructionCountColumn;
    @FXML
    private TableColumn<ProgramMetadata, Number> maxExpandLevelColumn;
    @FXML
    private TableColumn<ProgramMetadata, Number> totalRunsColumn;
    @FXML
    private TableColumn<ProgramMetadata, Number> avgCreditCostColumn;
    @FXML
    private Button executeProgramButton;

    private BiConsumer<String, Float> executeProgramCallback;

    @FXML
    public void initialize() {
        setupTableColumns();

        // Enable/disable execute button based on selection
        executeProgramButton.disableProperty().bind(
                programsTableView.getSelectionModel().selectedItemProperty().isNull()
        );

        System.out.println("ProgramsPanelController initialized");
    }

    private void setupTableColumns() {
        // Bind columns to ProgramMetadata properties
        programNameColumn.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().name()));
        uploadedByColumn.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().uploadedBy()));
        instructionCountColumn.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().numOfInstructions()));
        maxExpandLevelColumn.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().maxExpandLevel()));
        totalRunsColumn.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().numberOfExecutions()));
        avgCreditCostColumn.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().averageCreditsCost()));

        programsTableView.setItems(programsList);
    }

    /**
     * Initialize component with necessary dependencies
     */
    public void initComponent(@NotNull BiConsumer<String, Float> executeProgramCallback,
                              ListProperty<ProgramMetadata> availableProgramsProperty) {

        this.executeProgramCallback = executeProgramCallback;

        // Bind execute button to both program loaded and selection state
        executeProgramButton.disableProperty().bind((
                programsTableView.getSelectionModel().selectedItemProperty().isNull())
        );
        // Bind the available programs property to the internal list
        programsTableView.itemsProperty().bind(availableProgramsProperty);
    }


    @FXML
    private void handleExecuteProgram() {
        ProgramMetadata selectedProgram = programsTableView.getSelectionModel().getSelectedItem();
        if (selectedProgram != null && executeProgramCallback != null) {
            String programName = selectedProgram.name();
            float avgCredits = selectedProgram.averageCreditsCost();
            System.out.println("Execute program requested: " + programName);
            executeProgramCallback.accept(programName, avgCredits);
        }
    }

    /**
     * Clear all programs from the table
     */
    public void clearPrograms() {
        programsList.clear();
    }
}