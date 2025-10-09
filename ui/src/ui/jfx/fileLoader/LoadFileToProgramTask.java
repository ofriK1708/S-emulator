package ui.jfx.fileLoader;

import dto.engine.ProgramDTO;
import javafx.concurrent.Task;
import system.controller.LocalEngineController;
import ui.utils.UIUtils;

import java.nio.file.Path;
import java.util.List;

public class LoadFileToProgramTask extends Task<ProgramDTO> {
    private final LocalEngineController localEngineController;
    private final Path filePath;
    private final UIAdapterLoadFileTask uiAdapter;

    public LoadFileToProgramTask(LocalEngineController localEngineController, Path filePath,
                                 UIAdapterLoadFileTask uiAdapter) {
        this.localEngineController = localEngineController;
        this.filePath = filePath;
        this.uiAdapter = uiAdapter;
    }

    @Override
    protected ProgramDTO call() throws Exception {
        updateMessage("Loading file...");
        updateProgress(0, 4);
        final int SLEEP_TIME = 300;
        localEngineController.LoadProgramFromFile(filePath);
        ProgramDTO program = localEngineController.getBasicProgram();
        List<String> allVars = UIUtils.sortAllProgramNames(
                localEngineController.getAllVariablesAndLabelsNames(0, true));
        UIUtils.sleep(SLEEP_TIME);
        updateProgress(1, 4);
        UIUtils.sleep(SLEEP_TIME);
        // update titledPanes in the UI
        uiAdapter.variablesAndInstructionsHandler(program, allVars);
        updateMessage("Setting program...");
        updateProgress(2, 4);
        UIUtils.sleep(SLEEP_TIME);
        updateProgress(3, 4);
        updateMessage("Eating cookies and milk...");
        UIUtils.sleep(SLEEP_TIME);
        // update expand levels on UI
        uiAdapter.expandLevelsAndCyclesHandler(localEngineController.getMaxExpandLevel());
        updateProgress(4, 4);
        updateMessage("File loaded successfully");
        UIUtils.sleep(SLEEP_TIME);

        return program;
    }
}
