package dto.engine;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * A Data Transfer Object (DTO) representing the result of a debug state change action.
 * This DTO contains the current values of all variables in the program being debugged
 * and a flag indicating whether the program execution has finished.
 *
 * @param allVarsValue A sorted variables map, output and then arguments and work variables. (<strong>y, x1,x2,...,
 *                     z1,z2,...</strong>)
 *                     where the key is the variable name and the value is its integer value.
 * @param debugPC      A pc counter telling us where we stopped after the debug action.
 * @param debugCycles  The number of cycles executed until the current debug state.
 * @param isFinished   A boolean flag indicating whether the program execution has finished.
 */
public record DebugStateChangeResultDTO(@NotNull Map<String, Integer> allVarsValue, int debugPC, int debugCycles,
                                        int creditLeft,
                                        boolean isFinished) {
}
