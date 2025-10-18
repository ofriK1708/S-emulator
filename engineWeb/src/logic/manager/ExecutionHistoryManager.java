package logic.manager;

public class ExecutionHistoryManager {
    private record ExecutionRecord(
            int runNumber,
            boolean isMainProgram,
            String name,
            String architectureType,
            String executionLevel,
            int outputValue,
            int cyclesUsed
    ) {
    }
}
