package ui.jfx;

import dto.engine.ExecutionResultDTO;
import dto.engine.ExecutionStatisticsDTO;
import dto.engine.InstructionDTO;
import dto.engine.ProgramDTO;
import dto.ui.VariableDTO;
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
import system.controller.controller.EngineController;
import ui.jfx.VariableInputDialog.VariableInputDialogController;
import ui.jfx.cycles.CyclesController;
import ui.jfx.debugger.DebuggerController;
import ui.jfx.fileHandler.FileHandlerController;
import ui.jfx.fileLoader.FileLoaderController;
import ui.jfx.fileLoader.UIAdapterLoadFileTask;
import ui.jfx.instruction.InstructionTableController;
import ui.jfx.program.function.PaneMode;
import ui.jfx.program.function.ProgramFunctionController;
import ui.jfx.runControls.RunControlsController;
import ui.jfx.statistics.HistoryStatsController;
import ui.jfx.summaryLine.SummaryLineController;
import ui.jfx.variables.VariablesTableController;
import ui.utils.UIUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static ui.utils.UIUtils.*;

public class AppController {

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
    // Arguments and variables Lists
    private final ListProperty<VariableDTO> allVariablesDTO =
            new SimpleListProperty<>(FXCollections.observableArrayList());
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
    private TitledPane staticsTitledPane;
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
    @FXML
    private TitledPane statisticsTitledPane;
    @FXML
    private AnchorPane historyStats;

    private final ListProperty<VariableDTO> argumentsDTO =
            new SimpleListProperty<>(FXCollections.observableArrayList());
    private final BooleanProperty argumentsLoaded = new SimpleBooleanProperty(false);

    // Core system controller - same as console version
    private final ListProperty<InstructionDTO> programInstructions =
            new SimpleListProperty<>(FXCollections.observableArrayList());

    private final ListProperty<InstructionDTO> derivedInstructions =
            new SimpleListProperty<>(FXCollections.observableArrayList());

    private final ListProperty<ExecutionStatisticsDTO> executionStatistics =
            new SimpleListProperty<>(FXCollections.observableArrayList());

    private final ListProperty<String> programVariablesNamesAndLabels =
            new SimpleListProperty<>(FXCollections.observableArrayList());

    private final EngineController engineController;
    private ProgramDTO loadedProgram = null;
    private final Map<String, Integer> programArguments = new HashMap<>();
    private final BooleanProperty programLoaded = new SimpleBooleanProperty(false);
    private final BooleanProperty debugMode = new SimpleBooleanProperty(false);
    private final BooleanProperty programRanAtLeastOnce = new SimpleBooleanProperty(false);
    private final BooleanProperty programRunning = new SimpleBooleanProperty(false);
    private final BooleanProperty programFinished = new SimpleBooleanProperty(false);
    private final IntegerProperty maxExpandLevel = new SimpleIntegerProperty(0);
    private final IntegerProperty currentExpandLevel = new SimpleIntegerProperty(0);
    private final IntegerProperty currentCycles = new SimpleIntegerProperty(0);
    private boolean inDebugSession = false;

    public AppController() {
        this.engineController = new EngineController();

    }

    @FXML
    public void initialize() {
        if (fileHandlerController != null && cyclesController != null
                && programFunctionController != null && instructionsTableController != null &&
                derivedInstructionsTableController != null && argumentsTableController != null &&
                allVarsTableController != null && debugControlsController != null &&
                runControlsController != null && historyStatsController != null && summaryLineController != null) {

            fileHandlerController.initComponent(this::loadProgramFromFile, this::clearLoadedProgram);
            programFunctionController.initComponent(this::expandProgramToLevel, this::setPaneMode,
                    currentExpandLevel, maxExpandLevel, programLoaded, this::handleVariableSelection,
                    programVariablesNamesAndLabels);
            runControlsController.initComponent(this::RunProgram, this::prepareForTakingArguments, programLoaded, argumentsLoaded);
            cyclesController.initComponent(currentCycles);
            instructionsTableController.initializeMainInstructionTable(programInstructions, derivedInstructions);
            derivedInstructionsTableController.markAsDerivedInstructionsTable();
            derivedInstructionsTableController.setDerivedInstructionsTable(derivedInstructions);
            argumentsTableController.initArgsTable(argumentsDTO);
            allVarsTableController.initAllVarsTable(allVariablesDTO);
            debugControlsController.setAppController(this);
            historyStatsController.initComponent(executionStatistics);
            System.out.println("AppController initialized");
        } else {
            System.err.println("One or more controllers are not injected properly!");
            throw new IllegalStateException("FXML injection failed: required controllers are null.");
        }
    }

    private void bindTitlePanesExpansion() {
        // Bind properties to expand/collapse titled panes based on program state

        runControlsTitledPane.expandedProperty().bind(
                Bindings.and(programLoaded, programRunning.not()));

        debugControlsTitledPane.expandedProperty().bind(
                Bindings.and(programLoaded,
                        Bindings.and(debugMode, programFinished.not())));

        variablesTitledPane.expandedProperty().bind(
                Bindings.and(programLoaded,
                        Bindings.or(argumentsLoaded,
                                Bindings.or(programFinished, debugMode))));

        statisticsTitledPane.expandedProperty().bind(
                Bindings.and(programLoaded,
                        Bindings.and(programRunning.not(), programRanAtLeastOnce)));

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

    public void loadProgramFromFile(File file) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("fileLoader/fileLoader.fxml"));
            Parent root = loader.load();
            FileLoaderController fileLoaderController = loader.getController();

            Stage loadingStage = new Stage();
            loadingStage.initModality(Modality.APPLICATION_MODAL);
            loadingStage.setTitle("Loading File");
            loadingStage.setScene(new Scene(root, 1000, 183));
            UIAdapterLoadFileTask uiAdapter = new UIAdapterLoadFileTask(programLoaded::set,
                    argumentsLoaded::set,
                    programVariablesNamesAndLabels::setAll,
                    programInstructions::setAll,
                    derivedInstructions::clear,
                    summaryLineController::updateCounts,
                    maxExpandLevel::set,
                    currentExpandLevel::set,
                    currentCycles::set,
                    (program) -> {
                        loadedProgram = program;
                        loadingStage.close();
                    });
            loadingStage.setOnShown(event -> fileLoaderController.initializeAndRunFileLoaderTaskThread(
                    file.toPath(), engineController, uiAdapter));
            loadingStage.show();
        } catch (Exception e) {
            System.err.println("Error loading file: " + e.getMessage());
            e.printStackTrace();
            showError("Error loading file: " + e.getMessage());
        }
    }

    public void prepareForTakingArguments() {
        // Clear previous variables
        //programArguments.clear();
        //allVariablesDTO.clear();
        //argumentsDTO.clear();

        try {
            // Get required program arguments from SystemController
            if (programArguments.isEmpty()) {
                programArguments.putAll(engineController.getArguments());
            }

            if (programArguments.isEmpty()) {
                // No arguments needed, mark as entered
                argumentsLoaded.set(true);
                showSuccess("Program loaded successfully from: " + loadedProgram.ProgramName() +
                        "\nNo variables required. Ready for execution.");
                return;
            }

            // Create and show the multi-variable input dialog
            getArgumentsFromUser();
            argumentsLoaded.set(true);
            argumentsDTO.setAll(UIUtils.extractArguments(programArguments));
            // Display success message and show variables
            showSuccess("Program loaded successfully from: " + loadedProgram.ProgramName() +
                    "\nArguments captured. Ready for execution.");

        } catch (Exception e) {
            System.err.println("Error getting program arguments: " + e.getMessage());
            showError("Error getting program arguments: " + e.getMessage());
        }
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

    public void RunProgram(ProgramRunType programRunType) {
        if (!programLoaded.get()) {
            showError("No program loaded. Please load a program first.");
            return;
        }
        if (!argumentsLoaded.get()) {
            showError("Arguments not entered. Please load a program and enter arguments first.");
            return;
        }
        switch (programRunType) {
            case REGULAR:
                startRegularExecution();
                break;
            case DEBUG:
                startDebugExecution();
                break;
            default:
                showError("Unknown run type selected.");
        }
    }

    public void startRegularExecution() {
        try {
            int expandLevel = currentExpandLevel.get();
            System.out.println("Starting regular execution with arguments: " + programArguments);
            programRunning.set(true);

            // Execute the program using SystemController
            ExecutionResultDTO executionResult = engineController.runLoadedProgram(expandLevel, programArguments);
            allVariablesDTO.setAll(UIUtils.getAllVariablesSorted(engineController, expandLevel));
            executionStatistics.add(engineController.getLastExecutionStatistics());

            // Get the updated program state after execution
            ProgramDTO executedProgram = engineController.getProgramByExpandLevel(expandLevel);
            programInstructions.setAll(executedProgram.instructions());
            derivedInstructions.clear();
            currentCycles.set(executionResult.numOfCycles());
            summaryLineController.updateCounts(executedProgram.instructions());

            // Show execution results
            updateProperties();

            showSuccess("Program executed successfully!\n" +
                    "Cycles: " + executionResult.numOfCycles() + "\n" +
                    "Final memory state updated in instruction table.");

        } catch (IllegalArgumentException e) {
            System.err.println("Invalid arguments for execution: " + e.getMessage());
            showError("Invalid arguments for execution: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error during execution: " + e.getMessage());
            showError("Error during execution: " + e.getMessage());
        }
    }

    public void expandProgramToLevel(int expandLevel) {
        if (!programLoaded.get()) {
            showError("No program loaded. Please load a program first.");
            return;
        }

        if (expandLevel < 0 || expandLevel > maxExpandLevel.get()) {
            showError("Invalid expand level. Must be between 0 and " + maxExpandLevel.get());
            return;
        }

        try {
            currentExpandLevel.set(expandLevel);
            System.out.println("Program expanded to level: " + expandLevel);

            ProgramDTO program = engineController.getProgramByExpandLevel(expandLevel);
            System.out.println("Program at level " + expandLevel + " has " + program.instructions().size() + " instructions");
            derivedInstructions.clear();
            programInstructions.setAll(program.instructions());
            currentCycles.set(engineController.getCyclesCount(expandLevel));
            allVariablesDTO.clear();
            argumentsDTO.clear();
            summaryLineController.updateCounts(program.instructions()); // NEW: Update summary
            programVariablesNamesAndLabels.setAll(engineController.getAllVariablesAndLabelsNames(expandLevel));

            showInfo("Program expanded to level " + expandLevel);

        } catch (Exception e) {
            System.err.println("Error expanding program: " + e.getMessage());
            showError("Error expanding program: " + e.getMessage());
        }
    }

    private void updateProperties() {
        programRunning.set(false);
        programFinished.set(true);
        programRanAtLeastOnce.set(true);
    }

    public void clearLoadedProgram() {
        programLoaded.set(false);
        argumentsLoaded.set(false);
        maxExpandLevel.set(0);
        currentExpandLevel.set(0);
        loadedProgram = null;
        programArguments.clear();
        engineController.clearLoadedProgram();
        executionStatistics.clear();
        // Clear UI components
        programInstructions.clear();
        summaryLineController.clearCounts(); // NEW: Clear summary
        allVariablesDTO.clear();
        argumentsDTO.clear();
        derivedInstructions.clear();
        currentCycles.set(0);
        instructionsTableController.highlightVariable(null);
        derivedInstructionsTableController.highlightVariable(null);

        showInfo("Loaded program cleared.");
    }

    private void handleVariableSelection(String variableName) {
        // Highlight the variable in the main instruction table
        instructionsTableController.highlightVariable(variableName);

        // Also highlight in derived instructions table if it has content
        derivedInstructionsTableController.highlightVariable(variableName);

        if (variableName != null) {
            System.out.println("Highlighting variable '" + variableName + "' in all instruction tables");
        } else {
            System.out.println("Clearing variable highlighting in all instruction tables");
        }
    }

    public boolean isProgramLoaded() {
        return programLoaded.get();
    }


    //add this method to implement Debugger:
    public void startDebugExecution() {
        try {
            int expandLevel = currentExpandLevel.get();
            System.out.println("Starting debug execution with arguments: " + programArguments);

            programRunning.set(true);
            debugMode.set(true);
            if (debugControlsController != null) {
                debugControlsController.notifyDebugSessionStarted();
            }

            // Start debug session
            engineController.startDebugSession(expandLevel, programArguments);
            inDebugSession = true;

            // Get the program at this expand level for UI display
            ProgramDTO program = engineController.getProgramByExpandLevel(expandLevel);

            // Show initial debug state
            showInitialDebugState();

            // Highlight first instruction
            highlightCurrentInstruction(0);

            showInfo("Debug session started. Use step controls to debug the program.");

        } catch (Exception e) {
            System.err.println("Error starting debug execution: " + e.getMessage());
            showError("Error starting debug execution: " + e.getMessage());
            debugMode.set(false);
            programRunning.set(false);
            inDebugSession = false;
        }
    }
    private void showInitialDebugState() {
        try {
            // Get initial state without executing any instruction
            int currentPC = engineController.getCurrentDebugPC();

            // Show initial variable state with arguments applied


            // Update cycles (should be 0 initially)
            currentCycles.set(0);

        } catch (Exception e) {
            System.err.println("Error showing initial debug state: " + e.getMessage());
        }
    }


    public void debugStep() {
        if (!inDebugSession) {
            showError("No debug session active");
            return;
        }

        try {
            ExecutionResultDTO result = engineController.debugStep();
            int currentPC = engineController.getCurrentDebugPC();

            highlightCurrentInstruction(currentPC);
            showDebugState(result);

            if (engineController.isDebugFinished()) {
                showInfo("Program execution completed. Total cycles: " + result.numOfCycles());
                endDebugSession(result);
            } else {
                // Show current step info
                showInfo("Step executed. PC: " + currentPC + ", Total cycles: " + result.numOfCycles());
            }

        } catch (Exception e) {
            System.err.println("Error during debug step: " + e.getMessage());
            showError("Error during debug step: " + e.getMessage());
        }
    }

    public void debugStepBackward() {
        if (!inDebugSession) {
            showError("No debug session active");
            return;
        }

        try {
            ExecutionResultDTO result = engineController.debugStepBackward();
            int currentPC = engineController.getCurrentDebugPC();

            highlightCurrentInstruction(currentPC);
            showDebugState(result);

            // Note: Cycles should remain monotonic even when stepping backward
            showInfo("Stepped backward to PC: " + currentPC +
                    ", Total execution cycles: " + result.numOfCycles());

        } catch (Exception e) {
            System.err.println("Error during debug step backward: " + e.getMessage());
            showError("Error during debug step backward: " + e.getMessage());
        }
    }

    public void debugResume() {
        if (!inDebugSession) {
            showError("No debug session active");
            return;
        }

        try {
            ExecutionResultDTO result = engineController.debugResume();

            // Clear instruction highlighting
            instructionsTableController.highlightVariable(null);
            showDebugState(result);
            endDebugSession(result);

            showInfo("Program resumed and completed successfully!");

        } catch (Exception e) {
            System.err.println("Error during debug resume: " + e.getMessage());
            showError("Error during debug resume: " + e.getMessage());
            endDebugSession(null);
        }
    }

    public void stopDebugSession() {
        try {
            engineController.stopDebugSession();
            endDebugSession(null);
            showInfo("Debug session stopped");

        } catch (Exception e) {
            System.err.println("Error stopping debug session: " + e.getMessage());
            showError("Error stopping debug session: " + e.getMessage());
        }
    }

    private void endDebugSession(ExecutionResultDTO finalResult) {
        inDebugSession = false;
        debugMode.set(false);
        programRunning.set(false);

        if (debugControlsController != null) {
            debugControlsController.notifyDebugSessionEnded();
        }

        if (finalResult != null) {
            programFinished.set(true);
            programRanAtLeastOnce.set(true);
            currentCycles.set(finalResult.numOfCycles());
            // updateExecutionStatistics(finalResult);
            // updateVariableDropdown();
        }

        // Clear instruction highlighting
        instructionsTableController.highlightVariable(null);
    }

    private void highlightCurrentInstruction(int instructionIndex) {
        // Scroll to and highlight current instruction
        javafx.application.Platform.runLater(() -> {
            instructionsTableController.highlightCurrentInstruction(instructionIndex);
        });
    }

    private void showDebugState(ExecutionResultDTO result) {
        // Update cycles with monotonic counter
        currentCycles.set(result.numOfCycles());
    }
}