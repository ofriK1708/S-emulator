package engine.utils;

import engine.core.Instruction;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

public class ProgramUtils {
    public static final String EXIT_LABEL_NAME = "EXIT";
    public static final String PC_NAME = "PC";
    public static final String OUTPUT_NAME = "y";
    public static final String ARG_PREFIX = "x";
    public static final String WORK_VAR_PREFIX = "z";
    public static final String LABEL_PREFIX = "L";


    public static @NotNull Predicate<String> validArgumentCheck = (arg) ->
    {
        try {
            int value = Integer.parseInt(arg);
            return value >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    };

    public static @NotNull String getNextFreeLabelName(@NotNull Map<String, Integer> contextMap) {
        int labelIndex = 1;
        String labelName;
        do {
            labelName = LABEL_PREFIX + labelIndex++;
        } while (contextMap.containsKey(labelName));
        contextMap.put(labelName, 0); // Initialize label with a dummy value
        return labelName;
    }

    public static @NotNull String getNextFreeWorkVariableName(@NotNull Map<String, Integer> contextMap) {
        int varIndex = 1;
        String varName;
        do {
            varName = WORK_VAR_PREFIX + varIndex++;
        } while (contextMap.containsKey(varName));
        contextMap.put(varName, 0); // Initialize variable with a dummy value
        return varName;
    }

    private static boolean isNumber(@NotNull String argName) {
        try {
            Integer.parseInt(argName);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isSingleValidArgument(@NotNull String argName) {
        return argName.equals(OUTPUT_NAME) || argName.startsWith(ARG_PREFIX) || argName.startsWith(WORK_VAR_PREFIX);
    }

    public static int calculateExpandedLevel(@NotNull Instruction instruction, int currentLevel) {
        if (currentLevel == -1) {

            // Basic instructions have an expand level of 0
            if (instruction.getType() == CommandType.BASIC) {
                return 0;
            }

            // For synthetic instructions, we need to expand and calculate recursively
            List<Instruction> expanded = instruction.expand(new HashMap<>(), 0);

            int maxSubLevel = getMaxExpandLevel(expanded);

            return 1 + maxSubLevel;
        }
        return currentLevel;
    }

    public static int calculateExpandedLevel(@NotNull Instruction instruction) {
        return calculateExpandedLevel(instruction, -1);
    }

    public static int getMaxExpandLevel(@NotNull List<Instruction> instructions) {
        int maxLevel = 0;
        for (Instruction instr : instructions) {
            maxLevel = Math.max(maxLevel, instr.getExpandLevel());
        }
        return maxLevel;
    }

    public static @NotNull Set<String> extractAllVariableAndLabelNamesUnsorted(@NotNull Map<String, Integer> contextMap) {
        contextMap.remove(EXIT_LABEL_NAME);
        contextMap.remove(PC_NAME);
        return new HashSet<>(contextMap.keySet());
    }

    public static @NotNull Map<String, Integer> extractSortedWorkVars(@NotNull Map<String, Integer> contextMap) {
        return getStringIntegerMap(contextMap, WORK_VAR_PREFIX);
    }

    public static @NotNull Map<String, Integer> extractSortedArguments(@NotNull Map<String, Integer> contextMap) {
        return getStringIntegerMap(contextMap, ARG_PREFIX);
    }

    private static @NotNull Map<String, Integer> getStringIntegerMap(@NotNull Map<String, Integer> contextMap, @NotNull String argPrefix) {
        Map<String, Integer> sortedArguments = new LinkedHashMap<>();
        contextMap.entrySet().stream()
                .filter(e -> e.getKey().startsWith(argPrefix))
                .sorted(Map.Entry.comparingByKey(Comparator.comparingInt(str -> Integer.parseInt(str.substring(1)))))
                .forEach(e -> sortedArguments.put(e.getKey(), e.getValue()));
        return sortedArguments;
    }

    public static @NotNull Set<String> extractLabels(@NotNull List<Set<String>> labelsByExpandLevel, int expandLevel) {
        if (expandLevel < 0 || expandLevel >= labelsByExpandLevel.size()) {
            throw new IllegalArgumentException("Invalid expand level: " + expandLevel);
        }
        return new HashSet<>(labelsByExpandLevel.get(expandLevel));
    }

    public static boolean isFunctionCall(String argName) {
        if (argName.startsWith("(") && argName.endsWith(")")) {
            int openParenIndex = argName.indexOf('(');
            int closeParenIndex = argName.lastIndexOf(')');
            return openParenIndex < closeParenIndex;
        } else {
            return false;
        }
    }

    public static void initAllVariablesFromQuoteArguments(String value, Map<String, Integer> originalContextMap) {
        if (isFunctionCall(value)) {
            value = extractFunctionContent(value);
        }
        String[] parts = value.split(",");
        for (String part : parts) {
            part = part.trim();
            if (isSingleValidArgument(part) && !originalContextMap.containsKey(part)) {
                originalContextMap.put(part, 0);
            } else if (isFunctionCall(part)) {
                initAllVariablesFromQuoteArguments(part, originalContextMap);
            } else {
                System.out.println("Encountered \"" + part + "\" a function name or invalid argument while initializing variables from quote arguments.");
            }
        }
    }

    public static String extractFunctionContent(String argName) {
        return argName.substring(1, argName.length() - 1); // Remove parentheses
    }

    public static List<String> splitArgs(String input) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int parentLevel = 0;
        for (char c : input.toCharArray()) {
            if (c == ',' && parentLevel == 0) {
                result.add(current.toString().trim());
                current.setLength(0);
            } else {
                if (c == '(') parentLevel++;
                if (c == ')') parentLevel--;
                current.append(c);
            }
        }
        if (!current.isEmpty()) {
            result.add(current.toString().trim());
        }
        return result;
    }
}
