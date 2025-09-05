package jfx;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import jfx.cycles.CyclesController;
import jfx.fileHandler.FileHandlerController;

import java.util.Random;

public class AppController {
    Random random = new Random();
    @FXML
    private HBox fileHandler;
    @FXML
    private FileHandlerController fileHandlerController;
    @FXML
    private AnchorPane cycles;
    @FXML
    private CyclesController cyclesController;

    @FXML
    public void initialize() {
        if (fileHandlerController != null && cyclesController != null) {
            fileHandlerController.setAppController(this);
            cyclesController.setNumOfCycles(random.nextInt(1, 1000));
        }
    }

    public void setCyclesController(CyclesController cyclesController) {
        this.cyclesController = cyclesController;
    }

    public void setFileHandlerController(FileHandlerController fileHandlerController) {
        this.fileHandlerController = fileHandlerController;
    }
}
