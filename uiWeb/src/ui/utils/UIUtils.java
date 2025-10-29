package ui.utils;

import dto.engine.ExecutionResultStatisticsDTO;
import dto.ui.VariableDTO;
import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableRow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNull;
import ui.dashboard.history.info.ShowRunController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ui.utils.clientConstants.SHOW_INFO_PATH;

public class UIUtils {
    public static final String ArgumentResourcePath = "/ui/execution/VariableInputDialog/VariableInputDialog.fxml";
    public static final boolean showInfoAndSuccess = true;

    public static boolean isValidProgramArgument(String arg) {
        try {
            int res = Integer.parseInt(arg);
            return res >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidExpandChoice(String choice, int maxExpandLevel) {
        try {
            int res = Integer.parseInt(choice);
            return res >= 0 && res <= maxExpandLevel;
        } catch (NumberFormatException e) {
            return false;
        }
    }

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

    public static void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException ignored) {
            // Ignored
        }
    }

    /**
     * SIMPLIFIED: Open Show Run dialog without re-run callback.
     * Re-run functionality is now handled internally by the Show dialog.
     *
     * @param executionStats Execution statistics to display
     */
    public static void openShowRunDialog(
            @NotNull ExecutionResultStatisticsDTO executionStats) {

        try {
            // Load the FXML file for the Show Run dialog
            FXMLLoader loader = new FXMLLoader(
                    UIUtils.class.getResource(SHOW_INFO_PATH)
            );
            Parent root = loader.load();

            // Get the controller and initialize it with data
            ShowRunController controller = loader.getController();
            controller.initializeWithData(executionStats);

            // Create and configure the dialog stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Execution Details - Run #" + executionStats.runNumber());
            dialogStage.setScene(new Scene(root));


            // Show the dialog
            dialogStage.show();

            System.out.println("Show Run dialog opened for execution #" +
                    executionStats.runNumber() + " with integrated re-run functionality");

        } catch (Exception e) {
            System.err.println("Error opening Show Run dialog: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to open execution details dialog: " + e.getMessage());
        }
    }

    /**
     * Extracts arguments from a program arguments map and converts them to VariableDTO objects.
     *
     * @param programArguments Map of argument names to values
     * @return List of VariableDTO objects for UI display
     */
    public static @NotNull List<VariableDTO> formatArgumentsToVariableDTO(
            @NotNull Map<String, Integer> programArguments) {
        return toVariableDTO(programArguments);
    }


    public static @NotNull Stage createTaskLoadingStage(@NotNull String title, @NotNull Parent root) {

        Stage loadingStage = new Stage();
        loadingStage.initModality(Modality.APPLICATION_MODAL);
        loadingStage.setTitle(title);
        loadingStage.setScene(new Scene(root, 1000, 183));

        return loadingStage;
    }


    public static @NotNull List<VariableDTO> toVariableDTO(Map<String, Integer> allVariablesSorted) {
        List<VariableDTO> variablesList = new ArrayList<>();

        // Convert map entries to VariableDTO objects
        for (Map.Entry<String, Integer> entry : allVariablesSorted.entrySet()) {
            VariableDTO variableDTO = new VariableDTO(
                    new SimpleStringProperty(entry.getKey()),
                    new SimpleIntegerProperty(entry.getValue()),
                    // No change detection needed for this view
                    new SimpleBooleanProperty(false)
            );
            variablesList.add(variableDTO);
        }
        return variablesList;
    }

    public static void checkIfShouldAnimate(@NotNull TableRow<?> row, boolean isAnimationsOn) {
        if (isAnimationsOn) {
            FadeTransition fade = new FadeTransition(Duration.millis(400), row);
            fade.setFromValue(1.0);
            fade.setToValue(0.3);
            fade.setCycleCount(6);
            fade.setAutoReverse(true);
            fade.setOnFinished(e -> row.setOpacity(1.0));
            fade.play();
            row.getProperties().put("highlightFade", fade);
        } else {
            System.out.println("Skipping highlight animation as animations are disabled");
        }
    }
}