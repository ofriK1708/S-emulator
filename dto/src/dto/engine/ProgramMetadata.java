package dto.engine;

import org.jetbrains.annotations.NotNull;

public record ProgramMetadata(@NotNull String name,
                              @NotNull String uploadedBy,
                              int numOfInstructions,
                              int maxExpandLevel,
                              int numberOfExecutions,
                              float averageCycles) {
}
