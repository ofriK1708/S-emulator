package ui.jfx.fileLoader;

import dto.engine.ProgramDTO;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import org.jetbrains.annotations.NotNull;
import system.controller.EngineController;
import ui.utils.UIUtils;

import java.nio.file.Path;
import java.util.function.Consumer;

public class FileLoaderController {

    @FXML
    private Label fileNameLabel;

    @FXML
    private Label taskMessage;
    @FXML
    private Label percentLabel;

    @FXML
    private ProgressBar progressBar;

    public void initializeAndRunFileLoaderTaskThread(@NotNull Path filePath, EngineController engineController,
                                                     @NotNull UIAdapterLoadFileTask uiAdapter) {
        fileNameLabel.setText(filePath.toString());
        percentLabel.setText("0%");
        progressBar.setProgress(0);
        LoadFileToProgramTask loadFileToProgramTask = new LoadFileToProgramTask(
                engineController, filePath, uiAdapter
        );
        bindTaskToUIComponents(loadFileToProgramTask, uiAdapter.onFinish());
        Thread thread = new Thread(loadFileToProgramTask, "FileLoaderTaskThread");
        thread.start();
    }

    private void bindTaskToUIComponents(@NotNull LoadFileToProgramTask task, @NotNull Consumer<ProgramDTO> onFinish) {
        progressBar.progressProperty().bind(task.progressProperty());
        taskMessage.textProperty().bind(task.messageProperty());
        percentLabel.textProperty().bind(Bindings.concat(
                Bindings.format(
                        "%.0f",
                        Bindings.multiply(
                                task.progressProperty(),
                                100)),
                " %"));

        task.setOnSucceeded(e -> onFinish.accept(task.getValue()));
        task.setOnCancelled(e -> onFinish.accept(task.getValue()));
        task.setOnFailed(e -> {
            UIUtils.showError("Failed to load the file: " + task.getException().getMessage())
            ;
            onFinish.accept(null);
        });
    }

}
