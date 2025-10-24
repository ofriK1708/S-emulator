package ui.web.jfx.fileLoader;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import org.jetbrains.annotations.NotNull;
import system.controller.EngineController;

import java.nio.file.Path;

public class FileLoaderController {

    @FXML
    private Label fileNameLabel;

    @FXML
    private Label taskMessage;
    @FXML
    private Label percentLabel;

    @FXML
    private ProgressBar progressBar;

    public void initializeAndRunFileLoaderTaskThread(@NotNull Path filePath, EngineController engineController) {
        fileNameLabel.setText(filePath.toString());
        percentLabel.setText("0%");
        progressBar.setProgress(0);
        UploadFileToSystemTask uploadFileToSystemTask = new UploadFileToSystemTask(
                engineController, filePath);
        bindTaskToUIComponents(uploadFileToSystemTask);
        Thread thread = new Thread(uploadFileToSystemTask, "FileLoaderTaskThread");
        thread.start();
    }

    private void bindTaskToUIComponents(@NotNull UploadFileToSystemTask task) {
        progressBar.progressProperty().bind(task.progressProperty());
        taskMessage.textProperty().bind(task.messageProperty());
        percentLabel.textProperty().bind(Bindings.concat(
                Bindings.format(
                        "%.0f",
                        Bindings.multiply(
                                task.progressProperty(),
                                100)),
                " %"));

//        task.setOnSucceeded(e -> onFinish.accept());
//        task.setOnCancelled(e -> onFinish.accept(task.getValue()));
//        task.setOnFailed(e -> {
//            UIUtils.showError("Failed to load the file: " + task.getException().getMessage())
//            ;
//            onFinish.accept(null);
//        });
    }

}
