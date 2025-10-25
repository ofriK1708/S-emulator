package ui.task.program.upload;

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
        updateProgress(1, 2);
        engineController.loadProgramFromFile(filePath);
        updateProgress(2, 2);
        updateMessage("File loaded successfully");
        return null;
    }
}
