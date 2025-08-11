package backend.engine;

import java.util.Map;

public class Instruction
{
    protected String mainVarName;
    protected Map<String,Integer> contextMap;
    protected final String PCName = "PC";

    protected Instruction(String mainVarName, Map<String, Integer> contextMap)
    {
        this.mainVarName = mainVarName;
        this.contextMap = contextMap;
    }
}
