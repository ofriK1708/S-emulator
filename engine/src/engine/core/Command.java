package engine.core;

import engine.utils.ArchitectureType;
import engine.utils.CommandType;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface Command extends Serializable {
    void execute(Map<String, Integer> contextMap) throws IllegalArgumentException;
    int getCycles();

    @NotNull CommandType getType();
    int getExpandLevel();

    @NotNull List<Instruction> expand(Map<String, Integer> contextMap, int originalInstructionIndex);

    @NotNull ArchitectureType getArchitectureType();

    @NotNull String getStringRepresentation();
}
