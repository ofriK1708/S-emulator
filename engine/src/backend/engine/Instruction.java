package backend.engine;

import java.util.Map;

public class Instruction
{
    protected String mainVarName;
    protected Map<String,String> args;
    protected final String PCName = "PC";

    protected Instruction(String mainVarName, Map<String, String> args)
    {
        this.mainVarName = mainVarName;
        this.args = args;
    }
}
