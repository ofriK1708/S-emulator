package dto.system;

import dto.engine.FunctionMetadata;
import dto.engine.ProgramMetadata;

import java.util.Set;

public record LoadProgramResultDTO(Set<ProgramMetadata> programs, Set<FunctionMetadata> functions) {
}
