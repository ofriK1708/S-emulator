package ui.jfx.instruction;

import dto.engine.InstructionDTO;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import ui.jfx.AppController;

import java.util.List;

public class InstructionTableController {

    private boolean isDerivedMap = false;

    @FXML
    private TableView<InstructionDTO> instructionTable;
    @FXML
    private TableColumn<InstructionDTO, Integer> indexColumn;
    @FXML
    private TableColumn<InstructionDTO, String> typeColumn;
    @FXML
    private TableColumn<InstructionDTO, String> labelColumn;
    @FXML
    private TableColumn<InstructionDTO, String> commandColumn;
    @FXML
    private TableColumn<InstructionDTO, Integer> cyclesColumn;
    // Reference to the main controller (set by dependency injection)

    private AppController appController;

    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    public void setDerivedMap(boolean derivedMap) {
        isDerivedMap = derivedMap;
    }

    @FXML
    public void initialize() {
        // Bind columns
        indexColumn.setCellValueFactory(callData ->
                new ReadOnlyObjectWrapper<>(callData.getValue().index() + 1));
        typeColumn.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().type()));
        labelColumn.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().label()));
        commandColumn.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().command()));
        cyclesColumn.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().cycles()));

    }

    public void initializeMainInstructionTable() {
        if (isDerivedMap) {
            throw new IllegalStateException("initializeMainInstructionTable called on derived map table");
        }

        instructionTable.setRowFactory(tv -> {
            TableRow<InstructionDTO> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 1) {
                    System.out.println("Row clicked: " + row.getItem()); // TODO - fix not getting derived from map
                    InstructionDTO clicked = row.getItem();
                    if (appController != null) {
                        appController.displayDerivedFromMap(clicked.derivedFromInstructions());
                    }
                }
            });
            return row;
        });
    }
    // helper method to add instructions
    public void setInstructions(List<InstructionDTO> instructions) {
        instructionTable.getItems().setAll(instructions);
    }

    public void clearInstructions() {
        instructionTable.getItems().clear();
    }

    public void setDerivedInstructions(List<InstructionDTO> instructionDTOList) {
        if (!isDerivedMap) {
            throw new IllegalStateException("setDerivedInstructions called on non-derived map table");
        }
        // Clear previous data
        instructionTable.getItems().clear();

        if (instructionDTOList == null || instructionDTOList.isEmpty()) {
            return;
        }

        // Fill the table with the keys
        instructionTable.getItems().addAll(instructionDTOList);
    }
}
