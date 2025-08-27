package core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProgramUtils
{
    public static final String EXITLabelName = "EXIT";
    public static final String outputName = "y";

    public static String getNextFreeLabelName(Map<String, Integer> contextMap)
    {
        int labelIndex = 1;
        String labelName;
        do
        {
            labelName = "L" + labelIndex++;
        } while (contextMap.containsKey(labelName));
        contextMap.put(labelName, 0); // Initialize label with a dummy value
        return labelName;
    }

    public static String getNextFreeWorkVariableName(Map<String, Integer> contextMap)
    {
        int varIndex = 1;
        String varName;
        do
        {
            varName = "z" + varIndex++;
        } while (contextMap.containsKey(varName));
        contextMap.put(varName, 0); // Initialize variable with a dummy value
        return varName;
    }

    public static void updateLabel(Map<String, Integer> contextMap, String labelName, int newLabelValue)
    {
        if (!labelName.isEmpty())
        {
            if (contextMap.containsKey(labelName))
            {
                contextMap.put(labelName, newLabelValue);
            } else
            {
                throw new IllegalArgumentException("No such label: " + labelName);
            }
        }
    }

    public static boolean isNumber(String argName)
    {
        try
        {
            Integer.parseInt(argName);
            return true;
        } catch (NumberFormatException e)
        {
            return false;
        }
    }

    public static int calculateExpandedLevel(Instruction instruction, int currentLevel)
    {
        if (currentLevel == -1)
        {
            if (instruction == null)
            {
                throw new IllegalArgumentException("Instruction cannot be null");
            }

            // Basic instructions have expand level 0
            if (instruction.getType() == CommandType.BASIC)
            {
                return 0;
            }

            // For synthetic instructions, we need to expand and calculate recursively
            List<Instruction> expanded = instruction.expand(new HashMap<>(), 0);

            int maxSubLevel = getMaxExpandLevel(expanded);

            return 1 + maxSubLevel;
        }
        return currentLevel;
    }

    public static int getMaxExpandLevel(List<Instruction> instructions)
    {
        int maxLevel = 0;
        for (Instruction instr : instructions)
        {
            maxLevel = Math.max(maxLevel, instr.getExpandLevel());
        }
        return maxLevel;
    }

}
