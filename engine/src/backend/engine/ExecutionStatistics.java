package backend.engine;

import java.util.LinkedList;

public class ExecutionStatistics
{
    private int executionNumber = 0;
    private int levelOfExpansion = 0;
    private LinkedList<Integer> argumentsValues = new LinkedList<>();
    private int Y = 0;
    private int numOfCycles = 0;

    public ExecutionStatistics(int executionNumber)
    {
        this.executionNumber = executionNumber;
    }

    public ExecutionStatistics(int levelOfExpansion, LinkedList<Integer> argumentsValues)
    {
        this.levelOfExpansion = levelOfExpansion;
        this.argumentsValues = argumentsValues;
    }

    public void setLevelOfExpansion(int levelOfExpansion)
    {
        this.levelOfExpansion = levelOfExpansion;
    }

    public void setArgumentsValues(LinkedList<Integer> argumentsValues)
    {
        this.argumentsValues = argumentsValues;
    }

    public void setY(int y)
    {
        Y = y;
    }

    public void setNumOfCycles(int numOfCycles)
    {
        this.numOfCycles = numOfCycles;
    }

    public void incrementCycles(int numOfCycles)
    {
        this.numOfCycles += numOfCycles;
    }

    @Override
    public String toString()
    {
        return "Statistics{" +
                "executionNumber=" + executionNumber +
                ", levelOfExpansion=" + levelOfExpansion +
                ", argumentsValues=" + argumentsValues +
                ", Y=" + Y +
                ", numOfCycles=" + numOfCycles +
                '}';
    }

    public int getNumOfCycles()
    {
        return numOfCycles;
    }
}
