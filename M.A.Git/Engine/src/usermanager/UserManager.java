package usermanager;

import exceptions.MyWebException;
import exceptions.eErrorCodes;

import java.util.*;

public class UserManager {
    private Set<User> users;

    public UserManager() {
        users = new HashSet<>();
    }

    public boolean checkUserAlreadyExist(String userName) {
        for (User user : users) {
            if (user.getName().equals(userName)) {
                return true;
            }
        }

        return false;
    }

    public User addUser(String userName, String password) throws MyWebException {
        if (checkUserAlreadyExist(userName)) {
            throw new MyWebException(eErrorCodes.USER_ALREADY_EXIST, userName);
        } else {
            if (password.length() < 2) {
                throw new MyWebException(eErrorCodes.PASSWORD_TOO_SHORT, null);
            }
            User user = new User(userName, password);
            users.add(user);
            return user;
        }
    }

    public User findUser(String username) {
        for (User user : users) {
            if (user.getName().equals(username)) {
                return user;
            }
        }

        return null;
    }

    public Set<User> getUsers() {
        return users;
    }
}
