package core;

import java.util.List;
import java.util.Map;

public interface Command
{
    void execute(Map<String, Integer> contextMap) throws IllegalArgumentException;
    int getCycles();
    CommandType getType();

    int getExpandLevel();
    List<Instruction> expand(Map<String, Integer> contextMap, int originalInstructionIndex);
    int getNumberOfArgs(Map<String, Integer> contextMap);
}
