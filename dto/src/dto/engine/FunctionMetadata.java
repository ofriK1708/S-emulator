package dto.engine;

import org.jetbrains.annotations.NotNull;

public record FunctionMetadata(@NotNull String name,
                               @NotNull String ProgramContext,
                               @NotNull String uploadedBy,
                               int numOfInstructions,
                               int maxExpandLevel) {
}
