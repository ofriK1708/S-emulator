package src.ui.jfx.debugger;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.jetbrains.annotations.NotNull;

public class DebuggerController {

    // Callback interfaces for debug actions only
    private Runnable debugStepCallback;
    private Runnable debugStepBackwardCallback;
    private Runnable debugResumeCallback;
    private Runnable stopDebugSessionCallback;

    // Internal state properties
    private final BooleanProperty debugSessionActive = new SimpleBooleanProperty(false);
    private final BooleanProperty atFirstStep = new SimpleBooleanProperty(true);

    // NEW: Track if execution is finished to disable all controls
    private final BooleanProperty executionFinished = new SimpleBooleanProperty(false);

    // FXML-injected controls - only debug control buttons
    @FXML private Button resume;
    @FXML private Button stepBackward;
    @FXML private Button stepOver;
    @FXML private Button stop;

    @FXML
    private void initialize() {
        System.out.println("DebuggerController FXML initialize() called");
        // Do nothing here - all setup happens in initComponent()
    }

    /**
     * Initialize the debugger controller - only handles debug session controls
     * Does NOT handle program execution - only debug operations
     */
    public void initComponent(
            @NotNull Runnable debugStepCallback,
            @NotNull Runnable debugStepBackwardCallback,
            @NotNull Runnable debugResumeCallback,
            @NotNull Runnable stopDebugSessionCallback) {

        // Store debug action callbacks
        this.debugStepCallback = debugStepCallback;
        this.debugStepBackwardCallback = debugStepBackwardCallback;
        this.debugResumeCallback = debugResumeCallback;
        this.stopDebugSessionCallback = stopDebugSessionCallback;

        // ENHANCED: Debug controls disabled when session inactive OR execution finished
        BooleanProperty controlsDisabled = new SimpleBooleanProperty();
        controlsDisabled.bind(debugSessionActive.not().or(executionFinished));

        stop.disableProperty().bind(controlsDisabled);
        resume.disableProperty().bind(controlsDisabled);

        // Step Over: disabled when controls disabled OR execution finished
        stepOver.disableProperty().bind(controlsDisabled);

        // Step Backward: additionally disabled at first step
        stepBackward.disableProperty().bind(controlsDisabled.or(atFirstStep));

        System.out.println("DebuggerController bindings initialized successfully");
    }

    // FXML Event Handlers - only debug operations
    @FXML
    private void handleStepOver() {
        if (debugStepCallback != null && !executionFinished.get()) {
            debugStepCallback.run();
            // After first step, enable step backward
            atFirstStep.set(false);
        }
    }

    @FXML
    private void handleStepBackward() {
        if (debugStepBackwardCallback != null && !executionFinished.get()) {
            debugStepBackwardCallback.run();
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
        atFirstStep.set(true); // Reset to first step when session starts
        executionFinished.set(false); // Reset execution finished state
        System.out.println("Debug session started - debug controls enabled, at first step");
    }

    public void notifyDebugSessionEnded() {
        debugSessionActive.set(false);
        atFirstStep.set(true); // Reset for next session
        executionFinished.set(false); // Reset for next session
        System.out.println("Debug session ended - debug controls disabled");
    }

    // NEW: Method to notify when execution reaches final step
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

    public boolean isAtFirstStep() {
        return atFirstStep.get();
    }

    public BooleanProperty atFirstStepProperty() {
        return atFirstStep;
    }

    public boolean isExecutionFinished() {
        return executionFinished.get();
    }

    public BooleanProperty executionFinishedProperty() {
        return executionFinished;
    }
}