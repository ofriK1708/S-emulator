package dto.engine;

import java.util.List;

public record InstructionDTO(int InstructionNumber,
                             String type,
                             String label,
                             String command,
                             int cycles,
                             List<InstructionDTO> derivedFromInstructions)
{
}
