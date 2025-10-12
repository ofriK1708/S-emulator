package uiweb.jfx.cycles;

import javafx.beans.property.IntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.jetbrains.annotations.NotNull;


public class CyclesController {

    @FXML
    private Label numOfCycles;

    public void initComponent(@NotNull IntegerProperty numOfCycles) {
        this.numOfCycles.textProperty().bind(numOfCycles.asString());
    }
}
