package dto.engine;

import java.util.List;
import java.util.Set;

public record ProgramDTO(String ProgramName,
                         Set<String> arguments,
                         Set<String> labels,
                         List<InstructionDTO> instructions)
{
}
