package ui.jfx.cycles;

import javafx.beans.property.IntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;


public class CyclesController {

    @FXML
    private Label numOfCycles;

    public void initComponent(IntegerProperty numOfCycles) {
        this.numOfCycles.textProperty().bind(numOfCycles.asString());
    }
}
