package core;

import dto.engine.ExecutionStatisticsDTO;

import java.util.List;

public class ExecutionStatistics
{
    private final int executionNumber;
    private final int levelOfExpansion;
    private final List<Integer> argumentsValues;
    private int Y = 0;
    private int numOfCycles = 0;

    public ExecutionStatistics(int executionNumber, int levelOfExpansion, List<Integer> argumentsValues)
    {
        this.executionNumber = executionNumber;
        this.levelOfExpansion = levelOfExpansion;
        this.argumentsValues = argumentsValues;
    }

    public void setY(int y)
    {
        Y = y;
    }

    public void incrementCycles(int numOfCycles)
    {
        this.numOfCycles += numOfCycles;
    }

    public ExecutionStatisticsDTO toDTO()
    {
        return new ExecutionStatisticsDTO(executionNumber, levelOfExpansion, argumentsValues, Y, numOfCycles);
    }
}
