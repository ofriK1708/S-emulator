package ui.web.jfx.fileLoader;

import javafx.concurrent.Task;
import system.controller.EngineController;

import java.nio.file.Path;

public class UploadFileToSystemTask extends Task<Void> {
    private final EngineController engineController;
    private final Path filePath;

    public UploadFileToSystemTask(EngineController engineController, Path filePath) {
        this.engineController = engineController;
        this.filePath = filePath;
    }

    @Override
    protected Void call() throws Exception {
        updateMessage("Loading file...");
        updateProgress(0, 2);
        engineController.LoadProgramFromFile(filePath);
        updateProgress(1, 2);
        // todo - here get all the programs and function metadata loaded
        updateProgress(4, 4);
        updateMessage("File loaded successfully");
        return null;
    }
}
