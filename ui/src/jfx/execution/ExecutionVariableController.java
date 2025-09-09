package jfx.execution;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import jfx.AppController;

import java.util.Map;

/**
 * Displays the work-variable map produced by the latest execution.
 * No buttons; data refreshed by AppController after each run.
 */
public class ExecutionVariableController {

    @FXML private VBox varsBox;

    private AppController app;   // injected from parent

    /* ---------- public API ---------- */
    public void setAppController(AppController controller){
        this.app = controller;
    }

    /** Called by AppController after every run to show fresh values. */
    public void showWorkVariables(Map<String,Integer> workVars){
        varsBox.getChildren().clear();

        if (workVars==null || workVars.isEmpty()){
            varsBox.getChildren().add(new Label("-- none --"));
            return;
        }

        workVars.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> varsBox.getChildren()
                        .add(new Label(e.getKey()+": "+e.getValue())));
    }
}
