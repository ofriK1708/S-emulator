package dto.engine;

import engine.utils.ArchitectureType;

/**
 * A Data Transfer Object (DTO) that encapsulates statistical information and metadata about the execution result of a
 * program.
 *
 * @param isMainProgram    Indicates if the executed program is the main program.
 * @param programName      The name of the executed program.
 * @param architectureType The architecture type used during execution.
 * @param expandLevel      The level of expansion used during execution.
 * @param output           The output value produced by the program.
 * @param cycleCount       The number of cycles taken during execution.
 */
public record ExecutionResultStatisticsDTO(boolean isMainProgram,
                                           String programName,
                                           ArchitectureType architectureType,
                                           int expandLevel,
                                           int output,
                                           int cycleCount) {
    public static ExecutionResultStatisticsDTO of(FullExecutionResultDTO dto) {
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
