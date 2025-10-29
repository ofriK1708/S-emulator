package dto.engine;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * DTO representing the execution result values of a program run.
 *
 * @param output        The output value produced by the program.
 * @param cycleCount    The total number of cycles consumed during execution.
 * @param creditsCost   The total credits cost incurred during execution.
 * @param arguments     A map of argument (x1,x2,x3,...) names to their integer sorted by their numeric suffixes
 * @param workVariables A map of work variable (z1,z2,...) names to their integer sorted by their numeric suffixes
 */
public record ExecutionResultValuesDTO(int output,
                                       int cycleCount,
                                       int creditsCost,
                                       @NotNull Map<String, Integer> arguments,
                                       @NotNull Map<String, Integer> workVariables) {

}
