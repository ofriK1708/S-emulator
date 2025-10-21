package dto.engine;

import engine.utils.ArchitectureType;

import java.util.Map;

/**
 * A Data Transfer Object (DTO) that encapsulates the full execution result of a program.
 *
 * @param isMainProgram    Indicates if the executed program is the main program.
 * @param programName      The name of the executed program.
 * @param architectureType The architecture type used during execution.
 * @param arguments        A map of argument (x1,x2,x3,...) names to their integer sorted by their numeric suffixes
 * @param workVariables    A map of work variable (z1,z2,...) names to their integer sorted by their numeric suffixes
 * @param output           The output (y) value produced by the program.
 * @param expandLevel      The level of expansion used during execution.
 * @param cycleCount       The number of cycles taken during execution.
 * @param creditsCost      The cost in credits for executing the program.
 */
public record FullExecutionResultDTO(boolean isMainProgram,
                                     String programName,
                                     ArchitectureType architectureType,
                                     Map<String, Integer> arguments,
                                     Map<String, Integer> workVariables,
                                     int output,
                                     int expandLevel,
                                     int cycleCount,
                                     int creditsCost) {
    public static FullExecutionResultDTO from(ExecutionResultValuesDTO valuesDTO, boolean isMainProgram,
                                              String programName, ArchitectureType architectureType) {
        return new FullExecutionResultDTO(
                isMainProgram,
                programName,
                architectureType,
                valuesDTO.arguments(),
                valuesDTO.workVariables(),
                valuesDTO.output(),
                valuesDTO.expandLevel(),
                valuesDTO.cycleCount(),
                valuesDTO.creditsCost()
        );
    }
}
