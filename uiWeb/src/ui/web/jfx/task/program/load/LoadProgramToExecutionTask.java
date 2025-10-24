package ui.web.jfx.task.program.load;

import dto.engine.ProgramDTO;
import javafx.concurrent.Task;
import org.jetbrains.annotations.NotNull;
import system.controller.EngineController;
import ui.web.utils.UIUtils;

public class LoadProgramToExecutionTask extends Task<ProgramDTO> {
    private final EngineController engineController;
    private final UIAdapter uiAdapter;
    private final String programName;

    public LoadProgramToExecutionTask(@NotNull EngineController engineController,
                                      @NotNull UIAdapter uiAdapter,
                                      @NotNull String ProgramName) {
        this.engineController = engineController;
        this.uiAdapter = uiAdapter;
        this.programName = ProgramName;
    }

    @Override
    protected ProgramDTO call() throws Exception {
        final int SLEEP_TIME = 500;
        updateMessage("Loading file...");
        updateProgress(0, 4);
        ProgramDTO program = engineController.loadProgram(programName);
        updateProgress(1, 4);
        // update titledPanes in the UI
        uiAdapter.variablesAndInstructionsHandler(program);
        updateMessage("Setting program...");
        updateProgress(2, 4);
        UIUtils.sleep(SLEEP_TIME);
        updateProgress(3, 4);
        updateMessage("Eating cookies and milk...");
        // update expand levels on UI
        uiAdapter.expandLevelsAndCyclesHandler(program.maxExpandLevel());
        updateProgress(4, 4);
        updateMessage("File loaded successfully");
        UIUtils.sleep(SLEEP_TIME);

        return program;
    }
}