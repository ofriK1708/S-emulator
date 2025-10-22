package engine.core;

import engine.utils.ArchitectureType;

import java.util.HashMap;
import java.util.Map;

public class ExecutionStatistics {
    int creditCost = 0;
    int cycleCount = 0;
    int numberOfInstructionsExecuted = 0;
    Map<ArchitectureType, Integer> instructionsPerArchitecture = new HashMap<>();

    public int getCreditCost() {
        return creditCost;
    }

    void setCreditCost(int creditCost) {
        this.creditCost = creditCost;
    }

    int getCycleCount() {
        return cycleCount;
    }

    void setCycleCount(int cycleCount) {
        this.cycleCount = cycleCount;
    }

    public int getNumberOfInstructionsExecuted() {
        return numberOfInstructionsExecuted;
    }

    void incrementNumberOfInstructionsExecuted() {
        this.numberOfInstructionsExecuted++;
    }

    public Map<ArchitectureType, Integer> getInstructionsPerArchitecture() {
        return instructionsPerArchitecture;
    }

    void incrementInstructionsForArchitecture(ArchitectureType architectureType) {
        this.instructionsPerArchitecture.put(architectureType,
                this.instructionsPerArchitecture.getOrDefault(architectureType, 0) + 1);
    }
}
