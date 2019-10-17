package magit;

import exceptions.MyWebException;
import settings.Settings;
import usermanager.User;
import usermanager.UserManager;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.Set;

@WebListener
public class WebUI implements ServletContextListener {
    private static UserManager userManager = new UserManager();

    synchronized public static void createRepositoryData(User user, HttpServletRequest request, Magit magit, int repository_count) {
        File userFolder = new File(Settings.USERS_FOLDER + File.separator + user);
        request.getSession().setAttribute(Settings.WSA_REPOSITORIES_NUMBER, repository_count);
    }

    synchronized public static Set<User> getUsers() {
        return userManager.getUsers();
    }

    public static void createUserFolder(String username) {
        new File(Settings.USERS_FOLDER + File.separator + username).mkdir();
    }

    public static User getUser(HttpServletRequest req, String userName) {
        User user;
        if (userName.equals(Settings.NULL_STRING)) {
            user = (User) req.getSession().getAttribute(Settings.WSA_USER);
        } else {
            user = WebUI.findUser(userName);
        }

        return user;
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        new File(Settings.SERVER_DATABASE).mkdir();
        new File(Settings.USERS_FOLDER).mkdir();
    }

    synchronized public static boolean checkUserAlreadyExist(String username) {
        return userManager.checkUserAlreadyExist(username);
    }

    synchronized public static void addUser(String username, String password, HttpSession session) throws MyWebException {
        User user = userManager.addUser(username, password);
        session.setAttribute(Settings.WSA_USER, user);
    }

    synchronized public static User findUser(String username) {
        return userManager.findUser(username);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        System.out.println("Start deleting files...");
        deleteFolder(new File(Settings.SERVER_DATABASE));
        System.out.println("Finish deleting files, good bye!");
    }

    private void deleteFolder(File root_folder) {
        File[] files = root_folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteFolder(file);
                } else {
                    file.delete();
                }
            }
        }
        root_folder.delete();
    }
}
