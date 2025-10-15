package dto.engine;

import java.util.Map;

public record ExecutionResult(Map<String, Integer> arguments,
                              Map<String, Integer> variables,
                              int output,
                              int expandLevel,
                              int cycleCount) {
}
