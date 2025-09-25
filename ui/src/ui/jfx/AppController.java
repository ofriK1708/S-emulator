package ui.jfx;

import dto.engine.ExecutionResultDTO;
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
import ui.jfx.runControls.RunControlsController;
import ui.jfx.statistics.HistoryStatsController;
import ui.jfx.summaryLine.SummaryLineController;
import ui.jfx.variables.VariablesTableController;
import ui.utils.UIUtils;

import java.io.File;
import java.util.*;

import static ui.utils.UIUtils.*;

public class AppController {

    @FXML private HBox fileHandler;
    @FXML private FileHandlerController fileHandlerController;
    @FXML private HBox cycles;
    @FXML private CyclesController cyclesController;
    @FXML private HBox programFunction;
    @FXML private ProgramFunctionController programFunctionController;
    @FXML private AnchorPane instructionsTable;
    @FXML private InstructionTableController instructionsTableController;
    @FXML private AnchorPane derivedInstructionsTable;
    @FXML private InstructionTableController derivedInstructionsTableController;
    @FXML private AnchorPane argumentsTable;
    @FXML private VariablesTableController argumentsTableController;
    @FXML private VBox runControls;
    @FXML private RunControlsController runControlsController;
    @FXML private TitledPane runControlsTitledPane;
    @FXML private TitledPane debugControlsTitledPane;
    @FXML private TitledPane variablesTitledPane;
    @FXML private TitledPane statisticsTitledPane;
    @FXML private AnchorPane allVarsTable;
    @FXML private HistoryStatsController historyStatsController;
    @FXML private SummaryLineController summaryLineController;
    @FXML private VariablesTableController allVarsTableController;
    @FXML private DebuggerController debugControlsController;
    @FXML private AnchorPane historyStats;

    // Arguments and variables
    private final ListProperty<VariableDTO> allVariablesDTO = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<VariableDTO> argumentsDTO = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final BooleanProperty argumentsLoaded = new SimpleBooleanProperty(false);

    // Core system controller
    private final ListProperty<InstructionDTO> programInstructions = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<InstructionDTO> derivedInstructions = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<ExecutionStatisticsDTO> executionStatistics = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<String> programVariablesNamesAndLabels = new SimpleListProperty<>(FXCollections.observableArrayList());

    private final @NotNull EngineController engineController;
    private @Nullable ProgramDTO loadedProgram = null;
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
    private Map<String, Integer> previousDebugVariables = new HashMap<>();
    private boolean isFirstDebugStep = true;


    public AppController() {
        this.engineController = new EngineController();
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
                    programVariablesNamesAndLabels);

            runControlsController.initComponent(this::RunProgram, this::prepareForTakingArguments, programLoaded, argumentsLoaded);
            cyclesController.initComponent(currentCycles);
            instructionsTableController.initializeMainInstructionTable(programInstructions, derivedInstructions);
            derivedInstructionsTableController.markAsDerivedInstructionsTable();
            derivedInstructionsTableController.setDerivedInstructionsTable(derivedInstructions);
            argumentsTableController.initArgsTable(argumentsDTO);
            allVarsTableController.initAllVarsTable(allVariablesDTO);
            historyStatsController.initComponent(executionStatistics);
            debugControlsController.initComponent(
                    this::debugStep,
                    this::debugStepBackward,
                    this::debugResume,
                    this::stopDebugSession
            );

            System.out.println("AppController initialized");
        } else {
            System.err.println("One or more controllers are not injected properly!");
            throw new IllegalStateException("FXML injection failed: required controllers are null.");
        }
    }

    private void bindTitlePanesExpansion() {
        runControlsTitledPane.expandedProperty().bind(Bindings.and(programLoaded, programRunning.not()));
        debugControlsTitledPane.expandedProperty().bind(Bindings.and(programLoaded, Bindings.and(debugMode, programFinished.not())));
        variablesTitledPane.expandedProperty().bind(Bindings.and(programLoaded,
                Bindings.or(argumentsLoaded, Bindings.or(programFinished, debugMode))));
        statisticsTitledPane.expandedProperty().bind(Bindings.and(programLoaded, Bindings.and(programRunning.not(), programRanAtLeastOnce)));
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
                    program -> { loadedProgram = program; loadingStage.close(); }
            );

            loadingStage.setOnShown(event -> fileLoaderController.initializeAndRunFileLoaderTaskThread(file.toPath(), engineController, uiAdapter));
            loadingStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error loading file: " + e.getMessage());
        }
    }

    public void prepareForTakingArguments() {
        if (loadedProgram == null) {
            showError("No program loaded. Please load a program first.");
            return;
        }

        if (programArguments.isEmpty()) programArguments.putAll(engineController.getSortedArguments());

        if (programArguments.isEmpty()) {
            argumentsLoaded.set(true);
            showSuccess("Program loaded successfully from: " + loadedProgram.ProgramName() + "\nNo variables required.");
            return;
        }

        getArgumentsFromUser();
        argumentsLoaded.set(true);
        argumentsDTO.setAll(UIUtils.extractArguments(programArguments));
        showSuccess("Program loaded successfully from: " + loadedProgram.ProgramName() + "\nArguments captured.");
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
        if (!programLoaded.get()) { showError("No program loaded"); return; }
        if (!argumentsLoaded.get()) { showError("Arguments not entered"); return; }

        switch (programRunType) {
            case REGULAR -> startRegularExecution();
            case DEBUG -> startDebugExecution();
            default -> showError("Unknown run type selected.");
        }
    }

    public void startRegularExecution() {
        try {
            int expandLevel = currentExpandLevel.get();
            programRunning.set(true);
            ExecutionResultDTO result = engineController.runLoadedProgram(expandLevel, programArguments);
            allVariablesDTO.setAll(UIUtils.getAllVariablesSorted(engineController, expandLevel));
            executionStatistics.add(engineController.getLastExecutionStatistics());

            ProgramDTO executedProgram = engineController.getProgramByExpandLevel(expandLevel);
            programInstructions.setAll(executedProgram.instructions());
            derivedInstructions.clear();
            currentCycles.set(result.numOfCycles());
            summaryLineController.updateCounts(executedProgram.instructions());

            updateProperties();
            showSuccess("Program executed successfully! Cycles: " + result.numOfCycles());
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
        if (!programLoaded.get()) { showError("No program loaded"); return; }
        if (expandLevel < 0 || expandLevel > maxExpandLevel.get()) { showError("Invalid expand level"); return; }

        try {
            currentExpandLevel.set(expandLevel);
            ProgramDTO program = engineController.getProgramByExpandLevel(expandLevel);
            derivedInstructions.clear();
            programInstructions.setAll(program.instructions());
            currentCycles.set(engineController.getCyclesCount(expandLevel));
            allVariablesDTO.clear();
            argumentsDTO.clear();
            summaryLineController.updateCounts(program.instructions());
            programVariablesNamesAndLabels.setAll(engineController.getAllVariablesAndLabelsNames(expandLevel));
            showInfo("Program expanded to level " + expandLevel);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error expanding program: " + e.getMessage());
        }
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
        programInstructions.clear();
        derivedInstructions.clear();
        allVariablesDTO.clear();
        argumentsDTO.clear();
        currentCycles.set(0);
        summaryLineController.clearCounts();
        instructionsTableController.highlightVariable(null);
        derivedInstructionsTableController.highlightVariable(null);
        showInfo("Loaded program cleared.");
    }

    private void handleVariableSelection(@Nullable String variableName) {
        instructionsTableController.highlightVariable(variableName);
        derivedInstructionsTableController.highlightVariable(variableName);
    }

    private void highlightCurrentInstruction(int instructionIndex) {
        javafx.application.Platform.runLater(() -> instructionsTableController.highlightCurrentInstruction(instructionIndex));
    }
    private @NotNull VariableDTO createVariableDTOWithChangeDetection(@NotNull String name, @NotNull Integer value) {
        boolean hasChanged = false;

        if (!isFirstDebugStep && previousDebugVariables.containsKey(name)) {
            Integer previousValue = previousDebugVariables.get(name);
            hasChanged = !Objects.equals(previousValue, value);

            // Debug logging (remove in production)
            if (hasChanged) {
                System.out.println("Variable changed: " + name + " from " + previousValue + " to " + value);
            }
        } else if (!isFirstDebugStep) {
            // Variable didn't exist before, so it's a new variable
            hasChanged = true;
            System.out.println("New variable detected: " + name + " = " + value);
        }

        return new VariableDTO(
                new SimpleStringProperty(name),
                new SimpleIntegerProperty(value),
                new SimpleBooleanProperty(hasChanged)
        );
    }

    // DEBUG METHODS
    public void startDebugExecution() {
        try {
            int expandLevel = currentExpandLevel.get();
            programRunning.set(true);
            debugMode.set(true);
            programFinished.set(false);

            // CRITICAL: Reset change tracking state for new debug session
            previousDebugVariables.clear();
            isFirstDebugStep = true;
            System.out.println("Debug session started - change tracking reset");

            if (debugControlsController != null) {
                debugControlsController.notifyDebugSessionStarted();
            }

            engineController.startDebugSession(expandLevel, programArguments);
            inDebugSession = true;

            showInitialDebugState();
            highlightCurrentInstruction(0);
            showInfo("Debug session started.");

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error starting debug execution: " + e.getMessage());
            debugMode.set(false);
            programRunning.set(false);
            inDebugSession = false;
            // Reset change tracking on error
            previousDebugVariables.clear();
            isFirstDebugStep = true;

            if (debugControlsController != null) {
                debugControlsController.notifyDebugSessionEnded();
            }
        }
    }

    private void showInitialDebugState() {
        try { currentCycles.set(0); }
        catch (Exception e) { e.printStackTrace(); }
    }

    private void updateDebugVariableStateWithResult(@NotNull ExecutionResultDTO result) {
        List<VariableDTO> variablesSorted = new ArrayList<>();
        Map<String, Integer> currentStepVariables = new HashMap<>();

        // 1. Add OUTPUT variable (always first)
        String outputName = ProgramUtils.OUTPUT_NAME;
        Integer outputValue = result.result();
        currentStepVariables.put(outputName, outputValue);
        variablesSorted.add(createVariableDTOWithChangeDetection(outputName, outputValue));

        // 2. Add argument variables (maintain order)
        result.argumentsValues().entrySet().stream()
                .sorted(Map.Entry.comparingByKey(UIUtils.programNameComparator))
                .forEach(entry -> {
                    String name = entry.getKey();
                    Integer value = entry.getValue();
                    currentStepVariables.put(name, value);
                    variablesSorted.add(createVariableDTOWithChangeDetection(name, value));
                });

        // 3. Add work variables (maintain order)
        result.workVariablesValues().entrySet().stream()
                .sorted(Map.Entry.comparingByKey(UIUtils.programNameComparator))
                .forEach(entry -> {
                    String name = entry.getKey();
                    Integer value = entry.getValue();
                    currentStepVariables.put(name, value);
                    variablesSorted.add(createVariableDTOWithChangeDetection(name, value));
                });

        // 4. Update UI collections
        allVariablesDTO.setAll(variablesSorted);
        argumentsDTO.setAll(UIUtils.extractArguments(result.argumentsValues()));

        // 5. Store current state as previous for next comparison
        previousDebugVariables.clear();
        previousDebugVariables.putAll(currentStepVariables);

        // 6. Mark that first step is completed
        isFirstDebugStep = false;

        System.out.println("Updated variables. Changed count: " +
                variablesSorted.stream().mapToInt(v -> v.hasChanged().get() ? 1 : 0).sum());
    }

    public void debugStep() {
        if (!inDebugSession) { showError("No debug session active"); return; }
        try {
            ExecutionResultDTO result = engineController.debugStep();
            int currentPC = engineController.getCurrentDebugPC();
            currentCycles.set(result.numOfCycles());
            highlightCurrentInstruction(currentPC);
            updateDebugVariableStateWithResult(result);

            if (engineController.isDebugFinished()) {
                if (!engineController.areDebugStatisticsFinalized()) engineController.finalizeDebugStatistics();
                executionStatistics.add(engineController.getLastExecutionStatistics());
                programRanAtLeastOnce.set(true);
                endDebugSession(result);
                showSuccess("Debug execution finished. Cycles: " + result.numOfCycles());
            } else showInfo("Step executed. PC: " + currentPC + ", Total cycles: " + result.numOfCycles());
        } catch (Exception e) { e.printStackTrace(); showError("Error during debug step: " + e.getMessage()); }
    }

    public void debugStepBackward() {
        if (!inDebugSession) { showError("No debug session active"); return; }
        try {
            ExecutionResultDTO result = engineController.debugStepBackward();
            int currentPC = engineController.getCurrentDebugPC();
            currentCycles.set(result.numOfCycles());
            highlightCurrentInstruction(currentPC);
            updateDebugVariableStateWithResult(result);
            showInfo("Stepped backward to PC: " + currentPC + ", Total cycles: " + result.numOfCycles());
        } catch (Exception e) { e.printStackTrace(); showError("Error during debug step backward: " + e.getMessage()); }
    }

    public void debugResume() {
        if (!inDebugSession) { showError("No debug session active"); return; }
        try {
            ExecutionResultDTO result = engineController.debugResume();
            instructionsTableController.highlightVariable(null);
            updateDebugVariableStateWithResult(result);
            currentCycles.set(result.numOfCycles());
            endDebugSession(result);
            showInfo("Program resumed and completed successfully!");
        } catch (Exception e) { e.printStackTrace(); showError("Error during debug resume: " + e.getMessage()); endDebugSession(null); }
    }

    public void stopDebugSession() {
        try {
            engineController.stopDebugSession();
            endDebugSession(null);
            showInfo("Debug session stopped");
        } catch (Exception e) { e.printStackTrace(); showError("Error stopping debug session: " + e.getMessage()); endDebugSession(null); }
    }

    private void endDebugSession(@Nullable ExecutionResultDTO result) {
        inDebugSession = false;
        debugMode.set(false);
        programRunning.set(false);
        programFinished.set(true);

        // CRITICAL: Reset change tracking when session ends
        previousDebugVariables.clear();
        isFirstDebugStep = true;
        System.out.println("Debug session ended - change tracking reset");

        if (result != null) {
            updateDebugVariableStateWithResult(result);
        }

        if (debugControlsController != null) {
            debugControlsController.notifyDebugSessionEnded();
        }
    }
}
