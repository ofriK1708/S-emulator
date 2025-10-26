package ui.dashboard.header;

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
    private TextField creditsField;
    @FXML
    private Button chargeCreditsButton;

    private Consumer<File> onLoadFile;
    private Runnable onChargeCredits;

    /**
     * Initialize the header with callbacks and bindings
     *
     * @param loggedInUserName Property for displaying logged-in username
     * @param availableCredits Property for available credits display
     * @param onLoadFile       Callback to handle file loading (receives File)
     * @param onChargeCredits  Callback to handle credit charging (receives amount)
     */
    public void initComponent(
            @NotNull StringProperty loggedInUserName,
            @NotNull IntegerProperty availableCredits,
            @NotNull Consumer<File> onLoadFile,
            @NotNull Consumer<Integer> onChargeCredits) {

        this.onLoadFile = onLoadFile;
        this.onChargeCredits = () -> onChargeCredits.accept(50);

        // Bind username, file path, and credits fields to properties
        userNameLabel.textProperty().bind(loggedInUserName);
        creditsField.textProperty().bind(availableCredits.asString());

        System.out.println("DashboardHeaderController initialized with username binding");
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
}