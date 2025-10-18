package logic.manager;

import dto.server.UserDTO;
import logic.User;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class UserManager {
    @NotNull
    private final ReadWriteLock usersLock = new ReentrantReadWriteLock();
    @NotNull
    private final Lock writeLock = usersLock.writeLock();
    @NotNull
    private final Lock readLock = usersLock.readLock();
    @NotNull
    Map<String, User> users = new LinkedHashMap<>();

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

    /**
     * Get all users in the system.
     * return an order set based on insertion order (the order in which users were registered)
     *
     * @return a set of all user DTOs
     */
    public @NotNull Set<UserDTO> getAllUsers() {
        readLock.lock();
        try {
            return users.values().stream()
                    .map(User::getUserDTO)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        } finally {
            readLock.unlock();
        }
    }
}
