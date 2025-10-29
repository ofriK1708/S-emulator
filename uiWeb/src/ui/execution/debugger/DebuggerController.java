package ui.execution.debugger;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.jetbrains.annotations.NotNull;

public class DebuggerController {

    @FXML
    private Button resume;

    @FXML
    private Button stepOver;

    @FXML
    public Button stepBackward;

    @FXML
    private Button stop;


    // Internal state properties
    private BooleanProperty debugSessionActive = new SimpleBooleanProperty(false);
    // FXML-injected controls
    private BooleanProperty atFirstStep = new SimpleBooleanProperty(true);
    private BooleanProperty executionFinished = new SimpleBooleanProperty(false);

    // Callback interfaces for debug actions
    private @NotNull Runnable debugStepCallback = () -> {
        System.out.println("Debug step callback not initialized");
    };
    private @NotNull Runnable debugResumeCallback = () -> {
        System.out.println("Debug resume callback not initialized");
    };
    private @NotNull Runnable stopDebugSessionCallback = () -> {
        System.out.println("Stop debug session callback not initialized");
    };
    private @NotNull Runnable debugStepBackwardCallback = () -> {
        System.out.println("Debug step backward callback not initialized");
    };


    @FXML
    private void initialize() {
        System.out.println("DebuggerController FXML initialize() called");
    }

    /**
     * Initialize the debugger controller - handles debug session controls only
     * Note: Back to Dashboard button has been moved to main execution layout
     */
    public void initComponent(
            @NotNull Runnable debugStepCallback,
            @NotNull Runnable debugStepBackwardCallback,
            @NotNull Runnable debugResumeCallback,
            @NotNull Runnable stopDebugSessionCallback,
            @NotNull BooleanProperty executionFinishedProperty,
            @NotNull BooleanProperty atFirstStepProperty,
            @NotNull BooleanProperty debugSessionActiveProperty) {

        this.debugStepCallback = debugStepCallback;
        this.debugResumeCallback = debugResumeCallback;
        this.debugStepBackwardCallback = debugStepBackwardCallback;
        this.stopDebugSessionCallback = stopDebugSessionCallback;
        this.executionFinished = executionFinishedProperty;
        this.atFirstStep = atFirstStepProperty;
        this.debugSessionActive = debugSessionActiveProperty;

        // Debug controls disabled when session inactive OR execution finished
        BooleanProperty controlsDisabled = new SimpleBooleanProperty();
        controlsDisabled.bind(debugSessionActive.not().or(executionFinished));

        stop.disableProperty().bind(controlsDisabled);
        resume.disableProperty().bind(controlsDisabled);
        stepOver.disableProperty().bind(controlsDisabled);
        stepBackward.disableProperty().bind(controlsDisabled);

        System.out.println("DebuggerController bindings initialized successfully");
    }

    // FXML Event Handlers
    @FXML
    private void handleStepOver() {
        if (!executionFinished.get()) {
            debugStepCallback.run();
            atFirstStep.set(false);
        }
    }

    public void handleStepBackward(ActionEvent actionEvent) {
        if (!executionFinished.get()) {
            debugStepBackwardCallback.run();
        }
    }

    @FXML
    private void handleResume() {
        if (!executionFinished.get()) {
            debugResumeCallback.run();
        }
    }

    @FXML
    private void handleStop() {
        stopDebugSessionCallback.run();
    }

    // Public methods for AppController to manage debug session state
    public void prepareForDebugSession() {
        debugSessionActive.set(true);
        atFirstStep.set(true);
        executionFinished.set(false);
        System.out.println("Debug session started - debug controls enabled");
    }

    public void notifyDebugSessionEnded() {
        debugSessionActive.set(false);
        atFirstStep.set(true);
        executionFinished.set(false);
        System.out.println("Debug session ended - debug controls disabled");
    }
}