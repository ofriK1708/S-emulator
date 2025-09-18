package engine.utils;

import engine.core.Instruction;

import java.util.*;
import java.util.function.Predicate;

public class ProgramUtils {
    public static final String EXIT_LABEL_NAME = "EXIT";
    public static final String PC_NAME = "PC";
    public static final String OUTPUT_NAME = "y";
    public static final String ARG_PREFIX = "x";
    public static final String WORK_VAR_PREFIX = "z";
    public static final String LABEL_PREFIX = "L";



    public static Predicate<String> validArgumentCheck = (arg) ->
    {
        try {
            int value = Integer.parseInt(arg);
            return value >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    };

    public static String getNextFreeLabelName(Map<String, Integer> contextMap) {
        int labelIndex = 1;
        String labelName;
        do {
            labelName = LABEL_PREFIX + labelIndex++;
        } while (contextMap.containsKey(labelName));
        contextMap.put(labelName, 0); // Initialize label with a dummy value
        return labelName;
    }

    public static String getNextFreeWorkVariableName(Map<String, Integer> contextMap) {
        int varIndex = 1;
        String varName;
        do {
            varName = WORK_VAR_PREFIX + varIndex++;
        } while (contextMap.containsKey(varName));
        contextMap.put(varName, 0); // Initialize variable with a dummy value
        return varName;
    }

    private static boolean isNumber(String argName) {
        try {
            Integer.parseInt(argName);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isSingleValidArgument(String argName) {
        return argName.equals(OUTPUT_NAME) || argName.startsWith(ARG_PREFIX) || argName.startsWith(WORK_VAR_PREFIX);
    }

    public static int calculateExpandedLevel(Instruction instruction, int currentLevel) {
        if (currentLevel == -1) {
            if (instruction == null) {
                throw new IllegalArgumentException("Instruction cannot be null");
            }

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

    public static int calculateExpandedLevel(Instruction instruction) {
        return calculateExpandedLevel(instruction, -1);
    }

    public static int getMaxExpandLevel(List<Instruction> instructions) {
        int maxLevel = 0;
        for (Instruction instr : instructions) {
            maxLevel = Math.max(maxLevel, instr.getExpandLevel());
        }
        return maxLevel;
    }

    public static Set<String> extractAllVariableAndLabelNames(Map<String, Integer> contextMap) {
        contextMap.remove(EXIT_LABEL_NAME);
        contextMap.remove(PC_NAME);
        return new HashSet<>(contextMap.keySet());
    }

    public static Map<String, Integer> extractWorkVars(Map<String, Integer> contextMap) {
        return getStringIntegerMap(contextMap, WORK_VAR_PREFIX);
    }

    public static Map<String, Integer> extractArguments(Map<String, Integer> contextMap) {
        return getStringIntegerMap(contextMap, ARG_PREFIX);
    }

    private static Map<String, Integer> getStringIntegerMap(Map<String, Integer> contextMap, String argPrefix) {
        Map<String, Integer> arguments = new HashMap<>();
        for (Map.Entry<String, Integer> entry : contextMap.entrySet()) {
            if (entry.getKey().startsWith(argPrefix)) {
                arguments.put(entry.getKey(), entry.getValue());
            }
        }
        return arguments;
    }

    public static Set<String> extractLabels(List<Set<String>> labelsByExpandLevel, int expandLevel) {
        if (expandLevel < 0 || expandLevel >= labelsByExpandLevel.size()) {
            throw new IllegalArgumentException("Invalid expand level: " + expandLevel);
        }
        return new HashSet<>(labelsByExpandLevel.get(expandLevel));
    }

}
