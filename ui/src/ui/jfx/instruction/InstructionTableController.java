package ui.jfx.instruction;

import dto.engine.InstructionDTO;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

import java.util.List;

public class InstructionTableController {

    private boolean isDerivedMap = false;
    private String currentHighlightedVariable = null; // Track currently highlighted variable

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
    private TableColumn<InstructionDTO, Number> cyclesColumn;

    public void markAsDerivedInstructionsTable() {
        isDerivedMap = true;
    }

    @FXML
    public void initialize() {
        // Bind columns
        indexColumn.setCellValueFactory(callData ->
                new ReadOnlyObjectWrapper<>(callData.getValue().index() + 1));
        typeColumn.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().type().getSymbol()));
        labelColumn.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().label()));
        commandColumn.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().command()));
        cyclesColumn.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().cycles()));
    }

    public void initializeMainInstructionTable(ListProperty<InstructionDTO> instructions,
                                               ListProperty<InstructionDTO> derivedInstructions) {
        if (isDerivedMap) {
            throw new IllegalStateException("initializeMainInstructionTable called on derived map table");
        }

        instructionTable.setRowFactory(tv -> {
            TableRow<InstructionDTO> row = new TableRow<InstructionDTO>() {
                @Override
                protected void updateItem(InstructionDTO item, boolean empty) {
                    super.updateItem(item, empty);
                    updateRowHighlighting(this, item);
                }
            };

            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 1) {
                    System.out.println("Row clicked: " + row.getItem());
                    InstructionDTO clicked = row.getItem();
                    derivedInstructions.setAll(clicked.derivedFromInstructions());
                }
            });
            return row;
        });
        instructionTable.itemsProperty().bind(instructions);
    }

    public void initializeDerivedInstructionsTable(ListProperty<InstructionDTO> derivedInstructions) {
        if (!isDerivedMap) {
            throw new IllegalStateException("setDerivedInstructions called on non-derived map table");
        }
        instructionTable.itemsProperty().bind(derivedInstructions);
    }

    /**
     * Highlights instructions that contain the specified variable
     * @param variableName The variable name to highlight, or null to clear highlighting
     */
    public void highlightVariable(String variableName) {
        currentHighlightedVariable = variableName;

        // Refresh the table to update row highlighting
        instructionTable.refresh();

        if (variableName != null) {
            System.out.println("Highlighting variable: " + variableName + " in instruction table");

            // Find first matching row and scroll to TOP
            List<InstructionDTO> items = instructionTable.getItems();
            for (int i = 0; i < items.size(); i++) {
                if (instructionContainsVariable(items.get(i), variableName)) {
                    final int targetIndex = i;
                    javafx.application.Platform.runLater(() -> {
                        // Calculate visible rows to position at top
                        double tableHeight = instructionTable.getHeight();
                        double estimatedRowHeight = 28.0; // Typical row height
                        int visibleRows = (int) (tableHeight / estimatedRowHeight);

                        // Scroll to position target row at top
                        int scrollToIndex = Math.max(0, targetIndex - 2); // -2 for header padding
                        instructionTable.scrollTo(scrollToIndex);

                        // Alternative: Force selection then clear to ensure top positioning
                        instructionTable.getSelectionModel().select(targetIndex);
                        javafx.application.Platform.runLater(() -> {
                            instructionTable.getSelectionModel().clearSelection();
                        });
                    });
                    break;
                }
            }
        } else {
            System.out.println("Clearing variable highlighting in instruction table");
        }
    }


    /**
     * Updates the highlighting for a specific row based on the current highlighted variable
     */
    private void updateRowHighlighting(TableRow<InstructionDTO> row, InstructionDTO item) {
        if (item == null || currentHighlightedVariable == null) {
            // Clear highlighting
            row.getStyleClass().removeAll("highlighted-row");
            return;
        }

        // Check if this instruction contains the highlighted variable
        boolean shouldHighlight = instructionContainsVariable(item, currentHighlightedVariable);

        if (shouldHighlight) {
            if (!row.getStyleClass().contains("highlighted-row")) {
                row.getStyleClass().add("highlighted-row");
            }
        } else {
            row.getStyleClass().removeAll("highlighted-row");
        }
    }

    /**
     * Determines if an instruction contains a reference to the specified variable
     */
    private boolean instructionContainsVariable(InstructionDTO instruction, String variableName) {
        if (instruction == null || variableName == null) {
            return false;
        }

        // Check in the command field - this is where variable references typically appear
        String command = instruction.command();
        if (command != null && command.contains(variableName)) {
            return true;
        }

        // Check in the label field as well
        String label = instruction.label();
        return label != null && label.contains(variableName);
    }

    public void clearInstructions() {
        instructionTable.getItems().clear();
        currentHighlightedVariable = null; // Clear highlighting when clearing instructions
    }
}
