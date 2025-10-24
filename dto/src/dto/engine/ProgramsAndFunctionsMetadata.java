package dto.engine;

import java.util.Set;

public record ProgramsAndFunctionsMetadata(Set<ProgramMetadata> mainPrograms,
                                           Set<FunctionMetadata> functions) {
}
