package ui.jfx.dashboard.header;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.function.Consumer;

public class DashboardHeaderController {

    @FXML private Label userNameLabel;
    @FXML private Button loadFileButton;
    @FXML private TextField filePathField;
    @FXML private TextField creditsField;
    @FXML private Button chargeCreditsButton;


    @FXML
    private void handleLoadFile() {
    }

    @FXML
    private void handleChargeCredits() {
    }

    public void setUserName(String username) {
    }
}
