package ui.jfx;

import dto.engine.BreakpointDTO;
import dto.engine.ExecutionStatisticsDTO;
import dto.engine.InstructionDTO;
import dto.engine.ProgramDTO;
import dto.ui.VariableDTO;
import engine.utils.ProgramUtils;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import system.controller.EngineController;
import ui.jfx.VariableInputDialog.VariableInputDialogController;
import ui.jfx.cycles.CyclesController;
import ui.jfx.debugger.DebuggerController;
import ui.jfx.fileHandler.FileHandlerController;
import ui.jfx.fileLoader.FileLoaderController;
import ui.jfx.fileLoader.UIAdapterLoadFileTask;
import ui.jfx.instruction.InstructionTableController;
import ui.jfx.program.function.PaneMode;
import ui.jfx.program.function.ProgramFunctionController;
import ui.jfx.program.function.Theme;
import ui.jfx.runControls.RunControlsController;
import ui.jfx.statistics.HistoryStatsController;
import ui.jfx.summaryLine.SummaryLineController;
import ui.jfx.variables.VariablesTableController;
import ui.utils.UIUtils;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ui.utils.UIUtils.*;

public class AppController {

    private final Map<String, Integer> previousDebugVariables = new HashMap<>();

    // Scene reference for theme switching
    private Scene scene;

    @FXML
    private HBox fileHandler;
    @FXML
    private FileHandlerController fileHandlerController;
    @FXML
    private HBox cycles;
    @FXML
    private CyclesController cyclesController;
    @FXML
    private HBox programFunction;
    @FXML
    private ProgramFunctionController programFunctionController;
    @FXML
    private AnchorPane instructionsTable;
    @FXML
    private InstructionTableController instructionsTableController;
    @FXML
    private AnchorPane derivedInstructionsTable;
    @FXML
    private InstructionTableController derivedInstructionsTableController;
    @FXML
    private AnchorPane argumentsTable;
    @FXML
    private VariablesTableController argumentsTableController;
    @FXML
    private VBox runControls;
    @FXML
    private RunControlsController runControlsController;
    @FXML
    private TitledPane runControlsTitledPane;
    @FXML
    private TitledPane debugControlsTitledPane;
    @FXML
    private TitledPane variablesTitledPane;
    @FXML
    private TitledPane statisticsTitledPane;
    @FXML
    private AnchorPane allVarsTable;
    @FXML
    private HistoryStatsController historyStatsController;
    @FXML
    private SummaryLineController summaryLineController;
    @FXML
    private VariablesTableController allVarsTableController;
    @FXML
    private DebuggerController debugControlsController;

    // Arguments and variables
    private final ListProperty<VariableDTO> allVariablesDTO =
            new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<VariableDTO> previousVariablesDTO =
            new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<VariableDTO> argumentsDTO =
            new SimpleListProperty<>(FXCollections.observableArrayList());
    private final BooleanProperty argumentsLoaded = new SimpleBooleanProperty(false);
    private final ObjectProperty<Theme> selectedTheme = new SimpleObjectProperty<>(Theme.LIGHT);

    // Core system controller
    private final ListProperty<InstructionDTO> programInstructions =
            new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<InstructionDTO> derivedInstructions =
            new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<ExecutionStatisticsDTO> executionStatistics =
            new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<String> programVariablesNamesAndLabels =
            new SimpleListProperty<>(FXCollections.observableArrayList());
    private final MapProperty<String, String> allSubFunction =
            new SimpleMapProperty<>(FXCollections.observableHashMap());

    private final StringProperty mainProgramName = new SimpleStringProperty("");
    private final StringProperty currentLoadedProgramName = new SimpleStringProperty("");
    private final @NotNull EngineController engineController;
    private @Nullable ProgramDTO loadedProgram = null;
    private final Map<String, Integer> programArguments = new HashMap<>();

    private final BooleanProperty isAnimationsOn = new SimpleBooleanProperty(true);
    private final BooleanProperty programLoaded = new SimpleBooleanProperty(false);
    private final BooleanProperty debugMode = new SimpleBooleanProperty(false);
    private final BooleanProperty programRanAtLeastOnce = new SimpleBooleanProperty(false);
    private final BooleanProperty programRunning = new SimpleBooleanProperty(false);
    private final BooleanProperty programFinished = new SimpleBooleanProperty(false);
    private final IntegerProperty maxExpandLevel = new SimpleIntegerProperty(0);
    private final IntegerProperty currentExpandLevel = new SimpleIntegerProperty(0);
    private final IntegerProperty currentCycles = new SimpleIntegerProperty(0);
    private boolean inDebugSession = false;
    @FXML
    private VBox historyStats;
    private boolean isFirstDebugStep = true;
    private boolean isInDebugResume = false;

    public AppController() {
        this.engineController = new EngineController();
        // Set this instance for UIUtils re-run functionality
        UIUtils.setAppControllerInstance(this);
    }

    @FXML
    public void initialize() {
        if (fileHandlerController != null && cyclesController != null && programFunctionController != null &&
                instructionsTableController != null && derivedInstructionsTableController != null && argumentsTableController != null &&
                allVarsTableController != null && debugControlsController != null && runControlsController != null &&
                historyStatsController != null && summaryLineController != null) {

            fileHandlerController.initComponent(this::loadProgramFromFile, this::clearLoadedProgram);
            programFunctionController.initComponent(this::expandProgramToLevel, this::setPaneMode,
                    currentExpandLevel, maxExpandLevel, programLoaded, this::handleVariableSelection,
                    programVariablesNamesAndLabels, mainProgramName, allSubFunction, this::switchLoadedProgram,
                    this::onThemeSelect, isAnimationsOn);

            runControlsController.initComponent(this::RunProgram, this::prepareForTakingArguments, programLoaded,
                    argumentsLoaded);
            summaryLineController.initComponent(currentLoadedProgramName);
            cyclesController.initComponent(currentCycles);
            instructionsTableController.initializeMainInstructionTable(programInstructions, derivedInstructions,
                    isAnimationsOn);
            instructionsTableController.initializeBreakpointSupport(this::handleBreakpointToggle);
            derivedInstructionsTableController.markAsDerivedInstructionsTable();
            derivedInstructionsTableController.setDerivedInstructionsTable(derivedInstructions, isAnimationsOn);
            argumentsTableController.initArgsTable(argumentsDTO, isAnimationsOn);
            allVarsTableController.initAllVarsTable(allVariablesDTO, isAnimationsOn);


            // CLEANED: Only pass statistics data and variable states provider
            // Re-run functionality is now handled exclusively by the Show dialog
            historyStatsController.initComponent(
                    executionStatistics,
                    this::getFinalVariableStates,
                    isAnimationsOn
            );

            debugControlsController.initComponent(
                    this::debugStep,
                    this::debugStepBackward,
                    this::debugResume,
                    this::stopDebugSession
            );

            System.out.println("AppController initialized with cleaned re-run architecture");
        } else {
            System.err.println("One or more controllers are not injected properly!");
            throw new IllegalStateException("FXML injection failed: required controllers are null.");
        }
    }

    /**
     * Handles breakpoint toggle request from the instruction table.
     * Called when user right-clicks on an instruction line.
     *
     * @param lineNumber The line number where user wants to toggle breakpoint
     */
    private void handleBreakpointToggle(int lineNumber) {
        try {
            boolean nowHasBreakpoint = engineController.toggleBreakpoint(lineNumber);

            // Refresh breakpoint display
            List<BreakpointDTO> allBreakpoints = engineController.getAllBreakpoints();
            instructionsTableController.updateBreakpoints(allBreakpoints);

            if (nowHasBreakpoint) {
                showInfo("Breakpoint set at line " + (lineNumber + 1));
            } else {
                showInfo("Breakpoint removed from line " + (lineNumber + 1));
            }

        } catch (Exception e) {
            System.err.println("Error toggling breakpoint: " + e.getMessage());
            showError("Error toggling breakpoint: " + e.getMessage());
        }
    }

    /**
     * Retrieves the final variable states for a given execution.
     * This method is called by HistoryStatsController to get variable data for the Show dialog.
     *
     * @param executionStats The execution statistics for which to retrieve variable states
     * @return Map of variable names to their final values from that execution
     */
    private @NotNull Map<String, Integer> getFinalVariableStates(@NotNull ExecutionStatisticsDTO executionStats) {
        try {
            // Extract execution parameters
            int expandLevel = executionStats.expandLevel();
            Map<String, Integer> arguments = executionStats.arguments();

            System.out.println("Retrieving final variable states for execution #" +
                    executionStats.executionNumber() + " (expand level: " + expandLevel + ")");

            // Use the engine controller to get the final variable states for this execution
            Map<String, Integer> finalStates = engineController.getFinalVariableStates(expandLevel, arguments);

            System.out.println("Retrieved " + finalStates.size() + " final variable states for execution #" +
                    executionStats.executionNumber());

            return finalStates;

        } catch (Exception e) {
            System.err.println("Error retrieving final variable states: " + e.getMessage());
            showError("Error retrieving execution details: " + e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * PUBLIC METHOD: Handle re-run request from Show dialog.
     * This prepares the system state for re-run by expanding to the correct level
     * and pre-populating arguments.
     *
     * @param expandLevel       The expand level from the selected execution
     * @param previousArguments The arguments from the selected execution
     */
    public void handleRerunRequest(int expandLevel, @NotNull Map<String, Integer> previousArguments) {
        if (!programLoaded.get()) {
            showError("No program loaded");
            return;
        }

        try {
            System.out.println("Preparing re-run with expand level " + expandLevel +
                    " and arguments: " + previousArguments);

            // IMPORTANT: Preserve the current debug mode before reset
            boolean wasInDebugMode = debugMode.get();
            System.out.println("Preserving debug mode state: " + wasInDebugMode);

            // Step 1: Reset system state for new run (preserve execution statistics and debug mode)
            resetForRerun(wasInDebugMode);

            // Step 2: Expand program to the required level from the previous run
            expandProgramToLevel(expandLevel);

            // Step 3: Pre-populate arguments from the previous run
            programArguments.clear();
            programArguments.putAll(previousArguments);

            // Step 4: Trigger the normal Set Run dialog flow with pre-populated arguments
            // This will open the dialog, allow user to confirm/edit, then proceed with execution
            prepareForTakingArguments();
            if (wasInDebugMode) {
                debugMode.set(true);
                startDebugExecution();
            } else {
                debugMode.set(false);
                startRegularExecution();
            }


            System.out.println("Re-run preparation completed. Set Run dialog should open with pre-populated arguments" +
                    ".");
            System.out.println("Debug mode preserved: " + debugMode.get());

        } catch (Exception e) {
            System.err.println("Error preparing re-run: " + e.getMessage());
            showError("Error preparing re-run: " + e.getMessage());
        }
    }

    private void resetForRerun(boolean preserveDebugMode) {
        // Reset execution state properties (but preserve debug mode if requested)
        programRunning.set(false);
        programFinished.set(false);

        if (!preserveDebugMode) {
            debugMode.set(false);
        } else {
            System.out.println("Debug mode preserved for rerun: " + debugMode.get());
        }

        if (inDebugSession) {
            try {
                engineController.stopDebugSession();
            } catch (Exception e) {
                System.err.println("Warning: Error stopping debug session during rerun reset: " + e.getMessage());
            }
            inDebugSession = false;

            if (!preserveDebugMode && debugControlsController != null) {
                debugControlsController.notifyDebugSessionEnded();
            }
        }

        // Reset debug state tracking for new execution
        previousDebugVariables.clear();
        isFirstDebugStep = true;

        // Clear UI data (but preserve program instructions and metadata)
        allVariablesDTO.clear();

        // Reset arguments loaded state so the dialog will appear
        argumentsLoaded.set(false);
        argumentsDTO.clear();

        // Reset cycles counter
        currentCycles.set(0);

        // Clear instruction highlighting
        instructionsTableController.highlightVariable(null);
        instructionsTableController.clearAllDebugHighlighting();
        instructionsTableController.clearBreakpointHitHighlight();
        derivedInstructionsTableController.highlightVariable(null);

        System.out.println("System state reset completed for rerun (statistics and debug mode preserved)");
    }

    private void bindTitlePanesExpansion() {
        runControlsTitledPane.expandedProperty().bind(Bindings.and(programLoaded, programRunning.not()));
        debugControlsTitledPane.expandedProperty().bind(Bindings.and(programLoaded, Bindings.and(debugMode,
                programFinished.not())));
        variablesTitledPane.expandedProperty().bind(Bindings.and(programLoaded,
                Bindings.or(argumentsLoaded, Bindings.or(programFinished, debugMode))));
        statisticsTitledPane.expandedProperty().bind(Bindings.and(programLoaded, Bindings.and(programRunning.not(),
                programRanAtLeastOnce)));
    }

    private void unbindTitlePanesExpansion() {
        runControlsTitledPane.expandedProperty().unbind();
        debugControlsTitledPane.expandedProperty().unbind();
        variablesTitledPane.expandedProperty().unbind();
        statisticsTitledPane.expandedProperty().unbind();
    }

    public void setPaneMode(PaneMode paneMode) {
        if (paneMode == PaneMode.AUTO) {
            bindTitlePanesExpansion();
        } else {
            unbindTitlePanesExpansion();
        }
    }

    public void loadProgramFromFile(@NotNull File file) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("fileLoader/fileLoader.fxml"));
            Parent root = loader.load();
            FileLoaderController fileLoaderController = loader.getController();

            Stage loadingStage = new Stage();
            loadingStage.initModality(Modality.APPLICATION_MODAL);
            loadingStage.setTitle("Loading File");
            loadingStage.setScene(new Scene(root, 1000, 183));

            UIAdapterLoadFileTask uiAdapter = new UIAdapterLoadFileTask(
                    programLoaded::set,
                    argumentsLoaded::set,
                    programVariablesNamesAndLabels::setAll,
                    programInstructions::setAll,
                    derivedInstructions::clear,
                    summaryLineController::updateCounts,
                    maxExpandLevel::set,
                    currentExpandLevel::set,
                    currentCycles::set,
                    program -> {
                        if (program != null) {
                            UIUtils.showSuccess("File loaded successfully: " + file.getName());
                            loadedProgram = program;
                            mainProgramName.set(program.ProgramName());
                            currentLoadedProgramName.set(program.ProgramName());
                            allSubFunction.putAll(engineController.getFunctionsSet());
                        }
                        loadingStage.close();
                    }
            );

            loadingStage.setOnShown(event -> fileLoaderController.initializeAndRunFileLoaderTaskThread(
                    file.toPath(),
                    engineController, uiAdapter));
            loadingStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error loading file: " + e.getMessage());
        }
    }

    public void switchLoadedProgram(String functionName) {
        if (functionName == null || functionName.isEmpty()) {
            showError("Function name cannot be null or empty.");
            return;
        }
        if (!allSubFunction.containsKey(functionName) && !functionName.equals(mainProgramName.get())) {
            showError("Function '" + functionName + "' does not exist in the loaded program.");
            return;
        }
        try {
            setStageForLoadedProgram(functionName);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error retrieving instructions for function '" + functionName + "': " + e.getMessage());
        }
    }

    private void setStageForLoadedProgram(String functionName) {
        loadedProgram = engineController.setLoadedProgram(functionName);
        currentLoadedProgramName.set(loadedProgram.ProgramName());
        maxExpandLevel.set(engineController.getMaxExpandLevel());
        currentExpandLevel.set(0);
        programLoaded.set(true);
        argumentsLoaded.set(false);
        programRunning.set(false);
        programFinished.set(false);
        programArguments.clear();
        programInstructions.setAll(loadedProgram.instructions());
        derivedInstructions.clear();
        executionStatistics.setAll(engineController.getAllExecutionStatistics());
        programRanAtLeastOnce.set(!executionStatistics.isEmpty());
        instructionsTableController.clearHighlighting();
        derivedInstructionsTableController.clearHighlighting();
        allVariablesDTO.clear();
        argumentsDTO.clear();
        currentCycles.set(0);
        List<BreakpointDTO> existingBreakpoints = engineController.getAllBreakpoints();
        instructionsTableController.updateBreakpoints(existingBreakpoints);
        summaryLineController.updateCounts(loadedProgram.instructions());
        programVariablesNamesAndLabels.setAll(sortAllProgramNames(engineController.
                getAllVariablesAndLabelsNames(0, true)));
        showSuccess("Function '" + functionName + "' loaded successfully.");
        showInfo("Displaying instructions for function: " + functionName);
    }

    public void prepareForTakingArguments() {
        if (loadedProgram == null) {
            showError("No program loaded. Please load a program first.");
            return;
        }

        // If programArguments is empty, populate with default arguments from engine
        if (programArguments.isEmpty()) {
            programArguments.putAll(engineController.getSortedArguments());
        }

        // If no arguments are needed, mark as loaded and return
        if (programArguments.isEmpty()) {
            argumentsLoaded.set(true);
            showSuccess("Program loaded successfully from: " + loadedProgram.ProgramName() + "\nNo variables required" +
                    ".");
            return;
        }

        // Open the arguments dialog (will be pre-populated if coming from re-run)
        getArgumentsFromUser();

        // After dialog closes, update UI and mark arguments as loaded
        argumentsLoaded.set(true);
        argumentsDTO.setAll(UIUtils.formatArgumentsToVariableDTO(programArguments));

        // Show appropriate success message based on whether this is a rerun and current mode
        if (argumentsHaveNonZeroValues(programArguments)) {
            String modeText = debugMode.get() ? "Debug" : "Regular";
            showSuccess("Re-run arguments configured successfully!\n" +
                    "Values were pre-populated from previous execution.\n" +
                    "Current mode: " + modeText + "\n" +
                    "Click 'Run Program' to execute in " + modeText.toLowerCase() + " mode.");
        } else {
            showSuccess("Program loaded successfully from: " + loadedProgram.ProgramName() +
                    "\nArguments captured.");
        }
    }

    private boolean argumentsHaveNonZeroValues(@NotNull Map<String, Integer> arguments) {
        return arguments.values().stream().anyMatch(value -> value != null && value != 0);
    }

    public void getArgumentsFromUser() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(ArgumentResourcePath));
            Parent root = loader.load();
            VariableInputDialogController controller = loader.getController();
            controller.initialiseController(programArguments);

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle("Program Arguments");
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void RunProgram(@NotNull ProgramRunType programRunType) {
        if (!programLoaded.get()) {
            showError("No program loaded");
            return;
        }
        if (!argumentsLoaded.get()) {
            showError("Arguments not entered");
            return;
        }

        switch (programRunType) {
            case REGULAR -> startRegularExecution();
            case DEBUG -> startDebugExecution();
            default -> showError("Unknown run type selected.");
        }
        argumentsLoaded.set(false);
    }

    public void startRegularExecution() {
        try {
            int expandLevel = currentExpandLevel.get();
            programRunning.set(true);

            instructionsTableController.clearBreakpointHitHighlight();

            // Execute the program using SystemController
            engineController.runLoadedProgram(expandLevel, programArguments);
            allVariablesDTO.setAll(UIUtils.getAllVariablesDTOSorted(engineController, expandLevel));
            executionStatistics.add(engineController.getLastExecutionStatistics());

            ProgramDTO executedProgram = engineController.getProgramByExpandLevel(expandLevel);
            programInstructions.setAll(executedProgram.instructions());
            derivedInstructions.clear();
            currentCycles.set(engineController.getLastExecutionNumberOfCycles());
            summaryLineController.updateCounts(executedProgram.instructions());

            updateProperties();

            showSuccess("Program executed successfully!\n" +
                    "Cycles: " + currentCycles.get() + "\n" +
                    "Final memory state updated in instruction table.");

        } catch (IllegalArgumentException e) {
            System.err.println("Invalid arguments for execution: " + e.getMessage());
            showError("Invalid arguments for execution: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error during execution: " + e.getMessage());
        }
    }

    private void updateProperties() {
        programRunning.set(false);
        programFinished.set(true);
        programRanAtLeastOnce.set(true);
    }

    public void expandProgramToLevel(int expandLevel) {
        if (!programLoaded.get()) {
            showError("No program loaded");
            return;
        }
        if (expandLevel < 0 || expandLevel > maxExpandLevel.get()) {
            showError("Invalid expand level");
            return;
        }

        try {
            instructionsTableController.clearHighlighting();
            derivedInstructionsTableController.clearHighlighting();
            currentExpandLevel.set(expandLevel);
            ProgramDTO program = engineController.getProgramByExpandLevel(expandLevel);
            derivedInstructions.clear();
            programInstructions.setAll(program.instructions());
            allVariablesDTO.clear();
            argumentsDTO.clear();
            argumentsLoaded.set(false);
            summaryLineController.updateCounts(program.instructions());
            programVariablesNamesAndLabels.setAll(sortAllProgramNames(engineController.
                    getAllVariablesAndLabelsNames(expandLevel, true)));
            showInfo("Program expanded to level " + expandLevel);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error expanding program: " + e.getMessage());
        }
    }

    public void clearLoadedProgram() {
        programLoaded.set(false);
        currentLoadedProgramName.set("");
        mainProgramName.set("");
        argumentsLoaded.set(false);
        maxExpandLevel.set(0);
        currentExpandLevel.set(0);
        loadedProgram = null;
        programArguments.clear();
        engineController.clearAllBreakpoints();
        instructionsTableController.updateBreakpoints(Collections.emptyList());

        engineController.clearLoadedProgram();
        executionStatistics.clear();
        programInstructions.clear();
        derivedInstructions.clear();
        allVariablesDTO.clear();
        argumentsDTO.clear();
        currentCycles.set(0);
        summaryLineController.clearCounts();
        instructionsTableController.clearHighlighting();
        derivedInstructionsTableController.clearHighlighting();
        programVariablesNamesAndLabels.clear();
        allSubFunction.clear();
        previousDebugVariables.clear();
        engineController.clearAllBreakpoints();
        isFirstDebugStep = true;
        inDebugSession = false;
        debugMode.set(false);
        programRunning.set(false);
        programFinished.set(false);
        programRanAtLeastOnce.set(false);
        showSuccess("Program cleared.");
    }

    private void handleVariableSelection(@Nullable String variableName) {
        instructionsTableController.highlightVariable(variableName);
        derivedInstructionsTableController.highlightVariable(variableName);

        if (variableName != null) {
            System.out.println("Highlighting variable '" + variableName + "' in all instruction tables");
        } else {
            System.out.println("Clearing variable highlighting in all instruction tables");
        }
    }

    // DEBUG METHODS
    public void stopDebugSession() {
        try {
            engineController.stopDebugSession();

            if (engineController.isDebugFinished()) {
                showInfo("Debug session stopped. Execution was completed.");
            } else {
                showInfo("Debug session stopped at PC: " + engineController.getCurrentDebugPC());
            }

            int currentPC = engineController.getCurrentDebugPC();
            highlightCurrentInstruction(currentPC);
            updateDebugVariableState();
            handleExecutionFinished();

        } catch (Exception e) {
            System.err.println("Error stopping debug session: " + e.getMessage());
            showError("Error stopping debug session: " + e.getMessage());
        }
    }

    private void highlightCurrentInstruction(int instructionIndex) {
        instructionsTableController.highlightCurrentInstruction(instructionIndex);
    }

    private @NotNull VariableDTO createVariableDTOWithChangeDetection(@NotNull String name, @NotNull Integer value) {
        boolean hasChanged = false;

            if (!isFirstDebugStep && previousDebugVariables.containsKey(name)) {
                Integer previousValue = previousDebugVariables.get(name);
                hasChanged = !previousValue.equals(value);

                if (hasChanged) {
                    System.out.println("Variable changed: " + name + " from " + previousValue + " to " + value);
                }
            } else if (isFirstDebugStep) {
                // On the first step, consider non-zero and non-argument variables as changed
                hasChanged = value != 0 && !ProgramUtils.isArgument(name);
            }


        return new VariableDTO(
                new SimpleStringProperty(name),
                new SimpleIntegerProperty(value),
                new SimpleBooleanProperty(hasChanged)
        );
    }

    public void startDebugExecution() {
        try {
            int expandLevel = currentExpandLevel.get();
            programRunning.set(true);
            debugMode.set(true);
            programFinished.set(false);
            currentCycles.set(0);

            previousDebugVariables.clear();
            isFirstDebugStep = true;
            System.out.println("Debug session started - change tracking reset");

            if (debugControlsController != null) {
                debugControlsController.notifyDebugSessionStarted();
            }

            engineController.startDebugSession(expandLevel, programArguments);
            inDebugSession = true;

            highlightCurrentInstruction(0);
            showInfo("Debug session started.");

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error starting debug execution: " + e.getMessage());
            debugMode.set(false);
            programRunning.set(false);
            inDebugSession = false;
            previousDebugVariables.clear();
            isFirstDebugStep = true;

            if (debugControlsController != null) {
                debugControlsController.notifyDebugSessionEnded();
                showSuccess("Debug session ended.");
                programRunning.set(false);
            }
        }
    }

    public void debugStep() {
        if (!inDebugSession) {
            showError("No debug session active");
            return;
        }

        if (engineController.isDebugFinished()) {
            showInfo("Execution finished. Program has completed successfully.");
            return;
        }

        try {
            // Clear previous breakpoint hit highlighting
            instructionsTableController.clearBreakpointHitHighlight();

            if (!isFirstDebugStep) {
                previousDebugVariables.putAll(UIUtils.getAllVariablesMap(engineController, currentExpandLevel.get()));
            }

            // Execute one instruction
            engineController.debugStep();

            int currentPC = engineController.getCurrentDebugPC();
            currentCycles.set(engineController.getCurrentDebugCycles());
            highlightCurrentInstruction(currentPC);
            updateDebugVariableState();

            if (isFirstDebugStep) {
                isFirstDebugStep = false;
            }

            // Check if we hit a breakpoint at the new PC location
            if (engineController.hasBreakpointAt(currentPC)) {
                instructionsTableController.highlightBreakpointHit(currentPC);
                showInfo("Breakpoint hit at line " + (currentPC + 1) + ". Execution paused.");
            }

            if (engineController.isDebugFinished()) {
                executionStatistics.setAll(engineController.getAllExecutionStatistics());
                handleExecutionFinished();
            } else {
                showInfo("Step executed. PC: " + currentPC + ", Total cycles: " + currentCycles.get());
            }
        } catch (Exception e) {
            System.err.println("Error during debug step: " + e.getMessage());
            showError("Error during debug step: " + e.getMessage());
            endDebugSession();
        }
    }

    private void updateDebugVariableState() {
        List<VariableDTO> allVarNoChangeDetection = UIUtils.getAllVariablesDTOSorted(engineController,
                currentExpandLevel.get());
        List<VariableDTO> allVarWithChangeDetection = FXCollections.observableArrayList();
        allVarNoChangeDetection.stream()
                .map(var -> createVariableDTOWithChangeDetection(var.name().get(), var.value().get()))
                .forEach(allVarWithChangeDetection::add);

        allVariablesDTO.setAll(allVarWithChangeDetection);
    }

    public void debugStepBackward() {
        if (!inDebugSession) {
            showError("No debug session active");
            return;
        }

        if (engineController.isDebugFinished()) {
            showInfo("Execution finished. Cannot step backward from final state.");
            return;
        }

        try {
            previousDebugVariables.putAll(UIUtils.getAllVariablesMap(engineController, currentExpandLevel.get()));
            engineController.debugStepBackward();
            int currentPC = engineController.getCurrentDebugPC();
            currentCycles.set(engineController.getCurrentDebugCycles());

            highlightCurrentInstruction(currentPC);
            updateDebugVariableState();

            showInfo("Stepped backward to PC: " + currentPC +
                    ", Total execution cycles: " + currentCycles.get());

        } catch (Exception e) {
            System.err.println("Error during debug step backward: " + e.getMessage());
            showError("Error during debug step backward: " + e.getMessage());
            endDebugSession();
        }
    }

    public void debugResume() {
        if (!inDebugSession) {
            showError("No debug session active");
            return;
        }

        if (engineController.isDebugFinished()) {
            showInfo("Execution already finished.");
            return;
        }
        if (!isFirstDebugStep) {
            previousDebugVariables.clear();
            previousDebugVariables.putAll(UIUtils.getAllVariablesMap(engineController, currentExpandLevel.get()));
        }

        isInDebugResume = true;
        try {
            engineController.debugResume();

            // Update UI state
            int currentPC = engineController.getCurrentDebugPC();
            currentCycles.set(engineController.getCurrentDebugCycles());
            highlightCurrentInstruction(currentPC);
            updateDebugVariableState();

            // Check if we stopped at a breakpoint
            Integer breakpointHit = engineController.getLastBreakpointHit();
            if (breakpointHit != null) {
                // Stopped at breakpoint - highlight it
                instructionsTableController.highlightBreakpointHit(breakpointHit);
                showInfo("Breakpoint hit at line " + (breakpointHit + 1) +
                        ". Execution paused.\nPC: " + currentPC +
                        ", Total cycles: " + currentCycles.get());
            } else if (engineController.isDebugFinished()) {
                handleExecutionFinished();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error during debug resume: " + e.getMessage());
            endDebugSession();
        } finally {
            // ADDED: Always reset flag
            isInDebugResume = false;
        }
    }

    private void handleExecutionFinished() {
        try {
            endDebugSession();
            showSuccess("Execution completed successfully!\n" +
                    "Total cycles: " + currentCycles.get() + "\n" +
                    "Program finished. Use 'Run' to start a new execution.");
        } catch (Exception e) {
            System.err.println("Error handling execution completion: " + e.getMessage());
            showError("Error completing execution: " + e.getMessage());
        }
    }

    private void endDebugSession() {
        inDebugSession = false;
        isInDebugResume = false;
        debugMode.set(false);
        programRunning.set(false);
        programFinished.set(true);
        previousDebugVariables.clear();
        isFirstDebugStep = true;
        executionStatistics.add(engineController.getLastExecutionStatistics());
        programRanAtLeastOnce.set(true);
        debugControlsController.notifyDebugSessionEnded();
        instructionsTableController.clearAllDebugHighlighting();
        instructionsTableController.clearBreakpointHitHighlight();

        // Get current breakpoints with ACTIVE status (not HIT)
        List<BreakpointDTO> currentBreakpoints = engineController.getAllBreakpoints();
        instructionsTableController.updateBreakpoints(currentBreakpoints);

        System.out.println("Debug session ended - ready for new execution");
    }

    public void onThemeSelect(Theme theme) {
        selectedTheme.set(theme);
        applyTheme(theme);
        showInfo("Theme changed to: " + theme);
        System.out.println("Theme changed to: " + theme);
    }

    /**
     * Apply the selected theme to the scene
     */
    private void applyTheme(Theme theme) {
        if (scene == null) {
            System.err.println("Scene not set - cannot apply theme");
            return;
        }
        fileHandlerController.removeJokes();
        if (theme.equals(Theme.MAC_IS_THE_BEST)) {
            fileHandlerController.addAppleJoke();
        }
        if (theme.equals(Theme.WINDOWS_IS_THE_WORST)) {
            fileHandlerController.addWindowsJoke();
        }

        // Remove existing theme stylesheets
        scene.getStylesheets().removeIf(stylesheet ->
                stylesheet.contains("/styles/light.css") ||
                        stylesheet.contains("/styles/dark.css") ||
                        stylesheet.contains("/styles/mac.css") ||
                        stylesheet.contains("/styles/windows.css"));

        // Add component CSS files (always loaded)
        loadComponentStylesheets();

        // Add the selected theme stylesheet
        String themeStylesheet = getClass().getResource("/ui/jfx/styles/" +
                theme.getDisplayName().toLowerCase() + ".css").toExternalForm();
        scene.getStylesheets().add(themeStylesheet);

        System.out.println("Applied " + theme + " theme with stylesheet: " + themeStylesheet);
    }

    /**
     * Load all component stylesheets that contain layout and structural styles
     */
    private void loadComponentStylesheets() {
        String[] componentStylesheets = {
                "/ui/jfx/cycles/cycles.css",
                "/ui/jfx/runControls/RunControls.css",
                "/ui/jfx/debugger/Debugger.css",
                "/ui/jfx/summaryLine/SummaryLine.css",
                "/ui/jfx/fileHandler/fileHandler.css",
                "/ui/jfx/variables/variablesTable.css",
                "/ui/jfx/statistics/Statistics.css",
                "/ui/jfx/instruction/InstructionsTableStyle.css"
        };

        for (String stylesheet : componentStylesheets) {
            try {
                String stylesheetUrl = getClass().getResource(stylesheet).toExternalForm();
                if (!scene.getStylesheets().contains(stylesheetUrl)) {
                    scene.getStylesheets().add(stylesheetUrl);
                }
            } catch (Exception e) {
                System.err.println("Warning: Could not load stylesheet: " + stylesheet);
            }
        }
    }

    public void setScene(Scene scene) {
        this.scene = scene;
        // Apply the selected theme to the new scene
        applyTheme(selectedTheme.get());
    }
}