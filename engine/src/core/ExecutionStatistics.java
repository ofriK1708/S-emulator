package core;

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

    public int getExecutionNumber()
    {
        return executionNumber;
    }

    public int getLevelOfExpansion()
    {
        return levelOfExpansion;
    }

    public LinkedList<Integer> getArgumentsValues()
    {
        return argumentsValues;
    }

    public int getY()
    {
        return Y;
    }

    public int getNumOfCycles()
    {
        return numOfCycles;
    }
}
