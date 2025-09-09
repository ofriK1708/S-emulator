package ui.utils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class UIUtils {
    public static final Comparator<String> programNameComparator =
            Comparator.comparingInt(str -> Integer.parseInt(str.substring(1)));

    public static boolean isValidProgramArgument(String arg) {
        try {
            int value = Integer.parseInt(arg);
            return value >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    public static List<Integer> getRuntimeArgument(Map<String,Integer> programArgsNameAndValue) {
        return programArgsNameAndValue.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(programNameComparator))
                .map(Map.Entry::getValue)
                .toList();
    }
}
