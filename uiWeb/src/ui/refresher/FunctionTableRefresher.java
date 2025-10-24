package ui.refresher;

import dto.engine.FunctionMetadata;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import system.controller.EngineController;

import java.io.IOException;
import java.util.List;
import java.util.TimerTask;

public class FunctionTableRefresher extends TimerTask {
    private final EngineController httpController;
    private final ListProperty<FunctionMetadata> FunctionInUI;
    private List<FunctionMetadata> functions;

    public FunctionTableRefresher(EngineController httpController,
                                  ListProperty<FunctionMetadata> mainProgramsInUI) {
        this.httpController = httpController;
        this.FunctionInUI = mainProgramsInUI;
    }

    @Override
    public void run() {
        try {
            functions = httpController.getFunctionsMetadata();
            if (!FunctionInUI.get().equals(functions)) {
                Platform.runLater(() -> FunctionInUI.setAll(functions));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
