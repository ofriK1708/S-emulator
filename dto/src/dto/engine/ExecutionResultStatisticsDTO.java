package dto.engine;

import engine.utils.ArchitectureType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

import static engine.utils.ProgramUtils.OUTPUT_NAME;

/**
 * A Data Transfer Object (DTO) that encapsulates statistical information and metadata about the execution result of a
 * program.
 *
 * @param runNumber        The run number of the execution.
 * @param isMainProgram    Indicates if the executed program is the main program.
 * @param innerName      The name of the executed program.
 * @param architectureType The architecture type used during execution.
 * @param expandLevel      The level of expansion used during execution.
 * @param output           The output value produced by the program.
 * @param allVarsSorted    A map of all variable names to their values, sorted with output first,
 *                         followed by arguments and work variables. sorted by their numerical suffixes.
 *                         (y,x1,x2,...z1,z2,...)
 * @param arguments        A map of argument names to their values.
 * @param cycleCount       The number of cycles taken during execution.
 */
public record ExecutionResultStatisticsDTO(int runNumber,
                                           boolean isMainProgram,
                                           @NotNull String innerName,
                                           @NotNull String displayName,
                                           @NotNull ArchitectureType architectureType,
                                           int expandLevel,
                                           int output,
                                           @NotNull Map<String, Integer> allVarsSorted,
                                           @NotNull Map<String, Integer> arguments,
                                           int cycleCount) {
    /**
     * Creates an instance of ExecutionResultStatisticsDTO from a FullExecutionResultDTO.
     *
     * @param dto The FullExecutionResultDTO containing execution result details.
     * @return An instance of ExecutionResultStatisticsDTO with relevant data extracted from the provided DTO.
     */
    @Contract("_, _ -> new")
    public static @NotNull ExecutionResultStatisticsDTO of(@NotNull FullExecutionResultDTO dto, int runNumber) {
        Map<String, Integer> allVarsSorted = new LinkedHashMap<>();
        allVarsSorted.put(OUTPUT_NAME, dto.output());
        allVarsSorted.putAll(dto.arguments());
        allVarsSorted.putAll(dto.workVariables());

        return new ExecutionResultStatisticsDTO(
                runNumber,
                dto.isMainProgram(),
                dto.innerName(),
                dto.displayName(),
                dto.architectureType(),
                dto.expandLevel(),
                dto.output(),
                allVarsSorted,
                dto.arguments(),
                dto.cycleCount()
        );
    }
}
