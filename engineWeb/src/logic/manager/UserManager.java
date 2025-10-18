package logic.manager;

import logic.User;

import java.util.Map;

public class UserManager {
    Map<String, User> users;


    public synchronized void addUser(String username) {
        User user = new User(username);
        users.put(username, user);
    }

    public synchronized void addUser(User user) {
        users.put(user.name(), user);
    }

    public synchronized User getUser(String userName) {
        return users.get(userName);
    }

    public synchronized boolean isUserExists(String userName) {
        return users.containsKey(userName);
    }
}
