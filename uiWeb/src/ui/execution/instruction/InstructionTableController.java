package ui.execution.instruction;

import dto.engine.InstructionDTO;
import engine.utils.ArchitectureType;
import javafx.animation.FadeTransition;
import javafx.beans.property.*;
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
    private @Nullable String currentHighlightedVariable = null;
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
    public TableColumn<InstructionDTO, String> architectureTypeColumn;

    @FXML
    private BooleanProperty animationsEnabledProperty = new SimpleBooleanProperty(true);

    public void markAsDerivedInstructionsTable() {
        isDerivedMap = true;
    }

    private ArchitectureType currentSelectedArchitectureType;

    @FXML
    public void initialize() {
        // Bind columns
        indexColumn.setCellValueFactory(callData ->
                new ReadOnlyIntegerWrapper(callData.getValue().index() + 1));
        typeColumn.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue().type().getSymbol()));
        labelColumn.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue().label()));
        commandColumn.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue().command()));
        cyclesColumn.setCellValueFactory(cellData ->
                new ReadOnlyIntegerWrapper(cellData.getValue().cycles()));
        architectureTypeColumn.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue().architectureType().getSymbol()));
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
     */
    public void highlightVariable(@Nullable String variableName) {
        currentHighlightedVariable = variableName;
        instructionTable.refresh();

        if (variableName != null) {
            System.out.println("Highlighting variable: " + variableName + " in instruction table");
            instructionTable.getSelectionModel().clearSelection();

            List<InstructionDTO> items = instructionTable.getItems();
            for (int targetIndex = 0; targetIndex < items.size(); targetIndex++) {
                if (instructionContainsVariable(items.get(targetIndex), variableName)) {
                    int scrollToIndex = Math.max(0, targetIndex - 2);
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

    public void clearSelectionHighlight() {
        highlightVariable(null);
    }

    public void clearAllHighlighting() {
        clearSelectionHighlight();
        clearAllDebugHighlighting();
        clearArchitectureHighlighting();
    }

    private void updateRowHighlighting(@NotNull TableRow<InstructionDTO> row, @Nullable InstructionDTO item) {
        // Remove any previous animation
        FadeTransition ft = (FadeTransition) row.getProperties().get("highlightFade");
        if (ft != null) {
            ft.stop();
            row.setOpacity(1.0);
            row.getProperties().remove("highlightFade");
        }
        if (item != null) {
            boolean instructionContainsVariable = instructionContainsVariable(item, currentHighlightedVariable);
            boolean debugInstructionHighlight = (row.getIndex() == highlightedInstructionIndex);
            boolean shouldHighlightForArchitecture = currentSelectedArchitectureType != null;

            // Apply/remove variable highlighting
            if (instructionContainsVariable) {
                if (!row.getStyleClass().contains("highlighted-row")) {
                    row.getStyleClass().add("highlighted-row");
                }
                UIUtils.checkIfShouldAnimate(row, animationsEnabledProperty.get());
            } else {
                row.getStyleClass().removeAll("highlighted-row");
            }

            // Apply/remove debug instruction highlighting
            if (debugInstructionHighlight) {
                if (!row.getStyleClass().contains("highlighted-row-debug")) {
                    row.getStyleClass().add("highlighted-row-debug");
                }
            } else {
                row.getStyleClass().removeAll("highlighted-row-debug");
            }
            if (shouldHighlightForArchitecture) {
                // it the instruction's architecture type is greater than the current selected architecture type,
                // it means the instruction cannot be executed on the current architecture
                if (item.architectureType().compareTo(currentSelectedArchitectureType) > 0) {
                    if (!row.getStyleClass().contains("highlighted-row-inadequate-architecture")) {
                        row.getStyleClass().remove("highlighted-row-suffice-architecture"); // clear if previously set
                        row.getStyleClass().add("highlighted-row-inadequate-architecture");
                    }
                } else {
                    if (!row.getStyleClass().contains("highlighted-row-suffice-architecture")) {
                        row.getStyleClass().remove("highlighted-row-inadequate-architecture"); // clear if previously
                        // set
                        row.getStyleClass().add("highlighted-row-suffice-architecture");
                    }
                }

            } else {
                row.getStyleClass().removeAll("highlighted-row-suffice-architecture", "highlighted-row-inadequate" +
                        "-architecture");
            }
        }
    }

    private boolean instructionContainsVariable(@Nullable InstructionDTO instruction, @Nullable String variableName) {
        if (instruction == null || variableName == null) {
            return false;
        }
        String pattern = "\\b" + Pattern.quote(variableName) + "\\b";

        String command = instruction.command();
        if (command != null && command.matches(".*" + pattern + ".*")) {
            return true;
        }

        String label = instruction.label();
        return label != null && label.matches(".*" + pattern + ".*");
    }

    /**
     * Highlights the current instruction being executed in debug mode
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

    public void highlightArchitectureInstructions(@NotNull ArchitectureType architectureType) {
        this.currentSelectedArchitectureType = architectureType;
        instructionTable.refresh();
    }

    public void clearArchitectureHighlighting() {
        this.currentSelectedArchitectureType = null;
        instructionTable.refresh();
    }
}