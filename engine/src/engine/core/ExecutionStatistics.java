package engine.core;

import engine.utils.ArchitectureType;

import java.util.HashMap;
import java.util.Map;

public class ExecutionStatistics {
    int creditCost = 0;
    int cycleCount = 0;
    int numberOfInstructionsExecuted = 0;
    Map<ArchitectureType, Integer> instructionsPerArchitecture = new HashMap<>();

    public ExecutionStatistics() {

    }

    public ExecutionStatistics(int creditCost, int cycleCount, int numberOfInstructionsExecuted,
                               Map<ArchitectureType, Integer> instructionsPerArchitecture) {
        this.creditCost = creditCost;
        this.cycleCount = cycleCount;
        this.numberOfInstructionsExecuted = numberOfInstructionsExecuted;
        this.instructionsPerArchitecture = instructionsPerArchitecture;
    }

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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        int numberOfInstructionsExecuted = 0;
        Map<ArchitectureType, Integer> instructionsPerArchitecture = new HashMap<>();
        private int creditCost;
        private int cycleCount;

        public Builder creditCost(int creditCost) {
            this.creditCost = creditCost;
            return this;
        }

        public Builder cycleCount(int cycleCount) {
            this.cycleCount = cycleCount;
            return this;
        }

        public Builder numberOfInstructionsExecuted(int numberOfInstructionsExecuted) {
            this.numberOfInstructionsExecuted = numberOfInstructionsExecuted;
            return this;
        }

        public Builder instructionsPerArchitecture(Map<ArchitectureType, Integer> instructionsPerArchitecture) {
            this.instructionsPerArchitecture = instructionsPerArchitecture;
            return this;
        }

        public ExecutionStatistics build() {
            return new ExecutionStatistics(creditCost, cycleCount, numberOfInstructionsExecuted,
                    instructionsPerArchitecture);
        }
    }
}
