package dto.engine;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Metadata information about a function.
 *
 * @param name              The internal name of the function.
 * @param displayName       The user-friendly display name of the function.
 * @param ProgramContext    The context or environment in which the function operates.
 * @param uploadedBy        The user who uploaded the function.
 * @param functionsToRun    A set of sub-function names that this function calls.
 * @param numOfInstructions The number of instructions in the function.
 * @param maxExpandLevel    The maximum level of expansion for the function.
 */
public record FunctionMetadata(@NotNull String name,
                               @NotNull String displayName,
                               @NotNull String ProgramContext,
                               @NotNull String uploadedBy,
                               @NotNull Set<String> functionsToRun,
                               int numOfInstructions,
                               int maxExpandLevel) {
}
