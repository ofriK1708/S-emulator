package dto.engine;

import engine.core.ExecutionMetadata;
import engine.core.ExecutionResultValues;
import engine.core.ExecutionStatistics;
import org.jetbrains.annotations.NotNull;

/**
 * A record that encapsulates the execution result information of a program, including metadata,
 * result values, and execution statistics.
 *
 * @param executionMetadata     metadata about the execution such as program name, architecture type, etc.
 * @param executionResultValues the result values obtained from the execution. (output, arguments, work variables).
 * @param executionStatistics   statistics related to the execution such as cycle count and credits cost.
 * @see ExecutionMetadata
 * @see ExecutionResultValues
 * @see ExecutionStatistics
 */
public record ExecutionResultInfoDTO(@NotNull ExecutionMetadata executionMetadata,
                                     @NotNull ExecutionResultValues executionResultValues,
                                     @NotNull ExecutionStatistics executionStatistics) {

//    public static ExecutionResultInfoDTO from(ExecutionResultValues values, boolean isMainProgram,
//                                              String programName, ArchitectureType architectureType) {
//        return new ExecutionResultInfoDTO(
//                isMainProgram,
//                programName,
//                architectureType,
//                values.arguments(),
//                values.workVariables(),
//                values.output(),
//                values.expandLevel(),
//                values.cycleCount(),
//                values.creditsCost()
//        );
//    }
}
