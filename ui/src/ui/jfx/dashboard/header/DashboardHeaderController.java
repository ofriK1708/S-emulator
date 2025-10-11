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
    private @NotNull Consumer<File> onFileLoad;
    private @NotNull Consumer<Integer> onChargeCredits;


    @FXML
    private void handleChargeCredits() {
    }

    public void setUserName(String username) {
    }

    public void initComponent(
            @NotNull StringProperty currentFilePath,
            @NotNull IntegerProperty availableCredits,
            @NotNull Consumer<File> onFileLoad,
            @NotNull Consumer<Integer> onChargeCredits) {

        this.onFileLoad = onFileLoad;
        this.onChargeCredits = onChargeCredits;

        filePathField.textProperty().bind(currentFilePath);
        creditsField.textProperty().bind(availableCredits.asString());

        System.out.println("DashboardHeaderController initialized");
    }
    @FXML
    private void handleLoadFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Program File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("XML Files", "*.xml"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File file = fileChooser.showOpenDialog(loadFileButton.getScene().getWindow());
        if (file != null) {
            onFileLoad.accept(file);
        }
    }

}
