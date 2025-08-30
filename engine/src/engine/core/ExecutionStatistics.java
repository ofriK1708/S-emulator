package engine.core;

import dto.engine.ExecutionStatisticsDTO;
import engine.utils.ProgramUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public class ExecutionStatistics implements Serializable
{
    @Serial
    private static final long serialVersionUID = 1L;
    private final int executionNumber;
    private final int expandLevel;
    private final List<Integer> argumentsValues;
    private int Y = 0;
    private int numOfCycles = 0;

    public ExecutionStatistics(int executionNumber, int expandLevel, List<Integer> argumentsValues)
    {
        this.executionNumber = executionNumber;
        this.expandLevel = expandLevel;
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
        return new ExecutionStatisticsDTO(executionNumber,
                expandLevel,
                ProgramUtils.getArgsMapFromValuesList(argumentsValues),
                Y,
                numOfCycles);
    }
}
