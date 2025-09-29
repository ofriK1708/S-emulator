package ui.utils;

import dto.engine.ExecutionStatisticsDTO;
import dto.ui.VariableDTO;
import engine.utils.ProgramUtils;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import system.controller.EngineController;
import ui.jfx.statistics.ShowRunController;

import java.util.*;

public class UIUtils {
    public static final String ArgumentResourcePath = "/ui/jfx/VariableInputDialog/VariableInputDialog.fxml";

    // Reference to AppController for re-run functionality
    private static ui.jfx.AppController appControllerInstance = null;

    public static void setAppControllerInstance(ui.jfx.AppController appController) {
        appControllerInstance = appController;
        System.out.println("AppController instance set for UIUtils re-run functionality");
    }

    public static final Comparator<String> programNameComparator =
            Comparator.comparingInt(str -> Integer.parseInt(str.substring(1)));

    public static boolean isValidProgramArgument(String arg) {
        return EngineController.validArgumentValueCheck.test(arg);
    }

    public static @NotNull VariableDTO toVariableDTO(Map.@NotNull Entry<String, Integer> entry) {
        return new VariableDTO(new SimpleStringProperty(entry.getKey()),
                new SimpleIntegerProperty(entry.getValue()));
    }

    public static @NotNull VariableDTO toVariableDTO(String variableName, Integer result) {
        return new VariableDTO(new SimpleStringProperty(variableName),
                new SimpleIntegerProperty(result));
    }

    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showSuccess(String message) {
        // Alert disabled for performance - can be re-enabled if needed
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showInfo(String message) {
        // Alert disabled for performance - can be re-enabled if needed
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setContentText(message);
        alert.showAndWait();
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
     * @param executionStats Execution statistics to display
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
            ShowRunController controller = loader.getController();
            controller.initializeWithData(executionStats, finalVariableStates);

            // Create and configure the dialog stage
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle("Execution Details - Run #" + executionStats.executionNumber());
            dialogStage.setScene(new Scene(root));

            // Set minimum size and make it resizable
            dialogStage.setMinWidth(650);
            dialogStage.setMinHeight(550);
            dialogStage.setResizable(true);

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
     * Formats a map of arguments into a readable string for display.
     *
     * @param arguments Map of argument names to values
     * @return Formatted string representation of arguments, or "None" if empty
     */
    public static @NotNull String formatArgumentsForDisplay(@NotNull Map<String, Integer> arguments) {
        if (arguments.isEmpty()) {
            return "None";
        }

        StringBuilder formatted = new StringBuilder();
        boolean first = true;

        for (Map.Entry<String, Integer> entry : arguments.entrySet()) {
            if (!first) {
                formatted.append(", ");
            }
            formatted.append(entry.getKey()).append(" = ").append(entry.getValue());
            first = false;
        }

        return formatted.toString();
    }

    /**
     * Extracts arguments from a program arguments map and converts them to VariableDTO objects.
     *
     * @param programArguments Map of argument names to values
     * @return List of VariableDTO objects for UI display
     */
    public static @NotNull List<VariableDTO> extractArguments(@NotNull Map<String, Integer> programArguments) {
        List<VariableDTO> argumentsList = new ArrayList<>();

        // Convert map entries to VariableDTO objects for table display
        for (Map.Entry<String, Integer> entry : programArguments.entrySet()) {
            VariableDTO variableDTO = new VariableDTO(
                    new SimpleStringProperty(entry.getKey()),
                    new SimpleIntegerProperty(entry.getValue()),
                    // Arguments in the table don't need change detection
                    new SimpleBooleanProperty(false)
            );
            argumentsList.add(variableDTO);
        }

        // Sort arguments by name for consistent display
        argumentsList.sort((v1, v2) -> v1.name().get().compareToIgnoreCase(v2.name().get()));

        return argumentsList;
    }

    /**
     * Gets all variables from the engine controller as a map.
     *
     * @param engineController The engine controller instance
     * @param expandLevel The expansion level to get variables from
     * @return Map of all variable names to their current values
     */
    public static @NotNull Map<String, Integer> getAllVariablesMap(
            @NotNull system.controller.EngineController engineController,
            int expandLevel) {

        Map<String, Integer> allVariables = new HashMap<>();

        // Add work variables (z1, z2, etc.)
        allVariables.putAll(engineController.getWorkVars(expandLevel));

        // Add arguments (x1, x2, etc.)
        allVariables.putAll(engineController.getSortedArguments(expandLevel));

        // Add output variable (y)
        allVariables.put("y", engineController.getProgramResult(expandLevel));

        return allVariables;
    }

    /**
     * Converts all variables from engine controller to VariableDTO list for UI display.
     *
     * @param engineController The engine controller instance
     * @param expandLevel The expansion level to get variables from
     * @return List of VariableDTO objects sorted by name
     */
    public static @NotNull List<VariableDTO> getAllVariablesDTOSorted(
            @NotNull system.controller.EngineController engineController,
            int expandLevel) {

        Map<String, Integer> allVariables = getAllVariablesMap(engineController, expandLevel);
        List<VariableDTO> variablesList = new ArrayList<>();

        // Convert map entries to VariableDTO objects
        for (Map.Entry<String, Integer> entry : allVariables.entrySet()) {
            VariableDTO variableDTO = new VariableDTO(
                    new SimpleStringProperty(entry.getKey()),
                    new SimpleIntegerProperty(entry.getValue()),
                    // No change detection needed for this view
                    new SimpleBooleanProperty(false)
            );
            variablesList.add(variableDTO);
        }

        // Sort variables by name for consistent display
        variablesList.sort((v1, v2) -> v1.name().get().compareToIgnoreCase(v2.name().get()));

        return variablesList;
    }
    public static void executeRerunFromShowDialog(int expandLevel, Map<String, Integer> arguments) {
        if (appControllerInstance == null) {
            throw new IllegalStateException("AppController instance not set in UIUtils");
        }

        // This should trigger the complete rerun process
        appControllerInstance.handleRerunRequest(expandLevel, arguments);
    }
}