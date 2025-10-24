package logic.manager;

import logic.ProgramState;
import logic.ProgramStatus;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutionManager {
    private final ExecutorService threadPool;
    private final ConcurrentHashMap<String, ProgramStatus> executions;

    public ExecutionManager(int poolSize) {
        this.threadPool = Executors.newFixedThreadPool(poolSize);
        this.executions = new ConcurrentHashMap<>();
    }

    public String submitProgram(Runnable programTask, String id) {
        ProgramStatus status = new ProgramStatus(id);
        executions.put(id, status);

        threadPool.submit(() -> {
            status.setState(ProgramState.IN_PROGRESS);
            try {
                programTask.run();
                status.setState(ProgramState.DONE);
            } catch (Exception e) {
                status.setState(ProgramState.FAILED);
                status.setErrorMessage(e.getMessage());
            }
        });

        return id;
    }

    public ProgramStatus getStatus(String id) {
        return executions.get(id);
    }
}
