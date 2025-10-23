package dto.engine;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object representing a program.
 *
 * @param ProgramName    the name of the program
 * @param maxExpandLevel the maximum expansion level of the program
 * @param instructions   the list of instructions in the program
 * @see InstructionDTO
 */
public record ProgramDTO(@NotNull String ProgramName,
                         @NotNull Map<String, Integer> arguments,
                         int maxExpandLevel,
                         @NotNull List<InstructionDTO> instructions) {
}
