package dto.engine;

import engine.utils.CommandType;

import java.util.List;

public record InstructionDTO(int index,
                             CommandType type,
                             String label,
                             String command,
                             int cycles,
                             List<InstructionDTO> derivedFromInstructions)
{
}
