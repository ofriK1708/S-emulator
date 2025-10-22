package dto.server;

import dto.engine.FunctionMetadataDTO;
import dto.engine.ProgramMetadataDTO;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public record LoadProgramResultDTO(@NotNull Set<ProgramMetadataDTO> programs,
                                   @NotNull Set<FunctionMetadataDTO> functions) {
}
