package dto.engine;

import java.util.Map;

public record InstructionDTO(String type,
                             String label,
                             String command,
                             int cycles,
                             Map<InstructionDTO, Integer> derivedFromInstructions)
{
}
