package logic.manager;

import dto.engine.ExecutionResultStatisticsDTO;
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
    private final @NotNull List<ExecutionResultStatisticsDTO> executionResultDTOList = new ArrayList<>();
    private final @NotNull Map<String, List<ExecutionResultStatisticsDTO>> userToExecutionHistory = new HashMap<>();
    private final @NotNull Map<String, List<ExecutionResultStatisticsDTO>> programToExecutionHistory = new HashMap<>();
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
                                   ExecutionResultStatisticsDTO executionResultStatisticsDTO) {
        writeLock.lock();
        try {
            executionResultDTOList.add(executionResultStatisticsDTO);

            List<ExecutionResultStatisticsDTO> userHistory = userToExecutionHistory.get(username);
            if (userHistory == null) {
                throw new IllegalStateException("User history not initialized for user: " + username);
            }

            userToExecutionHistory
                    .get(username)
                    .add(executionResultStatisticsDTO);

            programToExecutionHistory
                    .computeIfAbsent(programName, k -> new ArrayList<>())
                    .add(executionResultStatisticsDTO);
        } finally {
            writeLock.unlock();
        }
    }

    public void initUserHistory(String username) {
        writeLock.lock();
        try {
            userToExecutionHistory.putIfAbsent(username, new ArrayList<>());
        } finally {
            writeLock.unlock();
        }
    }

    public @NotNull List<ExecutionResultStatisticsDTO> getExecutionResultDTOList() {
        readLock.lock();
        try {
            return executionResultDTOList;
        } finally {
            readLock.unlock();
        }
    }

    public @NotNull List<ExecutionResultStatisticsDTO> getUserExecutionHistory(String username) {
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

    public @NotNull List<ExecutionResultStatisticsDTO> getProgramExecutionHistory(String programName) {
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
