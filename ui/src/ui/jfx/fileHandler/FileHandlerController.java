package ui.jfx.fileHandler;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.function.Consumer;

public class FileHandlerController {
    @FXML
    private Button addFileButton;
    @FXML
    private Button clearButton;
    @FXML
    private Label filePathField;

    private Consumer<File> onFileLoadedCallback;
    private Runnable onClearFileCallback;
    private final StringProperty filePath = new SimpleStringProperty();
    private @Nullable File selectedFile = null;

    public void initComponent(Consumer<File> onFileLoadedCallback, Runnable onClearFileCallback) {
        this.onFileLoadedCallback = onFileLoadedCallback;
        this.onClearFileCallback = onClearFileCallback;
        filePathField.textProperty().bind(filePath);
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
                filePath.set("");
            }

            // Store and display the new file
            selectedFile = file;
            filePath.set(file.getAbsolutePath());
            onFileLoadedCallback.accept(selectedFile);
        }
    }

    @FXML
    void onClearButtonPressed(MouseEvent event) {
        // Clear the selected file
        selectedFile = null;
        // Clear and disable the text field
        filePath.set("");
        onClearFileCallback.run();
    }
}