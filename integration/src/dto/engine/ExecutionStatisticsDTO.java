package dto.engine;

import java.util.List;

public record ExecutionStatisticsDTO(int executionNumber,
                                     int levelOfExpansion,
                                     List<Integer> arguments,
                                     int result,
                                     int cyclesUsed
)
{
}
