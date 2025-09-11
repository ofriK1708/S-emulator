package dto.engine;

import java.util.Map;

public record ExecutionResultDTO(int result, Map<String, Integer> argumentsValues,
                                 Map<String, Integer> workVariablesValues,
                                 int executionResult,
                                 int numOfCycles)
{
}
