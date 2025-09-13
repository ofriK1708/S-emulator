package ui.jfx.fileLoader;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import system.controller.controller.EngineController;

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

    public void initializeAndRunFileLoaderTaskThread(Path filePath, EngineController engineController,
                                                     Consumer<Boolean> programLoadedDelegate, Consumer<Boolean> VariablesEnteredDelegate,
                                                     Consumer<Integer> maxExpandLevelDelegate, Consumer<Integer> currentExpandLevelDelegate,
                                                     Consumer<Integer> cyclesDelegate, Runnable onFinish) {
        fileNameLabel.setText(filePath.toString());
        percentLabel.setText("0%");
        progressBar.setProgress(0);
        LoadFileToProgramTask loadFileToProgramTask = new LoadFileToProgramTask(
                engineController, filePath, programLoadedDelegate,
                VariablesEnteredDelegate, maxExpandLevelDelegate,
                currentExpandLevelDelegate, cyclesDelegate
        );
        bindTaskToUIComponents(loadFileToProgramTask, onFinish);
        Thread thread = new Thread(loadFileToProgramTask, "FileLoaderTaskThread");
        thread.start();
    }

    private void bindTaskToUIComponents(LoadFileToProgramTask task, Runnable onFinish) {
        progressBar.progressProperty().bind(task.progressProperty());
        taskMessage.textProperty().bind(task.messageProperty());
        percentLabel.textProperty().bind(Bindings.concat(
                Bindings.format(
                        "%.0f",
                        Bindings.multiply(
                                task.progressProperty(),
                                100)),
                " %"));

        task.setOnSucceeded(e -> {
            System.out.println(Thread.currentThread().getName());
            onFinish.run();
        });
        task.setOnCancelled(e -> onFinish.run());
        task.setOnFailed(e -> onFinish.run());
    }

}
