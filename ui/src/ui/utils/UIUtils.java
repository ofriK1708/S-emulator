package ui.utils;

import dto.ui.VariableDTO;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Alert;
import system.controller.controller.EngineController;

import java.util.Comparator;
import java.util.Map;

public class UIUtils {
    public static final String ArgumentResourcePath = "/ui/jfx/VariableInputDialog/VariableInputDialog.fxml";

    public static final Comparator<String> programNameComparator =
            Comparator.comparingInt(str -> Integer.parseInt(str.substring(1)));

    public static boolean isValidProgramArgument(String arg) {
        return EngineController.validArgumentValueCheck.test(arg);
    }

    public static VariableDTO toVariableDTO(Map.Entry<String, Integer> entry) {
        return new VariableDTO(new SimpleStringProperty(entry.getKey()),
                new SimpleIntegerProperty(entry.getValue()));
    }

    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException ignored) {

        }
    }
}
