package magit;

import exceptions.MyWebException;
import settings.Settings;
import usermanager.User;
import usermanager.UserManager;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.List;

@WebListener
public class WebUI implements ServletContextListener {
    private static UserManager userManager = new UserManager();

    synchronized public static void createRepositoryData(User user, HttpServletRequest request, Magit magit) {
        File userFolder = new File(Settings.USERS_FOLDER + File.separator + user);

    }

    synchronized public static List<User> getUsers() {
        return userManager.getUsers();
    }

    public static void createUserFolder(String username) {
        new File(Settings.USERS_FOLDER + File.separator + username).mkdir();
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        new File(Settings.SERVER_DATABASE).mkdir();
        new File(Settings.USERS_FOLDER).mkdir();
    }

    synchronized public static boolean checkUserAlreadyExist(String username) {
        return userManager.checkUserAlreadyExist(username);
    }

    synchronized public static void addUser(String username, String password, String sessionID) throws MyWebException {
        userManager.addUser(username, password, sessionID);
    }

    synchronized public static void updateID(User user, String id) {
        userManager.updateID(user, id);
    }

    synchronized public static User findUser(String username) {
        return userManager.findUser(username);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        System.out.println("Stopping application");
    }

}
