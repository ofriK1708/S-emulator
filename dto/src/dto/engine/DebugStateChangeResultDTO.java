package dto.engine;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record DebugStateChangeResultDTO(@NotNull Map<String, Integer> allVarsValue, boolean isFinished) {
}
