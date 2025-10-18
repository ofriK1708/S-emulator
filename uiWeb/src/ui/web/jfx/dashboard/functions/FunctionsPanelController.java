package ui.web.jfx.dashboard.functions;

import dto.engine.ProgramDTO;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.jetbrains.annotations.NotNull;
import system.controller.EngineController;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Controller for the Functions Panel.
 * Displays available functions with their metadata in a table.
 */
public class FunctionsPanelController {

    private final ObservableList<FunctionDTO> functionsList = FXCollections.observableArrayList();

    @FXML
    private TableView<FunctionDTO> functionsTableView;
    @FXML
    private TableColumn<FunctionDTO, String> functionNameColumn;
    @FXML
    private TableColumn<FunctionDTO, String> programNameColumn;
    @FXML
    private TableColumn<FunctionDTO, String> uploadedByColumn;
    @FXML
    private TableColumn<FunctionDTO, Number> instructionCountColumn;
    @FXML
    private TableColumn<FunctionDTO, Number> maxExpandLevelColumn;
    @FXML
    private Button executeFunctionButton;

    private Consumer<String> executeFunctionCallback;
    private EngineController engineController;
    private BooleanProperty programLoadedProperty;

    @FXML
    public void initialize() {
        setupTableColumns();

        // Enable/disable execute button based on selection
        executeFunctionButton.disableProperty().bind(
                functionsTableView.getSelectionModel().selectedItemProperty().isNull()
        );

        System.out.println("FunctionsPanelController initialized");
    }

    private void setupTableColumns() {
        // Bind columns to FunctionDTO properties
        functionNameColumn.setCellValueFactory(cellData ->
                cellData.getValue().functionName());
        programNameColumn.setCellValueFactory(cellData ->
                cellData.getValue().programName());
        uploadedByColumn.setCellValueFactory(cellData ->
                cellData.getValue().uploadedBy());
        instructionCountColumn.setCellValueFactory(cellData ->
                cellData.getValue().instructionCount());
        maxExpandLevelColumn.setCellValueFactory(cellData ->
                cellData.getValue().maxExpandLevel());

        functionsTableView.setItems(functionsList);
    }

    /**
     * Initialize component with necessary dependencies
     */
    public void initComponent(@NotNull BooleanProperty programLoadedProperty,
                              @NotNull BooleanProperty fileLoadedProperty,
                              @NotNull Consumer<String> executeFunctionCallback) {
        this.programLoadedProperty = programLoadedProperty;
        this.executeFunctionCallback = executeFunctionCallback;

        // Bind execute button to both program loaded and selection state
        executeFunctionButton.disableProperty().bind(
                programLoadedProperty.not()
                        .or(functionsTableView.getSelectionModel().selectedItemProperty().isNull())
        );

        // Listen for file load events to refresh function data
        fileLoadedProperty.addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                refreshFunctionData();
            }
        });
    }

    /**
     * Refresh function data from engine controller
     */
    public void refreshFunctionData() {
        if (engineController == null) {
            System.err.println("EngineController not set - cannot refresh function data");
            return;
        }

        try {
            // Get main program info
            ProgramDTO basicProgram = engineController.getBasicProgram();

            if (basicProgram == null) {
                return;
            }

            String mainProgramName = basicProgram.ProgramName();

            // Get all functions from engine
            Map<String, String> functions = getFunctionsFromEngine();

            functionsList.clear();

            // Create FunctionDTO for each function
            for (Map.Entry<String, String> entry : functions.entrySet()) {
                String functionKey = entry.getKey();
                String functionDisplayName = entry.getValue();

                try {
                    // Get function-specific program data
                    int instructionCount = getInstructionCountForFunction(functionKey);
                    int maxExpandLevel = engineController.getMaxExpandLevel();

                    FunctionDTO functionDTO = new FunctionDTO(
                            new SimpleStringProperty(functionDisplayName),
                            new SimpleStringProperty(mainProgramName),
                            new SimpleStringProperty("System"), // Default uploader
                            new SimpleIntegerProperty(instructionCount),
                            new SimpleIntegerProperty(maxExpandLevel)
                    );

                    functionsList.add(functionDTO);
                } catch (Exception e) {
                    System.err.println("Error loading function " + functionKey + ": " + e.getMessage());
                }
            }

            System.out.println("Functions panel refreshed with " + functionsList.size() + " functions");

        } catch (Exception e) {
            System.err.println("Error refreshing function data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get functions from engine controller
     * Adapts to your EngineController's actual API
     */
    private @NotNull Map<String, String> getFunctionsFromEngine() {
        // Check if EngineController has getFunctionsSet() method
        // If not, return empty map for now
        try {
            // Attempt to get functions via reflection or direct call
            // This assumes LocalEngineController has getFunctionsSet()
            if (engineController instanceof system.controller.LocalEngineController localEngine) {
                return localEngine.getFunctionsSet();
            }
        } catch (Exception e) {
            System.err.println("Could not retrieve functions: " + e.getMessage());
        }

        return Map.of(); // Return empty map if not available
    }

    /**
     * Get instruction count for a specific function
     */
    private int getInstructionCountForFunction(@NotNull String functionKey) {
        try {
            // This might need adjustment based on how functions are accessed
            // For now, return base program instruction count
            ProgramDTO program = engineController.getBasicProgram();
            return program != null ? program.instructions().size() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    @FXML
    private void handleExecuteFunction() {
        FunctionDTO selected = functionsTableView.getSelectionModel().getSelectedItem();
        if (selected != null && executeFunctionCallback != null) {
            String functionName = selected.functionName().get();
            System.out.println("Execute function requested: " + functionName);
            executeFunctionCallback.accept(functionName);
        }
    }

    /**
     * Set engine controller for data access
     */
    public void setEngineController(@NotNull EngineController engineController) {
        this.engineController = engineController;
    }

    /**
     * Manually set functions (for external updates)
     */
    public void setFunctions(@NotNull ObservableList<FunctionDTO> functions) {
        functionsList.setAll(functions);
    }

    /**
     * Clear all functions from the table
     */
    public void clearFunctions() {
        functionsList.clear();
    }
}