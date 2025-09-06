package jfx.instruction;

import dto.engine.InstructionDTO;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import jfx.AppController;

import java.util.List;

public class InstructionTableController {

    @FXML
    private TableView<InstructionDTO> instructionTable;
    @FXML
    private TableColumn<InstructionDTO, Number> indexColumn;
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

    @FXML
    public void initialize() {
        // Bind columns
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        labelColumn.setCellValueFactory(new PropertyValueFactory<>("label"));
        commandColumn.setCellValueFactory(new PropertyValueFactory<>("command"));
        cyclesColumn.setCellValueFactory(new PropertyValueFactory<>("cycles"));

        // Auto row number
        indexColumn.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(instructionTable.getItems().indexOf(cellData.getValue()) + 1)
        );

        // Row click handler
        instructionTable.setRowFactory(tv -> {
            TableRow<InstructionDTO> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 1) {
                    InstructionDTO clicked = row.getItem();
                    if (appController != null) {
                        appController.handleDerivedMap(clicked.derivedFromInstructions());
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

}
