package dto.system;

import dto.engine.FunctionMetadata;
import dto.engine.ProgramMetadata;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public record LoadProgramResultDTO(@NotNull Set<ProgramMetadata> programs,
                                   @NotNull Set<FunctionMetadata> functions) {
}
