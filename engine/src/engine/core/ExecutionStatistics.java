package engine.core;

import dto.engine.ExecutionStatisticsDTO;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ExecutionStatistics implements Serializable
{
    @Serial
    private static final long serialVersionUID = 1L;
    private final int executionNumber;
    private final int expandLevel;
    private final @NotNull Map<String, Integer> arguments;
    private int Y = 0;
    private int numOfCycles = 0;


    public ExecutionStatistics(int executionNumber, int expandLevel, @NotNull Map<String, Integer> arguments)
    {
        this.executionNumber = executionNumber;
        this.expandLevel = expandLevel;
        this.arguments = new HashMap<>(arguments);
    }

    public void setY(int y)
    {
        Y = y;
    }

    public void incrementCycles(int numOfCycles)
    {
        this.numOfCycles += numOfCycles;
    }

    public @NotNull ExecutionStatisticsDTO toDTO()
    {
        return new ExecutionStatisticsDTO(executionNumber,
                expandLevel,
                new HashMap<>(arguments),
                Y,
                numOfCycles);
    }
}
