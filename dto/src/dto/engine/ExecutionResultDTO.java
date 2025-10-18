package dto.engine;

import engine.utils.ArchitectureType;

import java.util.Map;


public record ExecutionResultDTO(boolean isMainProgram,
                                 String programName,
                                 ArchitectureType architectureType,
                                 Map<String, Integer> arguments,
                                 Map<String, Integer> variables,
                                 int output,
                                 int expandLevel,
                                 int cycleCount) {
    public static ExecutionResultDTO from(ExecutionResultValuesDTO valuesDTO, boolean isMainProgram,
                                          String programName, ArchitectureType architectureType,
                                          int expandLevel, int cycleCount) {
        return new ExecutionResultDTO(
                isMainProgram,
                programName,
                architectureType,
                valuesDTO.arguments(),
                valuesDTO.variables(),
                valuesDTO.output(),
                expandLevel,
                cycleCount
        );
    }
}
