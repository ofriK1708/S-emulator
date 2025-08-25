package mapper.engine;

import core.ExecutionStatistics;
import dto.engine.ExecutionStatisticsDTO;

import java.util.function.Function;

public class ExecutionStatisticsDTOMapper implements Function<ExecutionStatistics, ExecutionStatisticsDTO>
{
    @Override
    public ExecutionStatisticsDTO apply(ExecutionStatistics executionStatistics)
    {
        return new ExecutionStatisticsDTO(
                executionStatistics.getExecutionNumber(),
                executionStatistics.getLevelOfExpansion(),
                executionStatistics.getArgumentsValues(),
                executionStatistics.getY(),
                executionStatistics.getNumOfCycles()
        );
    }
}
