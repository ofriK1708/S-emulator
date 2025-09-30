package ui.jfx.fileHandler;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.function.Consumer;

public class FileHandlerController {
    @FXML
    private HBox jokeBox;
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
    private void onAddFileButtonPressed(MouseEvent event) {
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
    private void onClearButtonPressed(MouseEvent event) {
        // Clear the selected file
        selectedFile = null;
        // Clear and disable the text field
        filePath.set("");
        onClearFileCallback.run();
    }

    public void addAppleJoke() {
        // 1. SVG Icon
        SVGPath appleIcon = new SVGPath();
        appleIcon.setContent("M16.3 0c.214 1.46-.378 2.89-1.16 3.9c-.835 1.08-2.27 1.92-3.66 1.88c-.254-1.4.398-2.83 " +
                             "1.19-3.8C13.539.91 15.03.1 16.3.01zm.5 6c1.59 0 3.27.874 4.47 2.38c-3.93 2.17-3.29 7.84" +
                             ".68 " +
                             "9.36c-.413.996-.919 1.95-1.51 2.85c-.982 1.51-2.37 " +
                             "3.39-4.08 3.41c-.706.007-1.17-.207-1.67-.438c-.579-.267-1.21-.557-2.32-." +
                             "551c-1.1.005-1.74.292-2.33.556c-.512.23-.984.443-1.69.436c-1.72-.015-3.03-1.71-4.01-3." +
                             "22c-2.75-4.22-3.03-9.18-1.34-11.8c1.2-1.87 3.1-2.97 4.89-2.97c.952 0 1.72.276 2.45.539c" +
                             ".664.239 " +
                             "1.3.467 2.01.467c.662 0 1.21-.208 1.8-.435c.714-.273 1.5-.573 2.65-.573z");
        appleIcon.setFill(Color.BLACK);
        // Make the SVG icon larger
        appleIcon.setScaleX(1.5);
        appleIcon.setScaleY(1.5);

        Label label = new Label("Mac? any day! mac is the best! mac > windows");
        label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        jokeBox.getChildren().addAll(label, appleIcon);
    }

    public void addWindowsJoke() {
        // 1. SVG Icon
        SVGPath windowsPart1 = new SVGPath();
        windowsPart1.setContent("M6.235 6.778C4.105 9.089 3 12.328 3 15.998c0 2.667.584 5.106 1.719 7.127a2.5 2.5 0 0" +
                                " " +
                                "0-1.512 1.377C1.723 22.058 1 19.117 1 16c0-4.061 1.226-7.821 3.763-10.576C7.313 " +
                                "2.654 11.095 1 16 1s8.686 1.654 11.235 4.423c2.537 2.755 3.764 6.515 3.764 " +
                                "10.576c0 3.366-.843 6.525-2.575 9.076a.75.75 0 1 1-.417.579a13 13 0 0 1-1.208 " +
                                "1.372c-1.62-.058-2.972-.75-3.823-1.711Q23 25.16 23 25v-2a2 2 0 0 0-1.997-2l.007 " +
                                "2.16c.067 1.183.628 2.28 1.516 3.133c.841.807 1.976 1.394 3.261 1.627q.598.109 " +
                                "1.233.11c1.1 0 1.98.97 1.98 1.97H3c0-1.11.87-1.97 1.98-1.97q.634-.001 " +
                                "1.231-.11c1.287-.23 2.424-.816 3.267-1.622c.953-.91 1.532-2.1 1.532-3.378L11.003 " +
                                "21H11a2 2 0 0 0-2 2v2q0 .166.026.325a4.8 4.8 0 0 1-1.144.936A2.5 2.5 0 0 0 7 23" +
                                ".5V23a4 " +
                                "4 0 0 1 4-4h10c1.491 0 2.792.816 3.48 2.027A2.496 2.496 0 0 0 25 " +
                                "24.5v.5q-.002.576-.154 1.105a11 11 0 0 0 1.157-1.154q.241.048.497.049a2.5 2.5 0 0 " +
                                "0 1.797-4.238c.465-1.463.7-3.067.7-4.763c0-3.67-1.105-6.91-3.234-9.221C23.647 4.48 " +
                                "20.43 3 16 3S8.35 4.48 6.235 6.778");
        Group windowsIcon = getGroup(windowsPart1);

        // Make the Windows icon larger
        windowsIcon.setScaleX(1.5);
        windowsIcon.setScaleY(1.5);

        Label label = new Label("Windows? ew, gross, get a mac");
        label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-font-family: 'Comic Sans MS', cursive;");
        jokeBox.getChildren().addAll(label, windowsIcon);

    }

    private @NotNull Group getGroup(SVGPath windowsPart1) {
        SVGPath windowsPart2 = new SVGPath();
        windowsPart2.setContent("M27.877 21.904a1.5 1.5 0 1 1-2.755 1.192a1.5 1.5 0 0 1 2.755-1.192M6.94 25.919a1.5 " +
                                "1.5 0 1 1-2.88-.84a1.5 1.5 0 0 1 2.88.84m1.166-13.366a1 1 0 0 1 1.341-.448l4 2a1 1 " +
                                "0 0 1 0 1.79l-4 2a1 1 0 1 1-.894-1.79L10.763 15l-2.21-1.106a1 1 0 0 " +
                                "1-.447-1.341m14.447-.447a1 1 0 0 1 .894 1.788L21.237 15l2.21 1.105a1 " +
                                "1 0 0 1-.894 1.79l-4-2a1 1 0 0 1 0-1.79z");

        // Setting fill color for the Windows icon (light gray for the sad Windows face)
        windowsPart1.setFill(Color.web("#0078D7")); // Windows blue
        windowsPart2.setFill(Color.web("#D83B01")); // Windows accent color

        return new Group(windowsPart1, windowsPart2);
    }

    public void removeJokes() {
        jokeBox.getChildren().clear();
    }
}