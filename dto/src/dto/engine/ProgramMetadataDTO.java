package dto.engine;

import org.jetbrains.annotations.NotNull;

public record ProgramMetadataDTO(@NotNull String name,
                                 @NotNull String uploadedBy,
                                 int numOfInstructions,
                                 int maxExpandLevel,
                                 int numberOfExecutions,
                                 float averageCreditsCost) {
}
