package ui.execution;

import dto.engine.*;
import dto.server.SystemResponse;
import dto.ui.VariableDTO;
import engine.utils.ArchitectureType;
import engine.utils.ProgramUtils;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
    private final BooleanProperty isProgramLoaded = new SimpleBooleanProperty(false);
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

    private final StringProperty mainProgramName = new SimpleStringProperty("");
    private final StringProperty currentLoadedProgramName = new SimpleStringProperty("");

    // Use LocalEngineController so we can call the synchronous methods used throughout this controller
    private final @NotNull EngineController engineController;

    private final Map<String, Integer> programArguments = new HashMap<>();
    private final BooleanProperty isAnimationsOn = new SimpleBooleanProperty(true);
    private final BooleanProperty isInDebugMode = new SimpleBooleanProperty(false);
    private final BooleanProperty didProgramRanAtLeastOnce = new SimpleBooleanProperty(false);
    private final BooleanProperty isProgramRunning = new SimpleBooleanProperty(false);
    private final BooleanProperty isProgramFinished = new SimpleBooleanProperty(false);
    private Stage stage;
    private final IntegerProperty maxExpandLevel = new SimpleIntegerProperty(0);
    private final IntegerProperty currentExpandLevel = new SimpleIntegerProperty(0);
    private final IntegerProperty currentCycles = new SimpleIntegerProperty(0);
    private final StringProperty currentUserName = new SimpleStringProperty("Guest User");
    private final StringProperty screenTitle = new SimpleStringProperty("S-Emulator Execution");
    private final IntegerProperty availableCredits = new SimpleIntegerProperty(0);


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
                    isProgramLoaded,
                    this::handleVariableSelection,
                    programVariablesNamesAndLabels,
                    isAnimationsOn
            );
            runControlsController.initComponent(this::RunProgram,
                    this::prepareForTakingArguments,
                    isProgramLoaded,
                    argumentsLoaded
            );
            cyclesController.initComponent(currentCycles);

            instructionsTableController.initializeMainInstructionTable(programInstructions,
                    derivedInstructions,
                    isAnimationsOn
            );
            derivedInstructionsTableController.markAsDerivedInstructionsTable();
            derivedInstructionsTableController.setDerivedInstructionsTable(
                    derivedInstructions,
                    isAnimationsOn
            );
            argumentsTableController.initArgsTable(argumentsDTO,
                    isAnimationsOn
            );
            allVarsTableController.initAllVarsTable(allVariablesDTO, isAnimationsOn
            );

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
                    isProgramFinished.not()
                            .or(argumentsDTO.emptyProperty())
                            .or(isProgramLoaded.not())
            );

            // Show Info button: enabled when program has run at least once
            showInfoButton.disableProperty().bind(
                    didProgramRanAtLeastOnce.not()
            );

            // Bind the execution actions pane expansion
            executionActionsTitledPane.expandedProperty().bind(
                    Bindings.and(isProgramLoaded, didProgramRanAtLeastOnce)
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

    public void setStage(Stage stage) {
        this.stage = stage;
        this.stage.setOnCloseRequest(event -> {
            event.consume(); // Prevent the window from closing immediately
            handleBackToDashboard();
        });
    }

    public void loadProgramToExecution(@NotNull String programName) {
        loadProgramToExecution(programName, null);
    }

    /**
     * Load a program into the execution environment asynchronously.
     *
     * @param programName                  Name of the program to load
     * @param executionResultStatisticsDTO Optional statistics from previous executions otherwise null
     */
    public void loadProgramToExecution(@NotNull String programName,
                                       @Nullable ExecutionResultStatisticsDTO executionResultStatisticsDTO) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(TASK_PATH));
            Parent root = loader.load();
            ProgramTaskController programTaskController = loader.getController();

            Stage loadingStage = createTaskLoadingStage("Loading Program: " + programName, root);
            UIAdapter uiAdapter = buildUIAdapter(programName, loadingStage, executionResultStatisticsDTO);


            loadingStage.setOnShown(event -> programTaskController.initializeAndRunLoadTaskThread(
                    programName, engineController, uiAdapter));
            loadingStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error loading file: " + e.getMessage());
        }
    }

    /**
     * Build the UIAdapter for program loading callbacks.
     *
     * @param programName                  the name of the program being loaded
     * @param loadingStage                 the stage showing the loading progress
     * @param executionResultStatisticsDTO optional previous execution statistics
     * @return the constructed UIAdapter
     */
    private UIAdapter buildUIAdapter(@NotNull String programName, Stage loadingStage,
                                     @Nullable ExecutionResultStatisticsDTO executionResultStatisticsDTO) {
        return UIAdapter.builder()
                .programLoadedDelegate(isProgramLoaded::set)
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
                        isProgramLoaded.set(true);
                        if (executionResultStatisticsDTO != null) {
                            handleRerunRequest(executionResultStatisticsDTO);
                        }
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

    @FXML
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

    public void handleRerunRequest(@NotNull ExecutionResultStatisticsDTO executionResultStatisticsDTO) {
        if (!isProgramLoaded.get()) {
            showError("No program loaded");
            return;
        }
        if (loadedProgram == null) {
            showError("Loaded program is not available");
            return;
        }
        int expandLevel = executionResultStatisticsDTO.expandLevel();
        Map<String, Integer> previousArguments = executionResultStatisticsDTO.arguments();
        ArchitectureType architectureType = executionResultStatisticsDTO.architectureType();

        System.out.println("Preparing re-run with expand level " + expandLevel +
                " and arguments: " + previousArguments);

        resetForRerun();
        expandProgramToLevel(expandLevel);

        programArguments.clear();
        programArguments.putAll(previousArguments);
        argumentsDTO.setAll(formatArgumentsToVariableDTO(previousArguments));
        argumentsLoaded.set(true);

        runControlsController.setArchitectureType(architectureType);
        showSuccess("arguments loaded: " + previousArguments + "\nArchitecture: " + architectureType +
                "\nReady to re-run program: " + loadedProgram.ProgramName());
    }

    private void resetForRerun() {
        isProgramRunning.set(false);
        isProgramFinished.set(false);

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
        runControlsTitledPane.expandedProperty().bind(Bindings.and(isProgramLoaded, isProgramRunning.not()));
        debugControlsTitledPane.expandedProperty().bind(Bindings.and(isProgramLoaded,
                Bindings.and(isInDebugMode, isProgramFinished.not())));
        variablesTitledPane.expandedProperty().bind(Bindings.and(isProgramLoaded,
                Bindings.or(argumentsLoaded, Bindings.or(isProgramFinished, isInDebugMode))));
        executionActionsTitledPane.expandedProperty().bind(Bindings.and(isProgramLoaded, didProgramRanAtLeastOnce));
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
            showInfo("No arguments required for this program.");
            return;
        }

        getArgumentsFromUser();
        argumentsLoaded.set(true);
        argumentsDTO.setAll(UIUtils.formatArgumentsToVariableDTO(programArguments));
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

    public void RunProgram(@NotNull ProgramRunType programRunType, @NotNull ArchitectureType architectureType) {
        if (!isProgramLoaded.get()) {
            showError("No program loaded");
            return;
        }
        if (!argumentsLoaded.get()) {
            showError("Arguments not entered");
            return;
        }

        switch (programRunType) {
            case REGULAR -> startRegularExecution(architectureType);
            case DEBUG -> startDebugExecution(architectureType);
            default -> showError("Unknown run type selected.");
        }
        argumentsLoaded.set(false);
    }

    public void startRegularExecution(ArchitectureType architectureType) {
        int expandLevel = currentExpandLevel.get();
        isProgramRunning.set(true);
        engineController.runLoadedProgram(expandLevel, programArguments, architectureType, systemResponse -> {
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
                });
            }
        });

    }

    private void updatePropertiesAfterExecuting() {
        isProgramRunning.set(false);
        isProgramFinished.set(true);
        didProgramRanAtLeastOnce.set(true);
    }

    public void expandProgramToLevel(int expandLevel) {
        if (!isProgramLoaded.get()) {
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

    public void startDebugExecution(ArchitectureType architectureType) {
        int expandLevel = prepareForDebugSession();

        engineController.startDebugSession(expandLevel, programArguments, architectureType, systemResponse -> {
            if (!systemResponse.isSuccess()) {
                Platform.runLater(() -> {
                    showError("Error starting debug session: " + systemResponse.message());
                    endDebugSession();
                });
            } else {
                Platform.runLater(() -> {
                    inDebugSession = true;
                    highlightCurrentInstruction(0);
                });
            }
        });
    }

    private int prepareForDebugSession() {
        int expandLevel = currentExpandLevel.get();
        isProgramRunning.set(true);
        isInDebugMode.set(true);
        isProgramFinished.set(false);
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
                    afterDebugAction(systemResponse);
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
                    isDebugFinished = true; // resume always finishes the program
                    handleExecutionFinished();
                });
            }
        });
    }

    private void afterDebugAction(SystemResponse systemResponse) {
        DebugStateChangeResultDTO stateChangeResultDTO = systemResponse.debugStateChangeResultDTO();
        int currentPC = stateChangeResultDTO.debugPC();
        highlightCurrentInstruction(currentPC);
        currentCycles.set(stateChangeResultDTO.debugCycles());
        updateDebugVariableState(stateChangeResultDTO.allVarsValue());
        isDebugFinished = stateChangeResultDTO.isFinished();
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
        } catch (Exception e) {
            System.err.println("Error handling execution completion: " + e.getMessage());
            showError("Error completing execution: " + e.getMessage());
        }
    }

    private void endDebugSession() {
        inDebugSession = false;
        isInDebugMode.set(false);
        isProgramRunning.set(false);
        isProgramFinished.set(true);
        previousDebugVariables.clear();
        isFirstDebugStep = true;
        didProgramRanAtLeastOnce.set(true);
        debugControlsController.notifyDebugSessionEnded();
        instructionsTableController.clearAllDebugHighlighting();

        System.out.println("Debug session ended - ready for new execution");
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
            if (stage != null) {
                stage.close();
            }
            returnToDashboardCallback.run();
            System.out.println("Returning to Dashboard from execution screen...");
        } else {
            System.err.println("Return to Dashboard callback not set");
            showError("Navigation to Dashboard is not configured");
        }
    }
}