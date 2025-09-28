package dto.engine;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public record ProgramDTO(@NotNull String ProgramName,
                         Set<String> arguments,
                         Set<String> labels,
                         @NotNull List<InstructionDTO> instructions)
{
}
