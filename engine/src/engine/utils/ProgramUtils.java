package engine.utils;

import engine.core.Command;
import engine.core.Instruction;
import org.jetbrains.annotations.Contract;
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

    public static int calculateTotalCreditsCost(@NotNull List<Instruction> instructions) {
        return instructions.stream()
                .mapToInt(Command::getArchitectureCreditsCost)
                .sum();
    }

    public static @NotNull ArchitectureType calcMinimumArchitectureLevelNeeded(@NotNull List<Instruction> instructions) {
        Optional<ArchitectureType> minimumArchNeeded = instructions.stream()
                .map(Command::getArchitectureType)
                .max(Comparator.comparingInt(ArchitectureType::getCreditsCost));
        return minimumArchNeeded.orElseThrow(() ->
                new IllegalStateException("Unexpected error calculating architecture type"));
    }

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

    @Contract(pure = true)
    public static @NotNull Set<String> extractAllVariableAndLabelNamesUnsorted(@NotNull Map<String, Integer>
                                                                                       contextMap,
                                                                               boolean includeLabels) {
        Map<String, Integer> newContextMap = new HashMap<>(contextMap);
        newContextMap.remove(EXIT_LABEL_NAME);
        newContextMap.remove(PC_NAME);
        if (!includeLabels) {
            newContextMap.keySet().removeIf(ProgramUtils::isLabel);
        }
        return new HashSet<>(newContextMap.keySet());
    }

    @Contract(pure = true)
    public static @NotNull Map<String, Integer> extractSortedWorkVars(@NotNull Map<String, Integer> contextMap) {
        return getVariableIntegerMap(contextMap, WORK_VAR_PREFIX);
    }

    @Contract(pure = true)
    public static @NotNull Map<String, Integer> extractSortedArguments(@NotNull Map<String, Integer> contextMap) {
        return getVariableIntegerMap(contextMap, ARG_PREFIX);
    }

    @Contract(pure = true)
    private static @NotNull Map<String, Integer> getVariableIntegerMap(@NotNull Map<String, Integer> contextMap,
                                                                       @NotNull String argPrefix) {
        Map<String, Integer> sortedArguments = new LinkedHashMap<>();
        contextMap.entrySet().stream()
                .filter(e -> e.getKey().startsWith(argPrefix))
                .sorted(Map.Entry.comparingByKey(Comparator.comparingInt(str -> Integer.parseInt(str.substring(1)))))
                .forEach(e -> sortedArguments.put(e.getKey(), e.getValue()));
        return sortedArguments;
    }

    @Contract(pure = true)
    public static @NotNull Set<String> extractLabels(@NotNull List<Set<String>> labelsByExpandLevel, int expandLevel) {
        if (expandLevel < 0 || expandLevel >= labelsByExpandLevel.size()) {
            throw new IllegalArgumentException("Invalid expand level: " + expandLevel);
        }
        return new HashSet<>(labelsByExpandLevel.get(expandLevel));
    }

    public static boolean isFunctionCall(@NotNull String argName) {
        if (argName.startsWith("(") && argName.endsWith(")")) {
            int openParenIndex = argName.indexOf('(');
            int closeParenIndex = argName.lastIndexOf(')');
            return openParenIndex < closeParenIndex;
        } else {
            return false;
        }
    }

    public static @NotNull Set<String> extractAllVariablesFromQuoteArguments(@NotNull String value) {
        Set<String> variables = new LinkedHashSet<>();
        extractVariablesHelper(value, variables);
        return variables;
    }

    private static void extractVariablesHelper(@NotNull String value, @NotNull Set<String> variables) {
        List<String> parts = splitArgs(value);
        for (String part : parts) {
            if (isFunctionCall(part)) {
                extractVariablesHelper(extractFunctionContent(part), variables);
            } else {
                part = part.trim();
                if (isSingleValidArgument(part)) {
                    variables.add(part);
                }
            }
        }
    }

    public static void initAllVariablesFromQuoteArguments(@NotNull String value,
                                                          @NotNull Map<String, Integer> originalContextMap) {
        Set<String> variables = extractAllVariablesFromQuoteArguments(value);
        for (String var : variables) {
            if (!originalContextMap.containsKey(var)) {
                originalContextMap.put(var, 0);
            }
        }
    }

    public static @NotNull String extractFunctionContent(@NotNull String argName) {
        return argName.substring(1, argName.length() - 1); // Remove parentheses
    }

    /**
     * Splits the input string into arguments, considering nested parentheses for composition.
     *
     * @param input the input string to split
     * @return a list of split arguments
     */
    public static @NotNull List<String> splitArgs(@NotNull String input) {
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

    /**
     * Checks if the given argument name is a numbered label (e.g., "L1", "L2", etc.).
     * notice it return false for "EXIT"
     *
     * @param argName the argument name to check
     * @return true if the argument name is a numbered label, false otherwise
     */

    public static boolean isNumberedLabel(@NotNull String argName) {
        return argName.startsWith(LABEL_PREFIX);
    }

    /**
     * Checks if the given argument name is a label (either numbered label or "EXIT").
     *
     * @param argName the argument name to check
     * @return true if the argument name is a label, false otherwise
     */
    public static boolean isLabel(@NotNull String argName) {
        return isNumberedLabel(argName) || argName.equals(EXIT_LABEL_NAME);
    }


    public static boolean isArgument(@NotNull String argName) {
        return argName.startsWith(ARG_PREFIX);
    }

    /**
     * Checks if the given argument name is a variable (work variable, argument, or output).
     *
     * @param argName the argument name to check
     * @return true if the argument name is a variable, false otherwise
     */
    public static boolean isVariable(@NotNull String argName) {
        return argName.startsWith(WORK_VAR_PREFIX) || argName.startsWith(ARG_PREFIX) || argName.equals(OUTPUT_NAME);
    }

    /**
     * Checks if the given argument name is either a variable or a label.
     * includes the label "EXIT"
     *
     * @param argName the argument name to check
     * @return true if the argument name is either a variable or a label, false otherwise
     */
    public static boolean isVariableOrLabel(@NotNull String argName) {
        return isVariable(argName) || isLabel(argName);
    }

}
