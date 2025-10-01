package ui.jfx.instruction;

import dto.engine.InstructionDTO;
import javafx.animation.FadeTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ui.utils.AnimatedTableRow;
import ui.utils.UIUtils;

import java.util.List;
import java.util.regex.Pattern;

public class InstructionTableController {

    private boolean isDerivedMap = false;
    private @Nullable String currentHighlightedVariable = null;  // Track currently highlighted variable
    private int highlightedInstructionIndex = -1;

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

    @FXML
    private BooleanProperty animationsEnabledProperty = new SimpleBooleanProperty(true);

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

    public void initializeMainInstructionTable(@NotNull ListProperty<InstructionDTO> instructions,
                                               @NotNull ListProperty<InstructionDTO> derivedInstructions,
                                               @NotNull BooleanProperty animationsEnabledProperty) {
        if (isDerivedMap) {
            throw new IllegalStateException("initializeMainInstructionTable called on derived map table");
        }

        instructionTable.setRowFactory(tv -> {
            TableRow<InstructionDTO> row = new TableRow<>() {
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
        this.animationsEnabledProperty.bind(animationsEnabledProperty);
    }

    public void setDerivedInstructionsTable(ListProperty<InstructionDTO> derivedInstructions,
                                            BooleanProperty animationsEnabledProperty) {
        if (!isDerivedMap) {
            throw new IllegalStateException("setDerivedInstructions called on non-derived map table");
        }
        this.animationsEnabledProperty.bind(animationsEnabledProperty);
        instructionTable.setRowFactory(tv ->
                new AnimatedTableRow<>(animationsEnabledProperty, 100, false));
        instructionTable.itemsProperty().bind(derivedInstructions);
    }

    /**
     * Highlights instructions that contain the specified variable
     *
     * @param variableName The variable name to highlight, or null to clear highlighting
     */
    public void highlightVariable(@Nullable String variableName) {
        currentHighlightedVariable = variableName;

        // Refresh the table to update row highlighting
        instructionTable.refresh();

        if (variableName != null) {
            System.out.println("Highlighting variable: " + variableName + " in instruction table");

            instructionTable.getSelectionModel().clearSelection();

            // Find first matching row and scroll to TOP
            List<InstructionDTO> items = instructionTable.getItems();
            for (int targetIndex = 0; targetIndex < items.size(); targetIndex++) {
                if (instructionContainsVariable(items.get(targetIndex), variableName)) {
                    // Scroll to position target row at top
                    int scrollToIndex = Math.max(0, targetIndex - 2); // -2 for header padding
                    instructionTable.scrollTo(scrollToIndex);
                    instructionTable.getSelectionModel().select(targetIndex);
                    break;
                }
            }
        } else {
            instructionTable.getSelectionModel().clearSelection();
            System.out.println("Clearing variable highlighting in instruction table");
        }
    }

    public void clearHighlighting() {
        highlightVariable(null);
    }


    /**
     * Updates the highlighting for a specific row based on the current highlighted variable
     */
    private void updateRowHighlighting(@NotNull TableRow<InstructionDTO> row, @Nullable InstructionDTO item) {
        // Remove any previous animation
        FadeTransition ft = (FadeTransition) row.getProperties().get("highlightFade");
        if (ft != null) {
            ft.stop();
            row.setOpacity(1.0);
            row.getProperties().remove("highlightFade");
        }
        if (item != null) {

            if (currentHighlightedVariable == null) {
                row.getStyleClass().removeAll("highlighted-row");

            }

            boolean instructionContainsVariable = instructionContainsVariable(item, currentHighlightedVariable);
            boolean debugInstructionHighlight = (row.getIndex() == highlightedInstructionIndex);

            if (instructionContainsVariable) {
                if (!row.getStyleClass().contains("highlighted-row")) {
                    row.getStyleClass().add("highlighted-row");
                }
                UIUtils.checkIfShouldAnimate(row, animationsEnabledProperty.get());
            } else {
                row.getStyleClass().removeAll("highlighted-row");
            }
            if (debugInstructionHighlight) {
                if (!row.getStyleClass().contains("highlighted-row-debug")) {
                    row.getStyleClass().add("highlighted-row-debug");
                }
            } else {
                row.getStyleClass().removeAll();
            }
        }
    }

    /**
     * Determines if an instruction contains a reference to the specified variable
     */
    private boolean instructionContainsVariable(@Nullable InstructionDTO instruction, @Nullable String variableName) {
        if (instruction == null || variableName == null) {
            return false;
        }
        String pattern = Pattern.quote(variableName); // Escape special regex characters
        pattern = "\\b" + pattern + "\\b"; // Match whole word
        pattern = ".*" + pattern + ".*"; // Match anywhere in the string

        // Check in the command field - this is where variable references typically appear
        String command = instruction.command();
        if (command != null && command.matches(pattern)) {
            return true;
        }

        // Check in the label field as well
        String label = instruction.label();
        return label != null && label.matches(pattern);
    }

    /**
     * Highlights the current instruction being executed in debug mode
     *
     * @param instructionIndex The index of the instruction to highlight (0-based)
     */
    public void highlightCurrentInstruction(int instructionIndex) {
        if (instructionIndex >= 0 && instructionIndex < instructionTable.getItems().size()) {
            highlightedInstructionIndex = instructionIndex;
            instructionTable.scrollTo(Math.max(0, instructionIndex - 2));
            instructionTable.refresh();
        }
    }

    public void clearAllDebugHighlighting() {
        highlightedInstructionIndex = -1;
        instructionTable.refresh();
    }
}
