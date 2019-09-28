package usermanager;

import exceptions.MyWebException;
import exceptions.eErrorCodes;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class UserManager {
    private Map<String, User> users;

    public UserManager() {
        users = new HashMap<>();
    }

    public boolean checkUserAlreadyExist(String userName) {
        for (User user : users.values()) {
            if (user.getName().equals(userName)) {
                return true;
            }
        }

        return false;
    }

    public void addUser(String userName, String password, String sessionID) throws MyWebException {
        if (checkUserAlreadyExist(userName)) {
            throw new MyWebException(eErrorCodes.USER_ALREADY_EXIST, userName);
        } else {
            if (password.length() < 2) {
                throw new MyWebException(eErrorCodes.PASSWORD_TOO_SHORT, null);
            }
            users.put(sessionID, new User(userName, password, sessionID));
        }
    }

    public User getUser(String id) {
        return users.get(id);
    }

    public User findUser(String username) {
        for (User user : users.values()) {
            if (user.getName().equals(username)) {
                return user;
            }
        }

        return null;
    }

    public void updateID(User user, String id) {
        users.remove(user.getId());
        if (id == null) {
            int i = -1;
            while (users.containsKey(Integer.toString(i))) {
                i--;
            }
            id = Integer.toString(i);
        }
        users.put(id, user);
        user.updateID(id);
    }

    public List<User> getUsers() {
        return new LinkedList<>(users.values());
    }
}
