package logic.manager;

import logic.User;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class UserManager {
    @NotNull
    private final ReadWriteLock usersLock = new ReentrantReadWriteLock();
    @NotNull
    private final Lock writeLock = usersLock.writeLock();
    @NotNull
    private final Lock readLock = usersLock.readLock();
    @NotNull
    Map<String, User> users = new HashMap<>();

    public void addUser(String username) {
        writeLock.lock();
        try {
            User user = new User(username, 0, 0, 0, 0, 0);
            users.put(username, user);
        } finally {
            writeLock.unlock();
        }
    }

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
}
