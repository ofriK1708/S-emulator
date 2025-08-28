package dto.engine;

import java.util.Map;

public record ExecutionStatisticsDTO(int executionNumber,
                                     int expandLevel,
                                     Map<String, Integer> arguments,
                                     int result,
                                     int cyclesUsed
)
{
}
