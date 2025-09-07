package jfx.instruction;

import dto.engine.InstructionDTO;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import jfx.AppController;

import java.util.List;
import java.util.Map;

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
        // Auto row number
        indexColumn.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(instructionTable.getItems().indexOf(cellData.getValue()) + 1)
        );
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

    public void setDerivedInstructions(Map<InstructionDTO, Integer> instructionDTOIntegerMap) {
        if (!isDerivedMap) {
            throw new IllegalStateException("setDerivedInstructions called on non-derived map table");
        }
        // Clear previous data
        instructionTable.getItems().clear();

        if (instructionDTOIntegerMap == null || instructionDTOIntegerMap.isEmpty()) {
            return;
        }

        // Fill the table with the keys
        instructionTable.getItems().addAll(instructionDTOIntegerMap.keySet());

        // Override index column for derived map mode
        indexColumn.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(instructionDTOIntegerMap.get(cellData.getValue()) + 1)
        );
    }
}
