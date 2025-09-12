package ui.jfx.fileLoader;

import javafx.application.Platform;
import javafx.concurrent.Task;
import ui.utils.UIUtils;

import java.util.function.Consumer;

public class LoadFileToProgramTask extends Task<Void> {
    private final Consumer<Integer> cyclesDelegate;
    private final Consumer<Boolean> programLoadedDelegate;
    private final Consumer<Boolean> variablesEnteredDelegate;
    private final Consumer<Integer> maxExpandLevelDelegate;
    private final Consumer<Integer> currentExpandLevelDelegate;

    public LoadFileToProgramTask(Consumer<Boolean> programLoadedDelegate, Consumer<Boolean> variablesEnteredDelegate,
                                 Consumer<Integer> maxExpandLevelDelegate, Consumer<Integer> currentExpandLevelDelegate,
                                 Consumer<Integer> cyclesDelegate) {
        this.programLoadedDelegate = programLoadedDelegate;
        this.variablesEnteredDelegate = variablesEnteredDelegate;
        this.maxExpandLevelDelegate = maxExpandLevelDelegate;
        this.currentExpandLevelDelegate = currentExpandLevelDelegate;
        this.cyclesDelegate = cyclesDelegate;
    }

    @Override
    protected Void call() throws Exception {
        updateMessage("Loading file...");
        updateProgress(0, 4);
        int SLEEP_TIME = 750;
        UIUtils.sleep(SLEEP_TIME);
        updateProgress(1, 4);
        UIUtils.sleep(SLEEP_TIME);
        // update titledPanes in the UI
        Platform.runLater(() -> {
            programLoadedDelegate.accept(true);
            variablesEnteredDelegate.accept(false);
        });

        updateMessage("Setting program...");
        updateProgress(2, 4);
        UIUtils.sleep(SLEEP_TIME);
        updateProgress(3, 4);
        updateMessage("Eating cookies and milk...");
        UIUtils.sleep(SLEEP_TIME);
        // update expand levels on UI
        Platform.runLater(() -> {
            currentExpandLevelDelegate.accept(0);
        });
        updateProgress(4, 4);
        updateMessage("File loaded successfully");
        UIUtils.sleep(SLEEP_TIME);

        return null;
    }
}
