package dto.engine;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Record representing metadata of a program.
 *
 * @param name               The name of the program.
 * @param uploadedBy         The user who uploaded the program.
 * @param SubfunctionNames   A set of subfunction names within the program.
 * @param numOfInstructions  The number of instructions in the program.
 * @param maxExpandLevel     The maximum expansion level of the program.
 * @param numberOfExecutions The total number of times the program has been executed.
 * @param averageCreditsCost The average credits cost for executing the program.
 */
public record ProgramMetadata(@NotNull String name,
                              @NotNull String uploadedBy,
                              @NotNull Set<String> SubfunctionNames,
                              int numOfInstructions,
                              int maxExpandLevel,
                              int numberOfExecutions,
                              float averageCreditsCost) {
}
