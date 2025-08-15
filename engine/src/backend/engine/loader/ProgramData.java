package backend.engine.loader;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Data class representing a complete program loaded from XML
 */
public class ProgramData implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String programName;
    private final List<InstructionData> instructions;
    private final Map<String, Integer> labelMap;

    public ProgramData(String programName, List<InstructionData> instructions,
                       Map<String, Integer> labelMap) {
        this.programName = programName;
        this.instructions = instructions;
        this.labelMap = labelMap;
    }

    public String getProgramName() { return programName; }
    public List<InstructionData> getInstructions() { return instructions; }
    public Map<String, Integer> getLabelMap() { return labelMap; }
}