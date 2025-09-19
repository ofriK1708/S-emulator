package ui.utils;

import dto.ui.VariableDTO;
import engine.utils.ProgramUtils;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Alert;
import org.jetbrains.annotations.NotNull;
import system.controller.controller.EngineController;

import java.util.*;

public class UIUtils {
    public static final String ArgumentResourcePath = "/ui/jfx/VariableInputDialog/VariableInputDialog.fxml";

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

    public static @NotNull List<VariableDTO> getAllVariablesSorted(@NotNull EngineController engineController, int expandLevel) {
        List<VariableDTO> variablesSorted = new ArrayList<>();

        variablesSorted.add(toVariableDTO(ProgramUtils.OUTPUT_NAME, engineController.getProgramResult()));
        engineController.getSortedArguments().entrySet().stream()
                .map(UIUtils::toVariableDTO)
                .forEach(variablesSorted::add);

        engineController.getWorkVars(expandLevel).entrySet().stream()
                .map(UIUtils::toVariableDTO)
                .forEach(variablesSorted::add);

        return variablesSorted;
    }

    public static @NotNull List<VariableDTO> extractArguments(@NotNull Map<String, Integer> arguments) {
        return arguments.entrySet().stream()
                .map(UIUtils::toVariableDTO)
                .toList();

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
}
