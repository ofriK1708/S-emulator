package dto.engine;

import java.util.List;
import java.util.Map;

public record ProgramDTO(String ProgramName,
                         Map<String, Integer> arguments,
                         List<String> labels,
                         List<InstructionDTO> instructions)
{
}
