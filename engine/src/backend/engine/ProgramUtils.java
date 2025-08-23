package backend.engine;

import java.util.Map;

public class ProgramUtils
{
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
    /*public static int calculateExpandedLevel(Instruction instruction)
    {
        // 'this' is not available in static context, so pass a prototype or use a factory if needed
        List<Instruction> expanded = instruction.expand();
        int maxSubLevel = 0;
        for (Instruction instr : expanded)
        {
            maxSubLevel = Math.max(maxSubLevel, instr.getExpandLevel());
        }
        return maxSubLevel;
    }*/
}
