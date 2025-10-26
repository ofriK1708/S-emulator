package dto.engine;

import engine.utils.ArchitectureType;
import engine.utils.CommandType;

import java.util.List;

/**
 * DTO representing an instruction in a program.
 *
 * @param index                   the index of the instruction
 * @param type                    the type of command (synthetic or simple)
 * @param label                   the label of the instruction, if any
 * @param command                 the command string representing the instruction
 * @param cycles                  the number of cycles the instruction takes to execute
 * @param derivedFromInstructions list of instructions from which this instruction is derived (if any)
 */
public record InstructionDTO(int index,
                             CommandType type,
                             String label,
                             String command,
                             int cycles,
                             ArchitectureType architectureType,
                             List<InstructionDTO> derivedFromInstructions)
{
}
