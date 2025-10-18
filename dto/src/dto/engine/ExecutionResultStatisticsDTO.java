package dto.engine;

import engine.utils.ArchitectureType;

public record ExecutionResultStatisticsDTO(boolean isMainProgram,
                                           String programName,
                                           ArchitectureType architectureType,
                                           int expandLevel,
                                           int output,
                                           int cycleCount) {
    public static ExecutionResultStatisticsDTO of(ExecutionResultDTO dto) {
        return new ExecutionResultStatisticsDTO(
                dto.isMainProgram(),
                dto.programName(),
                dto.architectureType(),
                dto.expandLevel(),
                dto.output(),
                dto.cycleCount()
        );
    }
}
