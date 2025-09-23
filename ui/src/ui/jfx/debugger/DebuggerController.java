package ui.jfx.debugger;

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

        // All debug controls enabled only when debug session is active
        stop.disableProperty().bind(debugSessionActive.not());
        resume.disableProperty().bind(debugSessionActive.not());
        stepOver.disableProperty().bind(debugSessionActive.not());
        stepBackward.disableProperty().bind(debugSessionActive.not());

        System.out.println("DebuggerController bindings initialized successfully");
    }

    // FXML Event Handlers - only debug operations
    @FXML
    private void handleStepOver() {
        if (debugStepCallback != null) {
            debugStepCallback.run();
        }
    }

    @FXML
    private void handleStepBackward() {
        if (debugStepBackwardCallback != null) {
            debugStepBackwardCallback.run();
        }
    }

    @FXML
    private void handleResume() {
        if (debugResumeCallback != null) {
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
        System.out.println("Debug session started - debug controls enabled");
    }

    public void notifyDebugSessionEnded() {
        debugSessionActive.set(false);
        System.out.println("Debug session ended - debug controls disabled");
    }

    // Getters
    public boolean isDebugSessionActive() {
        return debugSessionActive.get();
    }

    public BooleanProperty debugSessionActiveProperty() {
        return debugSessionActive;
    }
}