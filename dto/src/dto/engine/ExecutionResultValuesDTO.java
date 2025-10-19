package dto.engine;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record ExecutionResultValuesDTO(int output,
                                       int expandLevel,
                                       int cycleCount,
                                       int creditsCost,
                                       @NotNull Map<String, Integer> arguments,
                                       @NotNull Map<String, Integer> variables) {

    public static ExecutionResultValuesDTO of(ExecutionResultDTO dto) {
        return new ExecutionResultValuesDTO(
                dto.output(),
                dto.expandLevel(),
                dto.cycleCount(),
                dto.creditsCost(),
                dto.arguments(),
                dto.variables()
        );
    }
}
