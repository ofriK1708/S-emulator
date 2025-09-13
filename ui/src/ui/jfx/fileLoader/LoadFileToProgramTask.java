package ui.jfx.fileLoader;

import dto.engine.ProgramDTO;
import javafx.application.Platform;
import javafx.concurrent.Task;
import system.controller.controller.EngineController;
import ui.utils.UIUtils;

import java.nio.file.Path;
import java.util.List;

public class LoadFileToProgramTask extends Task<ProgramDTO> {
    private final EngineController engineController;
    private final Path filePath;
    private final UIAdapterLoadFileTask uiAdapter;

    public LoadFileToProgramTask(EngineController engineController, Path filePath, UIAdapterLoadFileTask uiAdapter) {
        this.engineController = engineController;
        this.filePath = filePath;
        this.uiAdapter = uiAdapter;
    }

    @Override
    protected ProgramDTO call() throws Exception {
        updateMessage("Loading file...");
        updateProgress(0, 4);
        int SLEEP_TIME = 750;
        engineController.LoadProgramFromFile(filePath);
        ProgramDTO program = engineController.getBasicProgram();
        List<String> allVars = UIUtils.sortAllProgramNames(
                engineController.getAllVariablesAndLabelsNames(0));
        UIUtils.sleep(SLEEP_TIME);
        updateProgress(1, 4);
        UIUtils.sleep(SLEEP_TIME);
        // update titledPanes in the UI
        Platform.runLater(() -> {
            uiAdapter.programLoadedDelegate.accept(true);
            uiAdapter.variablesEnteredDelegate.accept(false);
            uiAdapter.variablesAndLabelsNamesDelegate.accept(allVars);
            uiAdapter.programInstructionsDelegate.accept(program.instructions());
            uiAdapter.clearDerivedInstructionsDelegate.run();
            uiAdapter.summaryLineDelegate.accept(program.instructions());
        });

        updateMessage("Setting program...");
        updateProgress(2, 4);
        UIUtils.sleep(SLEEP_TIME);
        updateProgress(3, 4);
        updateMessage("Eating cookies and milk...");
        UIUtils.sleep(SLEEP_TIME);
        // update expand levels on UI
        Platform.runLater(() -> {
            uiAdapter.maxExpandLevelDelegate.accept(engineController.getMaxExpandLevel());
            uiAdapter.currentExpandLevelDelegate.accept(0);
            uiAdapter.cyclesDelegate.accept(0);
        });
        updateProgress(4, 4);
        updateMessage("File loaded successfully");
        UIUtils.sleep(SLEEP_TIME);

        return program;
    }
}
