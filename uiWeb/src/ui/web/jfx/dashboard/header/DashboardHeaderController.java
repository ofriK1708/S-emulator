package ui.web.jfx.dashboard.header;

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

/**
 * Controller for the Dashboard header bar.
 * Displays user name, file path, available credits, and action buttons.
 */
public class DashboardHeaderController {

    @FXML
    private Label userNameLabel;
    @FXML
    private Button loadFileButton;
    @FXML
    private TextField filePathField;
    @FXML
    private TextField creditsField;
    @FXML
    private Button chargeCreditsButton;

    private Consumer<File> onLoadFile;
    private Runnable onChargeCredits;

    /**
     * Initialize the header with callbacks and bindings
     *
     * @param currentFilePath  Property for displaying current file path
     * @param availableCredits Property for available credits display
     * @param onLoadFile       Callback to handle file loading (receives File)
     * @param onChargeCredits  Callback to handle credit charging (receives amount)
     */
    public void initComponent(
            @NotNull StringProperty currentFilePath,
            @NotNull IntegerProperty availableCredits,
            @NotNull Consumer<File> onLoadFile,
            @NotNull Consumer<Integer> onChargeCredits) {

        this.onLoadFile = onLoadFile;
        this.onChargeCredits = () -> onChargeCredits.accept(50); // Default charge amount

        // Bind file path and credits fields to properties
        filePathField.textProperty().bind(currentFilePath);
        creditsField.textProperty().bind(availableCredits.asString());

        System.out.println("DashboardHeaderController initialized");
    }

    @FXML
    private void handleLoadFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Program File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("XML Files", "*.xml")
        );

        File file = fileChooser.showOpenDialog(loadFileButton.getScene().getWindow());

        if (file != null && onLoadFile != null) {
            System.out.println("DashboardHeader: File selected - " + file.getName());
            onLoadFile.accept(file);
        }
    }

    @FXML
    private void handleChargeCredits() {
        if (onChargeCredits != null) {
            System.out.println("DashboardHeader: Charge Credits clicked");
            onChargeCredits.run();
        }
    }

    /**
     * Set user name display
     */
    public void setUserName(String username) {
        if (userNameLabel != null) {
            userNameLabel.setText(username != null ? username : "Guest User");
        }
    }

    /**
     * Set file path display (alternative to binding)
     */
    public void setFilePath(String filePath) {
        if (filePathField != null) {
            filePathField.setText(filePath != null ? filePath : "");
        }
    }

    /**
     * Clear file path display
     */
    public void clearFilePath() {
        if (filePathField != null) {
            filePathField.clear();
        }
    }

    /**
     * Set credits value directly (alternative to binding)
     */
    public void setCredits(int credits) {
        if (creditsField != null) {
            creditsField.setText(String.valueOf(credits));
        }
    }
}