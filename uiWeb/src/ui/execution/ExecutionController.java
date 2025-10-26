package ui.execution;

import dto.engine.*;
import dto.server.SystemResponse;
import dto.ui.VariableDTO;
import engine.utils.ProgramUtils;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import system.controller.EngineController;
import system.controller.HttpEngineController;
import ui.execution.VariableInputDialog.VariableInputDialogController;
import ui.execution.cycles.CyclesController;
import ui.execution.debugger.DebuggerController;
import ui.execution.execution.header.ExecutionHeaderController;
import ui.execution.instruction.InstructionTableController;
import ui.execution.program.function.PaneMode;
import ui.execution.program.function.ProgramFunctionController;
import ui.execution.runControls.RunControlsController;
import ui.execution.summaryLine.SummaryLineController;
import ui.execution.variables.VariablesTableController;
import ui.task.program.ProgramTaskController;
import ui.task.program.load.UIAdapter;
import ui.utils.UIUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ui.utils.UIUtils.*;
import static ui.utils.clientConstants.TASK_PATH;

public class ExecutionController {

    private final Map<String, Integer> previousDebugVariables = new HashMap<>();

    private final ListProperty<VariableDTO> allVariablesDTO =
            new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<VariableDTO> previousVariablesDTO =
            new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<VariableDTO> argumentsDTO =
            new SimpleListProperty<>(FXCollections.observableArrayList());
    private final BooleanProperty argumentsLoaded = new SimpleBooleanProperty(false);

    private final ListProperty<InstructionDTO> programInstructions =
            new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<InstructionDTO> derivedInstructions =
            new SimpleListProperty<>(FXCollections.observableArrayList());

    private final ListProperty<String> programVariablesNamesAndLabels =
            new SimpleListProperty<>(FXCollections.observableArrayList());
    private final MapProperty<String, String> allSubFunction =
            new SimpleMapProperty<>(FXCollections.observableHashMap());

    private final ListProperty<ExecutionResultStatisticsDTO> executionStatistics =
            new SimpleListProperty<>(FXCollections.observableArrayList());

    private final StringProperty mainProgramName = new SimpleStringProperty("");
    private final StringProperty currentLoadedProgramName = new SimpleStringProperty("");

    // Use LocalEngineController so we can call the synchronous methods used throughout this controller
    private final @NotNull EngineController engineController;

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
    private final StringProperty currentUserName = new SimpleStringProperty("Guest User");
    private final StringProperty screenTitle = new SimpleStringProperty("S-Emulator Execution");
    private final IntegerProperty availableCredits = new SimpleIntegerProperty(0);
    private final boolean isInDebugResume = false;

    private Scene scene;

    @FXML
    private TitledPane executionActionsTitledPane;
    @FXML
    private Button rerunButton;
    @FXML
    private Button showInfoButton;

    @FXML
    private HBox executionHeader;
    @FXML
    private ExecutionHeaderController executionHeaderController;
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
    private SummaryLineController summaryLineController;
    @FXML
    private VariablesTableController allVarsTableController;
    @FXML
    private DebuggerController debugControlsController;
    @FXML
    private Button backToDashboardButton;

    private @Nullable ProgramDTO loadedProgram = null;
    private Runnable returnToDashboardCallback = null;
    private boolean inDebugSession = false;
    @FXML
    private VBox historyStats;
    private boolean isFirstDebugStep = true;
    private boolean isDebugFinished = false;

    public ExecutionController() {
        this.engineController = new HttpEngineController();
        UIUtils.setAppControllerInstance(this);
    }

    @FXML
    public void initialize() {
        if (cyclesController != null && programFunctionController != null &&
                instructionsTableController != null && derivedInstructionsTableController != null &&
                argumentsTableController != null && allVarsTableController != null &&
                debugControlsController != null && runControlsController != null &&
                summaryLineController != null) {

            programFunctionController.initComponent(
                    this::expandProgramToLevel,
                    this::setPaneMode,
                    currentExpandLevel,
                    maxExpandLevel,
                    programLoaded,
                    this::handleVariableSelection,
                    programVariablesNamesAndLabels,
                    isAnimationsOn
            );
            runControlsController.initComponent(this::RunProgram, this::prepareForTakingArguments,
                    programLoaded, argumentsLoaded);
            summaryLineController.initComponent(currentLoadedProgramName);
            cyclesController.initComponent(currentCycles);
            instructionsTableController.initializeMainInstructionTable(programInstructions,
                    derivedInstructions, isAnimationsOn);
            derivedInstructionsTableController.markAsDerivedInstructionsTable();
            derivedInstructionsTableController.setDerivedInstructionsTable(derivedInstructions, isAnimationsOn);
            argumentsTableController.initArgsTable(argumentsDTO, isAnimationsOn);
            allVarsTableController.initAllVarsTable(allVariablesDTO, isAnimationsOn);

            // Set back button callback in DebuggerController
            if (debugControlsController != null) {
                System.out.println("Back to Dashboard callback registered with DebuggerController");
                debugControlsController.initComponent(
                        this::debugStep,
                        this::debugResume,
                        this::stopDebugSession
                );

            }

            initializeExecutionHeader();
            initializeExecutionActionButtons();
            System.out.println("AppController initialized with cleaned architecture");
        } else {
            System.err.println("One or more controllers are not injected properly!");
            throw new IllegalStateException("FXML injection failed: required controllers are null.");
        }
    }

    private void initializeExecutionActionButtons() {
        if (rerunButton != null && showInfoButton != null) {
            // Rerun button: enabled when program has finished execution and we have arguments
            rerunButton.disableProperty().bind(
                    programFinished.not()
                            .or(argumentsDTO.emptyProperty())
                            .or(programLoaded.not())
            );

            // Show Info button: enabled when program has run at least once
            showInfoButton.disableProperty().bind(
                    programRanAtLeastOnce.not()
            );

            // Bind the execution actions pane expansion
            executionActionsTitledPane.expandedProperty().bind(
                    Bindings.and(programLoaded, programRanAtLeastOnce)
            );

            System.out.println("Execution action buttons initialized and bound");
        } else {
            System.err.println("Execution action buttons not injected!");
        }
    }
    private void initializeExecutionHeader() {
        if (executionHeaderController != null) {
            executionHeaderController.initComponent(
                    currentUserName,
                    screenTitle,
                    availableCredits
            );
            System.out.println("Execution header initialized with back button");
        } else {
            System.err.println("ExecutionHeaderController not injected!");
        }
    }

    public void setReturnToDashboardCallback(Runnable callback) {
        this.returnToDashboardCallback = callback;
        System.out.println("AppController: Return to Dashboard callback registered");
    }

    public void loadProgramToExecution(@NotNull String programName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(TASK_PATH));
            Parent root = loader.load();
            ProgramTaskController programTaskController = loader.getController();

            Stage loadingStage = createTaskLoadingStage("Loading Program: " + programName, root);
            UIAdapter uiAdapter = buildUIAdapter(programName, loadingStage);


            loadingStage.setOnShown(event -> programTaskController.initializeAndRunLoadTaskThread(
                    programName, engineController, uiAdapter));
            loadingStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error loading file: " + e.getMessage());
        }
    }

    private UIAdapter buildUIAdapter(@NotNull String programName, Stage loadingStage) {
        return UIAdapter.builder()
                .programLoadedDelegate(programLoaded::set)
                .argumentsEnteredDelegate(argumentsLoaded::set)
                .variablesAndLabelsNamesDelegate(programVariablesNamesAndLabels::setAll)
                .programInstructionsDelegate(programInstructions::setAll)
                .clearDerivedInstructionsDelegate(derivedInstructions::clear)
                .summaryLineDelegate(summaryLineController::updateCounts)
                .maxExpandLevelDelegate(maxExpandLevel::set)
                .currentExpandLevelDelegate(currentExpandLevel::set)
                .cyclesDelegate(currentCycles::set)
                .onFinish(program -> {
                    if (program != null) {
                        showSuccess("Program loaded successfully: " + programName);
                        loadedProgram = program;
                        mainProgramName.set(program.ProgramName());
                        currentLoadedProgramName.set(program.ProgramName());
                    }
                    loadingStage.close();
                })
                .build();
    }
    public void setScreenTitle(String title) {
        screenTitle.set(title);
    }

    public void setUserName(String name) {
        currentUserName.set(name);
    }

    public void setAvailableCredits(int credits) {
        availableCredits.set(credits);
    }

    public void handleReturnToDashboard() {
        if (returnToDashboardCallback != null) {
            if (inDebugSession) {
                stopDebugSession();
            }
            returnToDashboardCallback.run();
            System.out.println("Returning to Dashboard...");
        } else {
            System.err.println("Return to Dashboard callback not set");
        }
    }

    public void handleRerunRequest(int expandLevel, @NotNull Map<String, Integer> previousArguments) {
        if (!programLoaded.get()) {
            showError("No program loaded");
            return;
        }

        try {
            System.out.println("Preparing re-run with expand level " + expandLevel +
                    " and arguments: " + previousArguments);

            boolean wasInDebugMode = debugMode.get();
            System.out.println("Preserving debug mode state: " + wasInDebugMode);

            resetForRerun(wasInDebugMode);
            expandProgramToLevel(expandLevel);

            programArguments.clear();
            programArguments.putAll(previousArguments);

            String modeText = debugMode.get() ? "Debug" : "Regular";
            showSuccess("Re-run arguments configured successfully!\n" +
                    "Values were pre-populated from previous execution.\n" +
                    "Current mode: " + modeText + "\n" +
                    "Click 'Run Program' to execute in " + modeText.toLowerCase() + " mode.");

            debugMode.set(wasInDebugMode);

            System.out.println("Re-run preparation completed");
            System.out.println("Debug mode preserved: " + debugMode.get());

        } catch (Exception e) {
            System.err.println("Error preparing re-run: " + e.getMessage());
            showError("Error preparing re-run: " + e.getMessage());
        }
    }

    private void resetForRerun(boolean preserveDebugMode) {
        programRunning.set(false);
        programFinished.set(false);

        if (!preserveDebugMode) {
            debugMode.set(false);
        } else {
            System.out.println("Debug mode preserved for rerun: " + debugMode.get());
        }

        if (inDebugSession) {
            try {
                engineController.debugStop(systemResponse -> {
                    if (systemResponse.isSuccess()) {
                        System.out.println("Debug session stopped successfully during rerun reset");
                    } else {
                        System.err.println("Warning: Failed to stop debug session during rerun reset: "
                                + systemResponse.message());
                    }

                });
            } catch (Exception e) {
                System.err.println("Warning: Error stopping debug session during rerun reset: " + e.getMessage());
            }
            inDebugSession = false;

            if (!preserveDebugMode && debugControlsController != null) {
                debugControlsController.notifyDebugSessionEnded();
            }
        }

        previousDebugVariables.clear();
        isFirstDebugStep = true;
        allVariablesDTO.clear();
        argumentsLoaded.set(false);
        argumentsDTO.clear();
        currentCycles.set(0);

        instructionsTableController.highlightVariable(null);
        instructionsTableController.clearAllDebugHighlighting();
        derivedInstructionsTableController.highlightVariable(null);

        System.out.println("System state reset completed for rerun");
    }

    private void bindTitlePanesExpansion() {
        runControlsTitledPane.expandedProperty().bind(Bindings.and(programLoaded, programRunning.not()));
        debugControlsTitledPane.expandedProperty().bind(Bindings.and(programLoaded,
                Bindings.and(debugMode, programFinished.not())));
        variablesTitledPane.expandedProperty().bind(Bindings.and(programLoaded,
                Bindings.or(argumentsLoaded, Bindings.or(programFinished, debugMode))));
        executionActionsTitledPane.expandedProperty().bind(Bindings.and(programLoaded, programRanAtLeastOnce));
    }
    private void unbindTitlePanesExpansion() {
        runControlsTitledPane.expandedProperty().unbind();
        debugControlsTitledPane.expandedProperty().unbind();
        variablesTitledPane.expandedProperty().unbind();
        executionActionsTitledPane.expandedProperty().unbind();
    }

    public void setPaneMode(PaneMode paneMode) {
        if (paneMode == PaneMode.AUTO) {
            bindTitlePanesExpansion();
        } else {
            unbindTitlePanesExpansion();
        }
    }

    public void prepareForTakingArguments() {
        if (loadedProgram == null) {
            showError("No program loaded. Please load a program first.");
            return;
        }

        if (programArguments.isEmpty()) {
            programArguments.putAll(loadedProgram.arguments());
        }

        if (programArguments.isEmpty()) {
            argumentsLoaded.set(true);
            showSuccess("Program loaded successfully from: " + loadedProgram.ProgramName() +
                    "\nNo workVariables required.");
            return;
        }

        getArgumentsFromUser();
        argumentsLoaded.set(true);
        argumentsDTO.setAll(UIUtils.formatArgumentsToVariableDTO(programArguments));
        showSuccess("Program loaded successfully from: " + loadedProgram.ProgramName() +
                "\nArguments captured.");
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
        int expandLevel = currentExpandLevel.get();
        programRunning.set(true);
        engineController.runLoadedProgram(expandLevel, programArguments, systemResponse -> {
            if (!systemResponse.isSuccess()) {
                Platform.runLater(() -> {
                    showError("Error during execution: " + systemResponse.message());
                });
            } else {
                System.out.println("Program executed successfully in regular mode.");
                Platform.runLater(() -> {
                    FullExecutionResultDTO resultDTO = systemResponse.fullExecutionResultDTO();
                    allVariablesDTO.setAll(toVariableDTO(resultDTO.getAllVariablesSorted()));
                    currentCycles.set(resultDTO.cycleCount());
                    updatePropertiesAfterExecuting();
                    showSuccess("Program executed successfully!\n" +
                            "Cycles: " + currentCycles.get() + "\n");

                });
            }
        });

    }

    private void updatePropertiesAfterExecuting() {
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

        resetForExpansion(expandLevel);
        engineController.getProgramByExpandLevelAsync(expandLevel, SystemResponse ->
        {
            if (!SystemResponse.isSuccess()) {
                Platform.runLater(() -> showError("Error expanding program: " + SystemResponse.message()));
            } else {
                Platform.runLater(() -> {
                    loadedProgram = SystemResponse.programDTO();
                    programInstructions.setAll(loadedProgram.instructions());
                    summaryLineController.updateCounts(loadedProgram.instructions());
                    programVariablesNamesAndLabels.setAll(loadedProgram.allVariablesIncludingLabelsNames());
                    showInfo("Program expanded to level " + expandLevel);

                });
            }
        });
    }

    private void resetForExpansion(int expandLevel) {
        instructionsTableController.clearHighlighting();
        derivedInstructionsTableController.clearHighlighting();
        currentExpandLevel.set(expandLevel);
        derivedInstructions.clear();
        allVariablesDTO.clear();
        argumentsDTO.clear();
        argumentsLoaded.set(false);
    }
// Debug Methods - Continuation of AppController.java

// Debug Methods - Continuation of AppController.java

    public void clearLoadedProgram() {
        programLoaded.set(false);
        currentLoadedProgramName.set("");
        mainProgramName.set("");
        argumentsLoaded.set(false);
        maxExpandLevel.set(0);
        currentExpandLevel.set(0);
        loadedProgram = null;
        programArguments.clear();

        engineController.clearLoadedProgram();
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

    private void highlightCurrentInstruction(int instructionIndex) {
        instructionsTableController.highlightCurrentInstruction(instructionIndex);
    }
    // DEBUG METHODS

    public void stopDebugSession() {
        try {
            engineController.debugStop(systemResponse -> {
                if (systemResponse.isSuccess()) {
                    Platform.runLater(() -> {
                        showInfo("Debug session stopped.");
                        handleExecutionFinished();
                    });
                } else {
                    Platform.runLater(() -> {
                        showError("Error stopping debug session: " + systemResponse.message());
                    });
                }
            });
        } catch (Exception e) {
            System.err.println("Error stopping debug session: " + e.getMessage());
            showError("Error stopping debug session: " + e.getMessage());
        }
    }

    public void startDebugExecution() {
        int expandLevel = prepareForDebugSession();

        engineController.startDebugSession(expandLevel, programArguments, systemResponse -> {
            if (!systemResponse.isSuccess()) {
                Platform.runLater(() -> {
                    showError("Error starting debug session: " + systemResponse.message());
                    endDebugSession();
                });
            } else {
                Platform.runLater(() -> {
                    showSuccess("Debug session started successfully.");
                    inDebugSession = true;
                    highlightCurrentInstruction(0);
                });
            }
        });
    }

    private int prepareForDebugSession() {
        int expandLevel = currentExpandLevel.get();
        programRunning.set(true);
        debugMode.set(true);
        programFinished.set(false);
        currentCycles.set(0);

        previousDebugVariables.clear();
        isFirstDebugStep = true;
        System.out.println("Debug session started - change tracking reset");
        debugControlsController.prepareForDebugSession();
        return expandLevel;
    }

    public void debugStep() {
        if (!inDebugSession) {
            showError("No debug session active");
            return;
        }

        if (isDebugFinished) {
            showInfo("Execution finished. Program has completed successfully.");
            return;
        }

        engineController.debugStepOver(systemResponse -> {
            if (!systemResponse.isSuccess()) {
                Platform.runLater(() -> {
                    showError("Error during debug step: " + systemResponse.message());
                    endDebugSession();
                });
            } else {
                Platform.runLater(() -> {
                    showSuccess("Debug step completed successfully.");
                    DebugStateChangeResultDTO stateChangeResultDTO = systemResponse.debugStateChangeResultDTO();
                    int currentPC = stateChangeResultDTO.debugPC();
                    highlightCurrentInstruction(currentPC);
                    currentCycles.set(stateChangeResultDTO.debugCycles());
                    updateDebugVariableState(stateChangeResultDTO.allVarsValue());
                    isDebugFinished = stateChangeResultDTO.isFinished();
                    showInfo("Step executed. PC: " + currentPC + ", Total cycles: " + currentCycles.get());
                });
            }
        });

        if (isFirstDebugStep) {
            isFirstDebugStep = false;
        }
        if (isDebugFinished) {
            handleExecutionFinished();
        }
    }

    public void debugResume() {
        if (!inDebugSession) {
            showError("No debug session active");
            return;
        }

        if (isDebugFinished) {
            showInfo("Execution already finished.");
            return;
        }

        engineController.debugResume(systemResponse -> {
            if (!systemResponse.isSuccess()) {
                Platform.runLater(() -> {
                    showError("Error during debug resume: " + systemResponse.message());
                    endDebugSession();
                });
            } else {
                Platform.runLater(() -> {
                    showSuccess("Debug resume completed successfully.");
                    int currentPC = afterDebugAction(systemResponse);
                    isDebugFinished = true; // resume always finishes the program
                    showInfo("Program resumed. PC: " + currentPC + ", Total cycles: " + currentCycles.get());
                    handleExecutionFinished();
                });
            }
        });
    }

    private int afterDebugAction(SystemResponse systemResponse) {
        DebugStateChangeResultDTO stateChangeResultDTO = systemResponse.debugStateChangeResultDTO();
        int currentPC = stateChangeResultDTO.debugPC();
        highlightCurrentInstruction(currentPC);
        currentCycles.set(stateChangeResultDTO.debugCycles());
        updateDebugVariableState(stateChangeResultDTO.allVarsValue());
        return currentPC;
    }

    private void updateDebugVariableState(Map<String, Integer> allVarsAfterStep) {
        List<VariableDTO> allVarNoChangeDetection =
                UIUtils.toVariableDTO(allVarsAfterStep);

        List<VariableDTO> allVarWithChangeDetection = FXCollections.observableArrayList();
        allVarNoChangeDetection.stream()
                .map(var -> createVariableDTOWithChangeDetection(var.name().get(), var.value().get()))
                .forEach(allVarWithChangeDetection::add);

        allVariablesDTO.setAll(allVarWithChangeDetection);
        previousDebugVariables.putAll(allVarsAfterStep);
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
            hasChanged = value != 0 && !ProgramUtils.isArgument(name);
        }

        return new VariableDTO(
                new SimpleStringProperty(name),
                new SimpleIntegerProperty(value),
                new SimpleBooleanProperty(hasChanged)
        );
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
        debugMode.set(false);
        programRunning.set(false);
        programFinished.set(true);
        previousDebugVariables.clear();
        isFirstDebugStep = true;
        programRanAtLeastOnce.set(true);
        debugControlsController.notifyDebugSessionEnded();
        instructionsTableController.clearAllDebugHighlighting();

        System.out.println("Debug session ended - ready for new execution");
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    @FXML
    private void handleBackToDashboard() {
        if (returnToDashboardCallback != null) {
            if (inDebugSession) {
                try {
                    stopDebugSession();
                } catch (Exception e) {
                    System.err.println("Error stopping debug session: " + e.getMessage());
                }
            }
            returnToDashboardCallback.run();
            System.out.println("Returning to Dashboard from execution screen...");
        } else {
            System.err.println("Return to Dashboard callback not set");
            showError("Navigation to Dashboard is not configured");
        }
    }
    @FXML
    private void handleRerun() {
        if (!programLoaded.get()) {
            showError("No program loaded");
            return;
        }

        if (argumentsDTO.isEmpty()) {
            showError("No previous execution found. Please run the program first.");
            return;
        }

        try {
            // Extract arguments from current argumentsDTO
            Map<String, Integer> previousArguments = new HashMap<>();
            for (VariableDTO varDTO : argumentsDTO) {
                previousArguments.put(varDTO.name().get(), varDTO.value().get());
            }

            int expandLevel = currentExpandLevel.get();
            boolean wasInDebugMode = debugMode.get();

            System.out.println("Rerun button clicked - preparing rerun with level " + expandLevel +
                    ", debug mode: " + wasInDebugMode);

            // Prepare for rerun (sets up arguments and state)
            handleRerunRequest(expandLevel, previousArguments);

            // Now automatically execute the program
            // The handleRerunRequest already set argumentsLoaded to false, so we need to set it back to true
            argumentsLoaded.set(true);

            // Determine execution type based on preserved debug mode
            ProgramRunType runType = wasInDebugMode ? ProgramRunType.DEBUG : ProgramRunType.REGULAR;

            System.out.println("Executing rerun in " + runType + " mode");
            RunProgram(runType);

        } catch (Exception e) {
            System.err.println("Error handling rerun: " + e.getMessage());
            showError("Error initiating rerun: " + e.getMessage());
        }
    }

    /**
     * Handle the Show Info button click.
     * Displays execution statistics and information in a dialog.
     */
    @FXML
    private void handleShowInfo() {
        if (!programRanAtLeastOnce.get()) {
            showError("No execution history available");
            return;
        }

        try {
            // Build information summary
            StringBuilder info = new StringBuilder();
            info.append("=== Execution Information ===\n\n");
            info.append("Program: ").append(currentLoadedProgramName.get()).append("\n");
            info.append("Expand Level: ").append(currentExpandLevel.get()).append("\n");
            info.append("Max Expand Level: ").append(maxExpandLevel.get()).append("\n\n");

            info.append("=== Last Execution Results ===\n\n");
            info.append("Total Cycles: ").append(currentCycles.get()).append("\n");
            info.append("Variables Count: ").append(allVariablesDTO.size()).append("\n\n");

            if (!argumentsDTO.isEmpty()) {
                info.append("=== Arguments ===\n\n");
                for (VariableDTO arg : argumentsDTO) {
                    info.append(String.format("  %s = %d\n", arg.name().get(), arg.value().get()));
                }
                info.append("\n");
            }

            if (!allVariablesDTO.isEmpty()) {
                info.append("=== Final Variable Values ===\n\n");
                for (VariableDTO var : allVariablesDTO) {
                    info.append(String.format("  %s = %d\n", var.name().get(), var.value().get()));
                }
            }

            // Display in alert dialog
            Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
            infoAlert.setTitle("Execution Information");
            infoAlert.setHeaderText("Program Execution Details");
            infoAlert.setContentText(info.toString());
            infoAlert.getDialogPane().setPrefWidth(500);
            infoAlert.showAndWait();

            System.out.println("Show Info button clicked - displayed execution information");

        } catch (Exception e) {
            System.err.println("Error displaying execution info: " + e.getMessage());
            showError("Error displaying execution information: " + e.getMessage());
        }
    }
}
