package ui.web.utils;

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
import system.controller.LocalEngineController;
import ui.web.jfx.ExecutionController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class UIUtils {
    public static final String ArgumentResourcePath = "/ui/jfx/VariableInputDialog/VariableInputDialog.fxml";
    public static final Comparator<String> programNameComparator =
            Comparator.comparingInt(str -> Integer.parseInt(str.substring(1)));
    public static boolean showInfoAndSuccess = false;
    // Reference to AppController for re-run functionality
    private static ExecutionController executionControllerInstance = null;

    public static void setAppControllerInstance(ExecutionController executionController) {
        executionControllerInstance = executionController;
        System.out.println("AppController instance set for UIUtils re-run functionality");
    }

    public static boolean isValidProgramArgument(String arg) {
        return LocalEngineController.validArgumentValueCheck.test(arg);
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
     * @param executionStats      Execution statistics to display
     * @param finalVariableStates Final variable states from the execution
     */
    public static void openShowRunDialog(
            @NotNull ExecutionResultStatisticsDTO executionStats,
            @NotNull Map<String, Integer> finalVariableStates) {

        try {
            // Load the FXML file for the Show Run dialog
            FXMLLoader loader = new FXMLLoader(
                    UIUtils.class.getResource("/ui/jfx/statistics/ShowRunView.fxml")
            );
            Parent root = loader.load();

            // Get the controller and initialize it with data
//            ShowRunController controller = loader.getController();
//            controller.initializeWithData(executionStats, finalVariableStates);

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

    public static void executeRerunFromShowDialog(int expandLevel, Map<String, Integer> arguments) {
        if (executionControllerInstance == null) {
            throw new IllegalStateException("AppController instance not set in UIUtils");
        }

        // This should trigger the complete rerun process
        executionControllerInstance.handleRerunRequest(expandLevel, arguments);
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