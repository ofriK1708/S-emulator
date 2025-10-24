package ui.web.jfx.task.program;

import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import org.jetbrains.annotations.NotNull;
import system.controller.EngineController;
import ui.web.jfx.task.program.load.LoadProgramToExecutionTask;
import ui.web.jfx.task.program.load.UIAdapter;
import ui.web.jfx.task.program.upload.UploadFileToSystemTask;
import ui.web.utils.UIUtils;

import java.nio.file.Path;

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

    public void initializeAndRunUploadTaskThread(@NotNull Path filePath, EngineController engineController) {
        taskTitle.setText("Uploading Program File");
        initTaskMeasurements(filePath);
        UploadFileToSystemTask uploadFileToSystemTask = new UploadFileToSystemTask(
                engineController, filePath);
        bindUploadTaskToUIComponents(uploadFileToSystemTask);
        Thread thread = new Thread(uploadFileToSystemTask, "FileLoaderTaskThread");
        thread.start();
    }

    public void initializeAndRunLoadTaskThread(@NotNull String programName,
                                               @NotNull EngineController engineController,
                                               @NotNull UIAdapter uiAdapter) {
        taskTitle.setText("Loading Program");
        initTaskMeasurements(Path.of(programName));
        LoadProgramToExecutionTask loadProgramToExecutionTask = new LoadProgramToExecutionTask(
                engineController, uiAdapter, programName);
        bindLoadProgramToExecutionToUIComponents(loadProgramToExecutionTask, uiAdapter);
        Thread thread = new Thread(loadProgramToExecutionTask, "ProgramLoadTaskThread");
        thread.start();
    }

    private void initTaskMeasurements(@NotNull Path filePath) {
        taskOn.setText(filePath.toString());
        percentLabel.setText("0%");
        progressBar.setProgress(0);
    }

    private void bindUploadTaskToUIComponents(@NotNull UploadFileToSystemTask task) {
        bindUIComponents(task);
    }

    private void bindLoadProgramToExecutionToUIComponents(@NotNull LoadProgramToExecutionTask task,
                                                          @NotNull UIAdapter uiAdapter) {
        bindUIComponents(task);
        task.setOnSucceeded(e -> uiAdapter.onFinish().accept(task.getValue()));
        task.setOnCancelled(e -> uiAdapter.onFinish().accept(null));
        task.setOnFailed(e -> {
            UIUtils.showError("Failed to load the program: " + task.getException().getMessage());
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
