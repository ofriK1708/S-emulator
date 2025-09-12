package ui.utils;

import dto.ui.VariableDTO;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import system.controller.controller.EngineController;

import java.util.Comparator;
import java.util.Map;

public class UIUtils {
    public static final Comparator<String> programNameComparator =
            Comparator.comparingInt(str -> Integer.parseInt(str.substring(1)));

    public static boolean isValidProgramArgument(String arg) {
        return EngineController.validArgumentValueCheck.test(arg);
    }

    public static VariableDTO toVariableDTO(Map.Entry<String, Integer> entry) {
        return new VariableDTO(new SimpleStringProperty(entry.getKey()),
                new SimpleIntegerProperty(entry.getValue()));
    }
}
