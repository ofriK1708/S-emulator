package ui.dashboard.header;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import ui.dashboard.chargeCredits.ChargeCreditsDialogController;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

import static ui.utils.UIUtils.showError;
import static ui.utils.clientConstants.CHARGE_CREDITS_PATH;

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
    private Consumer<Integer> onChargeCredits;
    private IntegerProperty availableCredits;

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
        this.onChargeCredits = onChargeCredits;
        this.availableCredits = availableCredits;

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
        try {
            URL url = getClass().getResource(CHARGE_CREDITS_PATH);
            if (url == null) {
                throw new IllegalStateException("FXML resource not found: " + CHARGE_CREDITS_PATH);
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource(CHARGE_CREDITS_PATH));
            Parent root = loader.load();

            ChargeCreditsDialogController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Charge Credits");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(chargeCreditsButton.getScene().getWindow());
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            controller.init(availableCredits.get(), dialogStage);

            dialogStage.showAndWait();

            Integer result = (Integer) dialogStage.getUserData();
            if (result != null) {
                if (onChargeCredits != null) {
                    // Only charge if the amount has changed
                    if (availableCredits.get() != result) {
                        System.out.println("DashboardHeader: Charging credits - " + result);
                        onChargeCredits.accept(result);
                    }
                }
            } else {
                showError("credit charging failed");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}