package logic.manager;

import dto.server.UserDTO;
import engine.core.ExecutionStatistics;
import logic.User;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class UserManager {
    // region data structures
    private final @NotNull Map<String, User> users = new LinkedHashMap<>();
    // endregion
    // region read-write locks
    private final ReadWriteLock usersLock = new ReentrantReadWriteLock();
    private final Lock writeLock = usersLock.writeLock();
    private final Lock readLock = usersLock.readLock();

    /**
     * Get all users in the system.
     * return an order set based on insertion order (the order in which users were registered)
     *
     * @return a set of all user DTOs
     */
    public @NotNull Set<UserDTO> getAllUsersDTO() {
        readLock.lock();
        try {
            return users.values().stream()
                    .map(User::getUserDTO)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Private constructor to prevent instantiation from outside the class.
     */
    private UserManager() {
    }

    /**
     * Provides the singleton instance of the manager.
     *
     * @return The single instance of UserManager.
     */
    public static UserManager getInstance() {
        return UserManagerHolder.INSTANCE;
    }
    // endregion
    // region user management methods
    /**
     * Add a new user to the system. keeps the order of insertion (users registration order).
     *
     * @param username the username of the new user, this will be the ID for the user
     * @return the DTO of the newly created user
     */
    public @NotNull UserDTO addUser(String username) {
        writeLock.lock();
        try {
            User user = new User(username);
            users.put(username, user);
            return user.getUserDTO();
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Get a user by username.
     * @param userName the username of the user to retrieve
     * @return the User object if found, null otherwise
     */
    public User getUser(String userName) {
        readLock.lock();
        try {
            return users.get(userName);
        } finally {
            readLock.unlock();
        }
    }

    public boolean isUserExists(String userName) {
        readLock.lock();
        try {
            return users.containsKey(userName);
        } finally {
            readLock.unlock();
        }
    }

    public @NotNull List<ExecutionStatistics> getUserStatisticsDTO(String username) {
        readLock.lock();
        try {
            User user = users.get(username);
            ExecutionHistoryManager ehm = ExecutionHistoryManager.getInstance();
            if (user != null) {
                return ehm.getUserExecutionHistory(username);
            } else {
                throw new IllegalArgumentException("User not found: " + username);
            }
        } finally {
            readLock.unlock();
        }
    }

    // endregion
    // region singleton pattern
    private static class UserManagerHolder {
        private static final UserManager INSTANCE = new UserManager();
    }
    // endregion
}
