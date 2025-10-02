package ui.jfx.instruction;

import dto.engine.InstructionDTO;
import javafx.animation.FadeTransition;
import javafx.beans.property.BooleanProperty;
import dto.engine.BreakpointDTO;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ui.utils.AnimatedTableRow;
import ui.utils.UIUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class InstructionTableController {

    private boolean isDerivedMap = false;
    private @Nullable String currentHighlightedVariable = null;  // Track currently highlighted variable
    private int highlightedInstructionIndex = -1;
    private @Nullable Consumer<Integer> onBreakpointToggle;
    private final Set<Integer> breakpointLines = new HashSet<>();
    private @Nullable Integer currentBreakpointHitLine = null;

    @FXML
    private TableView<InstructionDTO> instructionTable;
    @FXML
    private TableColumn<InstructionDTO, Void> breakpointColumn; // NEW: Breakpoint button column
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

    public void clearHighlighting() {
        highlightVariable(null);
        clearBreakpointHitHighlight();
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

    /**
     * Initializes breakpoint functionality for the instruction table.
     */
    public void initializeBreakpointSupport(@NotNull Consumer<Integer> onBreakpointToggle) {
        this.onBreakpointToggle = onBreakpointToggle;

        // Setup breakpoint button column cell factory
        if (breakpointColumn != null) {
            breakpointColumn.setCellFactory(col -> new BreakpointButtonCell());
        }

        System.out.println("Breakpoint support initialized for instruction table");
    }

    /**
     * Custom cell for the breakpoint button column.
     */
    private class BreakpointButtonCell extends TableCell<InstructionDTO, Void> {
        private final Button breakpointButton = new Button();
        private final HBox container = new HBox(breakpointButton);

        public BreakpointButtonCell() {
            container.setAlignment(Pos.CENTER);
            breakpointButton.setMinSize(20, 20);
            breakpointButton.setMaxSize(20, 20);
            breakpointButton.getStyleClass().add("breakpoint-button");

            breakpointButton.setOnAction(event -> {
                InstructionDTO instruction = getTableRow().getItem();
                if (instruction != null && onBreakpointToggle != null) {
                    onBreakpointToggle.accept(instruction.index());
                }
            });
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                setGraphic(null);
                return;
            }

            InstructionDTO instruction = getTableRow().getItem();
            int lineNumber = instruction.index();
            boolean hasBreakpoint = breakpointLines.contains(lineNumber);
            boolean isHit = currentBreakpointHitLine != null && currentBreakpointHitLine == lineNumber;

            // Update button appearance based on breakpoint state
            breakpointButton.getStyleClass().removeAll("breakpoint-active", "breakpoint-hit");
            if (isHit) {
                breakpointButton.getStyleClass().add("breakpoint-hit");
                breakpointButton.setText("●");
            } else if (hasBreakpoint) {
                breakpointButton.getStyleClass().add("breakpoint-active");
                breakpointButton.setText("●");
            } else {
                breakpointButton.setText("○");
            }

            setGraphic(container);
        }
    }

    public void initializeMainInstructionTable(ListProperty<InstructionDTO> instructions,
                                               @NotNull ListProperty<InstructionDTO> derivedInstructions) {
        if (isDerivedMap) {
            throw new IllegalStateException("initializeMainInstructionTable called on derived map table");
        }

        instructionTable.setRowFactory(tv -> {
            TableRow<InstructionDTO> row = new TableRow<>() {
                @Override
                protected void updateItem(InstructionDTO item, boolean empty) {
                    super.updateItem(item, empty);
                    updateRowHighlighting(this, item);
                    updateBreakpointIndicator(this, item);
                }
            };

            // LEFT CLICK: Show derived instructions
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == javafx.scene.input.MouseButton.PRIMARY
                        && event.getClickCount() == 1) {
                    System.out.println("Row clicked: " + row.getItem());
                    InstructionDTO clicked = row.getItem();
                    derivedInstructions.setAll(clicked.derivedFromInstructions());
                }
            });

            return row;
        });

        instructionTable.itemsProperty().bind(instructions);
    }

    private void updateBreakpointIndicator(@NotNull TableRow<InstructionDTO> row, @Nullable InstructionDTO item) {
        if (item == null) {
            row.getStyleClass().removeAll("breakpoint-row", "breakpoint-hit-row");
            return;
        }

        int lineNumber = item.index();
        boolean hasBreakpoint = breakpointLines.contains(lineNumber);
        boolean isBreakpointHit = currentBreakpointHitLine != null && currentBreakpointHitLine == lineNumber;

        row.getStyleClass().removeAll("breakpoint-row", "breakpoint-hit-row");

        if (isBreakpointHit) {
            row.getStyleClass().add("breakpoint-hit-row");
        } else if (hasBreakpoint) {
            row.getStyleClass().add("breakpoint-row");
        }
    }

    public void updateBreakpoints(@NotNull List<BreakpointDTO> breakpoints) {
        breakpointLines.clear();
        for (BreakpointDTO bp : breakpoints) {
            if (bp.enabled()) {
                breakpointLines.add(bp.lineNumber());
            }
        }
        instructionTable.refresh();
        System.out.println("Updated breakpoint display: " + breakpointLines.size() + " breakpoints");
    }

    public void highlightBreakpointHit(@Nullable Integer lineNumber) {
        currentBreakpointHitLine = lineNumber;
        instructionTable.refresh();

        if (lineNumber != null) {
            instructionTable.scrollTo(Math.max(0, lineNumber - 2));
            instructionTable.getSelectionModel().select(lineNumber);
            System.out.println("Highlighted breakpoint hit at line " + lineNumber);
        }
    }

    public void clearBreakpointHitHighlight() {
        highlightBreakpointHit(null);
    }
}