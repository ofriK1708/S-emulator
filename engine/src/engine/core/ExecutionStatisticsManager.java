package engine.core;

import dto.engine.ExecutionResult;
import dto.engine.ExecutionStatisticsDTO;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExecutionStatisticsManager {
    List<ExecutionStatistics> executionStatisticsList = new ArrayList<>();

    private int getNextExecutionNumber() {
        return executionStatisticsList.size() + 1;
    }

    public void addExecutionStatistics(ExecutionResult result, @NotNull Map<String, Integer> arguments) {
        int executionNumber = getNextExecutionNumber();
        executionStatisticsList.add(new ExecutionStatistics(
                executionNumber,
                result.expandLevel(),
                arguments,
                result.output(),
                result.cycleCount()));
    }

    public void addExecutionStatistics(int expandLevel, @NotNull Map<String, Integer> arguments,
                                       int result, int cycles) {
        int executionNumber = getNextExecutionNumber();
        executionStatisticsList.add(new ExecutionStatistics(
                executionNumber,
                expandLevel,
                arguments,
                result,
                cycles));
    }

    public List<ExecutionStatisticsDTO> getExecutionStatisticsDTOList() {
        List<ExecutionStatisticsDTO> dtoList = new ArrayList<>();
        for (ExecutionStatistics stats : executionStatisticsList) {
            dtoList.add(stats.toDTO());
        }
        return dtoList;
    }

    public ExecutionStatisticsDTO getLastExecutionStatisticsDTO() {
        if (executionStatisticsList.isEmpty()) {
            return null;
        }
        return executionStatisticsList.getLast().toDTO();
    }

    public int getLastExecutionCycles() {
        if (executionStatisticsList.isEmpty()) {
            return 0;
        }
        return executionStatisticsList.getLast().numOfCycles;
    }

    public int getExecutionCount() {
        return executionStatisticsList.size();
    }

    private static class ExecutionStatistics {
        private final int executionNumber;
        private final int expandLevel;
        private final @NotNull Map<String, Integer> arguments;
        private int Y = 0;
        private int numOfCycles = 0;


        public ExecutionStatistics(int executionNumber, int expandLevel, @NotNull Map<String, Integer> arguments,
                                   int result, int cycles) {
            this.executionNumber = executionNumber;
            this.expandLevel = expandLevel;
            this.arguments = new HashMap<>(arguments);
            this.Y = result;
            this.numOfCycles = cycles;
        }

        public @NotNull ExecutionStatisticsDTO toDTO() {
            return new ExecutionStatisticsDTO(executionNumber,
                    expandLevel,
                    new HashMap<>(arguments),
                    Y,
                    numOfCycles);
        }
    }
}
