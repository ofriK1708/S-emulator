package engine.core;

import dto.engine.InstructionDTO;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface Command extends Serializable {
    void execute(Map<String, Integer> contextMap) throws IllegalArgumentException;
    int getCycles();
    CommandType getType();
    int getExpandLevel();
    List<Instruction> expand(Map<String, Integer> contextMap, int originalInstructionIndex);
    InstructionDTO toDTO(int idx);
}
