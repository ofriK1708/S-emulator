package ui.jfx.cycles;

import javafx.fxml.FXML;
import javafx.scene.control.Label;


public class CyclesController {

    @FXML
    private Label numOfCycles;

    public void setNumOfCycles(int num) {
        numOfCycles.setText(String.valueOf(num));
    }
}
