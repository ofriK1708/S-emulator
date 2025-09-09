package dto.engine;

import java.util.List;
import java.util.Map;

public record InstructionDTO(int index,
                             String type,
                             String label,
                             String command,
                             int cycles,
                             List<InstructionDTO> derivedFromInstructions)
{
}
