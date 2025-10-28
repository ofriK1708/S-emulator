package ui.task.program;

import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import org.jetbrains.annotations.NotNull;
import system.controller.EngineController;
import ui.task.program.load.LoadProgramToExecutionTask;
import ui.task.program.load.UIAdapter;
import ui.task.program.upload.UploadFileToSystemTask;
import ui.utils.UIUtils;

import java.nio.file.Path;
import java.util.function.Consumer;

public class ProgramTaskController {

    @FXML
    public Label taskTitle;
    @FXML
    private Label taskOn;
    @FXML
    private Label taskMessage;
    @FXML
    private Label percentLabel;
    @FXML
    private ProgressBar progressBar;

    public void initializeAndRunUploadTaskThread(@NotNull Path filePath, EngineController engineController,
                                                 Consumer<Boolean> onFinish) {
        taskTitle.setText("Uploading Program File");
        initTaskMeasurements(filePath.toString());
        UploadFileToSystemTask uploadFileToSystemTask = new UploadFileToSystemTask(
                engineController, filePath);
        bindUploadTaskToUIComponents(uploadFileToSystemTask, onFinish);
        Thread thread = new Thread(uploadFileToSystemTask, "FileLoaderTaskThread");
        thread.start();
    }

    public void initializeAndRunLoadTaskThread(@NotNull String programName,
                                               @NotNull EngineController engineController,
                                               @NotNull UIAdapter uiAdapter) {
        taskTitle.setText("Loading Program");
        initTaskMeasurements(programName);
        LoadProgramToExecutionTask loadProgramToExecutionTask = new LoadProgramToExecutionTask(
                engineController, uiAdapter, programName);
        bindLoadProgramToExecutionToUIComponents(loadProgramToExecutionTask, uiAdapter);
        Thread thread = new Thread(loadProgramToExecutionTask, "ProgramLoadTaskThread");
        thread.start();
    }

    private void initTaskMeasurements(@NotNull String loadingName) {
        taskOn.setText(loadingName);
        percentLabel.setText("0%");
        progressBar.setProgress(0);
    }

    private void bindUploadTaskToUIComponents(@NotNull UploadFileToSystemTask task, Consumer<Boolean> onFinish) {
        bindUIComponents(task);
        task.setOnSucceeded(e -> onFinish.accept(true));
        task.setOnCancelled(e -> {
            UIUtils.showInfo("Program file upload cancelled.");
            onFinish.accept(false);
        });
        task.setOnFailed(e -> {
            UIUtils.showError(task.getException().getMessage());
            onFinish.accept(false);
        });
    }

    private void bindLoadProgramToExecutionToUIComponents(@NotNull LoadProgramToExecutionTask task,
                                                          @NotNull UIAdapter uiAdapter) {
        bindUIComponents(task);
        task.setOnSucceeded(e -> uiAdapter.onFinish().accept(task.getValue()));
        task.setOnCancelled(e -> uiAdapter.onFinish().accept(null));
        task.setOnFailed(e -> {
            UIUtils.showError(task.getException().getMessage());
            uiAdapter.onFinish().accept(null);
        });
    }

    private void bindUIComponents(@NotNull Task<?> task) {
        progressBar.progressProperty().bind(task.progressProperty());
        taskMessage.textProperty().bind(task.messageProperty());
        percentLabel.textProperty().bind(Bindings.concat(
                Bindings.format(
                        "%.0f",
                        Bindings.multiply(
                                task.progressProperty(),
                                100)),
                " %"));
    }

}
