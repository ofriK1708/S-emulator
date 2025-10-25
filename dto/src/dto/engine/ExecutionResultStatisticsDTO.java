package dto.engine;

import engine.utils.ArchitectureType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * A Data Transfer Object (DTO) that encapsulates statistical information and metadata about the execution result of a
 * program.
 *
 * @param runNumber        The run number of the execution.
 * @param isMainProgram    Indicates if the executed program is the main program.
 * @param displayName      The name of the executed program.
 * @param architectureType The architecture type used during execution.
 * @param expandLevel      The level of expansion used during execution.
 * @param output           The output value produced by the program.
 * @param cycleCount       The number of cycles taken during execution.
 */
public record ExecutionResultStatisticsDTO(int runNumber,
                                           boolean isMainProgram,
                                           String displayName,
                                           ArchitectureType architectureType,
                                           int expandLevel,
                                           int output,
                                           int cycleCount) {
    /**
     * Creates an instance of ExecutionResultStatisticsDTO from a FullExecutionResultDTO.
     *
     * @param dto The FullExecutionResultDTO containing execution result details.
     * @return An instance of ExecutionResultStatisticsDTO with relevant data extracted from the provided DTO.
     */
    @Contract("_, _ -> new")
    public static @NotNull ExecutionResultStatisticsDTO of(@NotNull FullExecutionResultDTO dto, int runNumber) {
        return new ExecutionResultStatisticsDTO(
                runNumber,
                dto.isMainProgram(),
                dto.programName(),
                dto.architectureType(),
                dto.expandLevel(),
                dto.output(),
                dto.cycleCount()
        );
    }
}
