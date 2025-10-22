package logic.manager;

import engine.core.ExecutionStatistics;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ExecutionHistoryManager {

    // region data structures
    private final @NotNull List<ExecutionStatistics> executionResultDTOList = new ArrayList<>();
    private final @NotNull Map<String, List<ExecutionStatistics>> userToExecutionHistory = new HashMap<>();
    private final @NotNull Map<String, List<ExecutionStatistics>> programToExecutionHistory = new HashMap<>();
    // endregion

    // region read-write locks
    private final @NotNull ReadWriteLock executionHistoryLock = new ReentrantReadWriteLock();
    private final @NotNull Lock readLock = executionHistoryLock.readLock();
    private final @NotNull Lock writeLock = executionHistoryLock.writeLock();
    // endregion

    // region singleton pattern
    private ExecutionHistoryManager() {
    }

    private static class ExecutionHistoryManagerHolder {
        private static final ExecutionHistoryManager INSTANCE = new ExecutionHistoryManager();
    }

    /**
     * Provides the singleton instance of the manager.
     *
     * @return The single instance of ExecutionHistoryManager.
     */
    public static ExecutionHistoryManager getInstance() {
        return ExecutionHistoryManagerHolder.INSTANCE;
    }
    // endregion

    // region execution history management methods

    public void addExecutionResult(String username, String programName,
                                   ExecutionStatistics executionStatistics) {
        writeLock.lock();
        try {
            executionResultDTOList.add(executionStatistics);

            userToExecutionHistory
                    .computeIfAbsent(username, k -> new ArrayList<>())
                    .add(executionStatistics);

            programToExecutionHistory
                    .computeIfAbsent(programName, k -> new ArrayList<>())
                    .add(executionStatistics);
        } finally {
            writeLock.unlock();
        }
    }

    public @NotNull List<ExecutionStatistics> getExecutionResultDTOList() {
        readLock.lock();
        try {
            return executionResultDTOList;
        } finally {
            readLock.unlock();
        }
    }

    public @NotNull List<ExecutionStatistics> getUserExecutionHistory(String username) {
        readLock.lock();
        try {
            if (!userToExecutionHistory.containsKey(username)) {
                throw new IllegalArgumentException("No execution history for user: " + username);
            }
            return userToExecutionHistory.get(username);
        } finally {
            readLock.unlock();
        }
    }

    public @NotNull List<ExecutionStatistics> getProgramExecutionHistory(String programName) {
        readLock.lock();
        try {
            if (!programToExecutionHistory.containsKey(programName)) {
                throw new IllegalArgumentException("No execution history for program: " + programName);
            }
            return programToExecutionHistory.get(programName);
        } finally {
            readLock.unlock();
        }
    }
    // endregion

}
