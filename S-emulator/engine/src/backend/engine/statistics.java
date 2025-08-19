package backend.engine;

import java.util.LinkedList;

public class statistics
{
    private int executionCount = 0;
    private int levelOfExpansion = 0;
    private LinkedList<Integer> argumentsValues = new LinkedList<>();
    private int Y = 0;
    private int numOfCycles = 0;

    public statistics() {}

    public statistics(int levelOfExpansion, LinkedList<Integer> argumentsValues)
    {
        this.levelOfExpansion = levelOfExpansion;
        this.argumentsValues = argumentsValues;
    }
    public void incrementExecutionCount()
    {
        executionCount++;
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
                "executionCount=" + executionCount +
                ", levelOfExpansion=" + levelOfExpansion +
                ", argumentsValues=" + argumentsValues +
                ", Y=" + Y +
                ", numOfCycles=" + numOfCycles +
                '}';
    }
}
