package backend.engine;

import java.util.HashMap;
import java.util.List;
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
    public static int calculateExpandedLevel(Instruction instruction) {
        if (instruction == null) {
            throw new IllegalArgumentException("Instruction cannot be null");
        }

        // Basic instructions have expand level 0
        if (instruction.getType() == CommandType.BASIC) {
            return 0;
        }

        // For synthetic instructions, we need to expand and calculate recursively
        Map<String, Integer> dummyContext = createDummyContextMap();
        List<Instruction> expanded = instruction.expand(dummyContext, 0);

        int maxSubLevel = 0;
        for (Instruction instr : expanded) {
            maxSubLevel = Math.max(maxSubLevel, instr.getExpandLevel());
        }

        return 1 + maxSubLevel;
    }

    /**
     * Creates a dummy context map for expand level calculations.
     * This map contains some basic variables and labels that might be needed
     * during the expansion process.
     *
     * @return a dummy context map
     */
    private static Map<String, Integer> createDummyContextMap() {
        Map<String, Integer> dummyContext = new HashMap<>();

        // Add some basic variables that are commonly used
        dummyContext.put("PC", 0);
        dummyContext.put("y", 0);

        return dummyContext;
    }
}
