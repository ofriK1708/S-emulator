package dto.engine;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object representing a program.
 *
 * @param ProgramName    the name of the program
 * @param arguments      the map of argument names to their values
 * @param allVariablesIncludingLabelsNames the list of all variable and label names in the program
 * @param maxExpandLevel the maximum expansion level of the program
 * @param instructions   the list of instructions in the program
 * @see InstructionDTO
 */
public record ProgramDTO(@NotNull String ProgramName,
                         @NotNull Map<String, Integer> arguments,
                         @NotNull List<String> allVariablesIncludingLabelsNames,
                         int maxExpandLevel,
                         @NotNull List<InstructionDTO> instructions) {
}
