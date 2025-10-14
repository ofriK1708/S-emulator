package ui.web.jfx.debugger;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.jetbrains.annotations.NotNull;

public class DebuggerController {

    // Internal state properties
    private final BooleanProperty debugSessionActive = new SimpleBooleanProperty(false);
    private final BooleanProperty atFirstStep = new SimpleBooleanProperty(true);
    private final BooleanProperty executionFinished = new SimpleBooleanProperty(false);

    // Callback interfaces for debug actions
    private Runnable debugStepCallback;
    private Runnable debugResumeCallback;
    private Runnable stopDebugSessionCallback;

    // FXML-injected controls
    @FXML
    private Button resume;
    @FXML
    private Button stepOver;
    @FXML
    private Button stop;

    @FXML
    private void initialize() {
        System.out.println("DebuggerController FXML initialize() called");
    }

    /**
     * Initialize the debugger controller - only handles debug session controls
     */
    public void initComponent(
            @NotNull Runnable debugStepCallback,
            @NotNull Runnable debugResumeCallback,
            @NotNull Runnable stopDebugSessionCallback) {

        this.debugStepCallback = debugStepCallback;
        this.debugResumeCallback = debugResumeCallback;
        this.stopDebugSessionCallback = stopDebugSessionCallback;

        // Debug controls disabled when session inactive OR execution finished
        BooleanProperty controlsDisabled = new SimpleBooleanProperty();
        controlsDisabled.bind(debugSessionActive.not().or(executionFinished));

        stop.disableProperty().bind(controlsDisabled);
        resume.disableProperty().bind(controlsDisabled);
        stepOver.disableProperty().bind(controlsDisabled);

        System.out.println("DebuggerController bindings initialized successfully");
    }

    // FXML Event Handlers
    @FXML
    private void handleStepOver() {
        if (debugStepCallback != null && !executionFinished.get()) {
            debugStepCallback.run();
            atFirstStep.set(false);
        }
    }

    @FXML
    private void handleResume() {
        if (debugResumeCallback != null && !executionFinished.get()) {
            debugResumeCallback.run();
        }
    }

    @FXML
    private void handleStop() {
        if (stopDebugSessionCallback != null) {
            stopDebugSessionCallback.run();
        }
    }

    // Public methods for AppController to manage debug session state
    public void notifyDebugSessionStarted() {
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

    public void notifyExecutionFinished() {
        executionFinished.set(true);
        System.out.println("Execution finished - all debug controls disabled");
    }

    // Getters
    public boolean isDebugSessionActive() {
        return debugSessionActive.get();
    }

    public BooleanProperty debugSessionActiveProperty() {
        return debugSessionActive;
    }

    public boolean isExecutionFinished() {
        return executionFinished.get();
    }

    public BooleanProperty executionFinishedProperty() {
        return executionFinished;
    }
}