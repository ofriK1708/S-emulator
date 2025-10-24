package ui.refresher;

import dto.engine.ProgramMetadata;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import system.controller.EngineController;

import java.io.IOException;
import java.util.List;
import java.util.TimerTask;

public class ProgramsTableRefresher extends TimerTask {
    private final EngineController httpController;
    private final ListProperty<ProgramMetadata> mainProgramsInUI;
    private List<ProgramMetadata> programs;

    public ProgramsTableRefresher(EngineController httpController, ListProperty<ProgramMetadata> mainProgramsInUI) {
        this.httpController = httpController;
        this.mainProgramsInUI = mainProgramsInUI;
    }

    @Override
    public void run() {
        try {
            programs = httpController.getProgramsMetadata();
            if (!mainProgramsInUI.get().equals(programs)) {
                Platform.runLater(() -> mainProgramsInUI.setAll(programs));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
