package jfx.fileHandler;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import jfx.AppController;

import java.io.File;

public class FileHandlerController {
    private AppController appController;

    @FXML private Button addFileButton;
    @FXML private Button clearButton;
    @FXML private TextField filePathField;

    private File selectedFile = null;

    public void setAppController(AppController appController) {
        this.appController = appController;
    }

    @FXML
    void onAddFileButtonPressed(MouseEvent event) {
        // Create a new file chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select XML File");

        // Set extension filter to only show XML files
        FileChooser.ExtensionFilter xmlFilter =
                new FileChooser.ExtensionFilter("XML Files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(xmlFilter);

        // Show the file chooser dialog
        File file = fileChooser.showOpenDialog(addFileButton.getScene().getWindow());

        // If a file was selected, update the text field and store the file
        if (file != null) {
            // If we already had a file, we're replacing it
            if (selectedFile != null) {
                // Any cleanup code for the old file could go here
                selectedFile = null;
            }

            // Store and display the new file
            selectedFile = file;
            filePathField.setText(file.getAbsolutePath());

            // Enable text field for viewing (but keep it non-editable)
            filePathField.setDisable(false);

            // CRUCIAL FIX: Load the program through AppController
            if (appController != null) {
                appController.loadProgramFromFile(file);
            }
        }
    }

    @FXML
    void onClearButtonPressed(MouseEvent event) {
        // Clear the selected file
        selectedFile = null;

        // Clear and disable the text field
        filePathField.clear();
        filePathField.setDisable(true);
    }

    public File getSelectedFile() {
        return selectedFile;
    }
}