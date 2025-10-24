package utils;

import javafx.scene.control.Alert;

public class UIUtils {
    public static boolean showInfoAndSuccess = false;

    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showSuccess(String message) {
        // Alert disabled for performance - can be re-enabled if needed
        if (showInfoAndSuccess) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setContentText(message);
            alert.showAndWait();
        }
    }

    public static void showInfo(String message) {
        // Alert disabled for performance - can be re-enabled if needed
        if (showInfoAndSuccess) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setContentText(message);
            alert.showAndWait();
        }
    }
}
