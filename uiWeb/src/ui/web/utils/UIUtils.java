package ui.web.utils;

import dto.ui.VariableDTO;
import engine.utils.ProgramUtils;
import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableRow;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNull;
import system.controller.LocalEngineController;
import ui.web.jfx.ExecutionController;

import java.util.*;

public class UIUtils {
    public static final String ArgumentResourcePath = "/ui/jfx/VariableInputDialog/VariableInputDialog.fxml";
    public static final Comparator<String> programNameComparator =
            Comparator.comparingInt(str -> Integer.parseInt(str.substring(1)));
    public static boolean showInfoAndSuccess = false;
    // Reference to AppController for re-run functionality
    private static ExecutionController executionControllerInstance = null;

    // for later use if needed
    public static void setShowInfoAndSuccess(boolean showInfoAndSuccess) {
        UIUtils.showInfoAndSuccess = showInfoAndSuccess;
    }

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

    public static @NotNull List<String> sortAllProgramNames(@NotNull Set<String> programNames) {
        List<String> sortedProgramNames = new ArrayList<>();
        List<String> sortedLabels = new ArrayList<>();
        List<String> sortedArguments = new ArrayList<>();
        List<String> sortedWorkVars = new ArrayList<>();
        for (String name : programNames) {
            if (name.startsWith(ProgramUtils.LABEL_PREFIX)) {
                sortedLabels.add(name);
            } else if (name.startsWith(ProgramUtils.ARG_PREFIX)) {
                sortedArguments.add(name);
            } else if (name.startsWith(ProgramUtils.WORK_VAR_PREFIX)) {
                sortedWorkVars.add(name);
            }
        }
        sortedLabels.sort(programNameComparator);
        sortedArguments.sort(programNameComparator);
        sortedWorkVars.sort(programNameComparator);
        sortedProgramNames.add(ProgramUtils.OUTPUT_NAME);
        sortedProgramNames.addAll(sortedArguments);
        sortedProgramNames.addAll(sortedWorkVars);
        sortedProgramNames.addAll(sortedLabels);
        return sortedProgramNames;
    }

    /**
     * SIMPLIFIED: Open Show Run dialog without re-run callback.
     * Re-run functionality is now handled internally by the Show dialog.
     *
     * @param executionStats      Execution statistics to display
     * @param finalVariableStates Final variable states from the execution
     */
    public static void openShowRunDialog(
            @NotNull ExecutionStatisticsDTO executionStats,
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
            dialogStage.setTitle("Execution Details - Run #" + executionStats.executionNumber());
            dialogStage.setScene(new Scene(root));


            // Show the dialog
            dialogStage.show();

            System.out.println("Show Run dialog opened for execution #" +
                    executionStats.executionNumber() + " with integrated re-run functionality");

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
        return getSortedVariableDTOS(programArguments);
    }

    /**
     * Gets all workVariables from the engine controller as a map.
     *
     * @param engineController The engine controller instance
     * @param expandLevel      The expansion level to get workVariables from
     * @return Map of all variable names to their current values
     */
    public static @NotNull Map<String, Integer> getAllVariablesMap(
            @NotNull LocalEngineController engineController,
            int expandLevel) {

        Map<String, Integer> allVariables = new LinkedHashMap<>();

        // Add work workVariables (z1, z2, etc.)
        allVariables.putAll(engineController.getWorkVars(expandLevel));

        // Add arguments (x1, x2, etc.)
        allVariables.putAll(engineController.getSortedArguments(expandLevel));

        // Add output variable (y)
        allVariables.put(ProgramUtils.OUTPUT_NAME, engineController.getProgramResult(expandLevel));

        sortVariableMapByName(allVariables);

        return allVariables;
    }

    /**
     * Converts all workVariables from engine controller to VariableDTO list for UI display.
     *
     * @param engineController The engine controller instance
     * @param expandLevel      The expansion level to get workVariables from
     * @return List of VariableDTO objects sorted by name
     */
    public static @NotNull List<VariableDTO> getAllVariablesDTOSorted(
            @NotNull LocalEngineController engineController,
            int expandLevel) {

        Map<String, Integer> allVariables = getAllVariablesMap(engineController, expandLevel);
        Map<String, Integer> sortedVars = sortVariableMapByName(allVariables);

        // Sort workVariables by name for consistent display

        return getSortedVariableDTOS(sortedVars);
    }

    public static Map<String, Integer> sortVariableMapByName(Map<String, Integer> variableMap) {
        List<String> sortedVariablesNames = sortAllProgramNames(variableMap.keySet());
        Map<String, Integer> sortedMap = new LinkedHashMap<>();

        for (String key : sortedVariablesNames) {
            sortedMap.put(key, variableMap.get(key));
        }
        return sortedMap;
    }

    private static @NotNull List<VariableDTO> getSortedVariableDTOS(Map<String, Integer> allVariablesSorted) {
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