package backend.engine;

import java.util.List;
import java.util.Map;

public interface Command
{
    void execute(Map<String, Integer> contextMap) throws IllegalArgumentException;
    int getCycles();
    CommandType getType();

    List<Instruction> expand(Map<String, Integer> contextMap, int originalInstructionIndex);
    int getNumberOfArgs(Map<String, Integer> contextMap);
    String getDisplayFormat(int instructionNumber);
}
