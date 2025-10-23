package dto.engine;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * A Data Transfer Object (DTO) representing the result of a debug state change action.
 * This DTO contains the current values of all variables in the program being debugged
 * and a flag indicating whether the program execution has finished.
 *
 * @param allVarsValue A map containing variable names as keys and their corresponding integer values.
 * @param isFinished   A boolean flag indicating whether the program execution has finished.
 */
public record DebugStateChangeResultDTO(@NotNull Map<String, Integer> allVarsValue, boolean isFinished) {
}
