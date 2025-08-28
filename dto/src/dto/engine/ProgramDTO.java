package dto.engine;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record ProgramDTO(String ProgramName,
                         Map<String, Integer> arguments,
                         Set<String> labels,
                         List<InstructionDTO> instructions)
{
}
