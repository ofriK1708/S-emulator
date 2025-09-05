package ui.javafx.fileloader;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import system.controller.controller.SystemController;
import ui.javafx.main.ComponentEventListener;
import ui.javafx.main.MainController;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.ResourceBundle;

/**
 * Enhanced FileLoader Controller with component communication
 * Integrates with MainController architecture
 */
public class FileLoaderController implements Initializable {

    @FXML private Button loadButton;
    @FXML private Button cleanButton;
    @FXML private Label statusText;

    private Stage stage;
    private SystemController systemController;
    private boolean programLoaded = false;

    // Component communication
    private ComponentEventListener eventListener;
    private MainController mainController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeController();
        setupInitialState();
    }

    private void initializeController() {
        try {
            this.systemController = new SystemController();
        } catch (Exception e) {
            showError("Failed to initialize system controller: " + e.getMessage());
        }
    }

    private void setupInitialState() {
        cleanButton.setDisable(true);
        statusText.setText("Ready to load XML file");
        statusText.getStyleClass().removeAll("success", "error");
    }

    @FXML
    private void onLoadButtonClicked(ActionEvent event) {
        try {
            File selectedFile = showFileChooser();
            if (selectedFile == null) return;

            Path filePath = selectedFile.toPath();
            systemController.LoadProgramFromFile(filePath);

            // Update local state
            programLoaded = true;
            cleanButton.setDisable(false);
            showSuccess("Program loaded: " + selectedFile.getName());

            // Notify main controller
            if (eventListener != null) {
                eventListener.onFileLoaded(systemController);
            }

            System.out.println("File loaded successfully: " + filePath);

        } catch (Exception e) {
            showError("Error loading file: " + e.getMessage());
            programLoaded = false;
            cleanButton.setDisable(true);
        }
    }

    @FXML
    private void onCleanButtonClicked(ActionEvent event) {
        try {
            systemController = new SystemController();

            // Update local state
            programLoaded = false;
            cleanButton.setDisable(true);
            showSuccess("Program cleared successfully");

            // Notify main controller
            if (eventListener != null) {
                eventListener.onFileCleaned();
            }

            System.out.println("Program state cleared");

        } catch (Exception e) {
            showError("Error clearing program: " + e.getMessage());
        }
    }

    private File showFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select XML Program File");

        String userHome = System.getProperty("user.home");
        File initialDir = new File(userHome);
        if (initialDir.exists()) {
            fileChooser.setInitialDirectory(initialDir);
        }

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("XML Files (*.xml)", "*.xml"),
                new FileChooser.ExtensionFilter("All Files (*.*)", "*.*")
        );

        return fileChooser.showOpenDialog(getStage());
    }

    private void showSuccess(String message) {
        statusText.setText(message);
        statusText.getStyleClass().removeAll("error");
        statusText.getStyleClass().add("success");
    }

    private void showError(String message) {
        statusText.setText(message);
        statusText.getStyleClass().removeAll("success");
        statusText.getStyleClass().add("error");
    }

    private Stage getStage() {
        if (stage == null && loadButton != null && loadButton.getScene() != null) {
            stage = (Stage) loadButton.getScene().getWindow();
        }
        return stage;
    }

    // ===== COMPONENT COMMUNICATION METHODS =====

    public void setEventListener(ComponentEventListener listener) {
        this.eventListener = listener;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public SystemController getSystemController() {
        return systemController;
    }

    public boolean isProgramLoaded() {
        return programLoaded;
    }
}
