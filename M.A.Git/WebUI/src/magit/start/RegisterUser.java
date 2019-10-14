package magit.start;

import exceptions.MyWebException;
import magit.WebUI;
import settings.Settings;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "Registration", urlPatterns = {"/register"})
public class RegisterUser extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String  username = request.getParameter("username"),
                password = request.getParameter("password");

        if (WebUI.checkUserAlreadyExist(username)) {
            printError(response, Settings.language.getString("USERNAME_ALREADY_EXIST"));
        } else {
            if (!validUsername(username)) {
                printError(response, Settings.language.getString("USERNAME_CONTAIN_SPECIAL_CHARS"));
            } else {
                try {
                    WebUI.addUser(username, password, session);
                    WebUI.createUserFolder(username);
                    printError(response, "success");
                    System.out.println(String.format("User %s created successfully", username));
                } catch (MyWebException e) {
                    printError(response, e.getMessage());
                }
            }
        }
    }

    private boolean validUsername(String username) {
        if (!new File(username).getName().equals(username)) {
            return false;
        }

        for (char ch : Settings.special_chars) {
            if (username.contains(Character.toString(ch))) {
                return false;
            }
        }

        return true;
    }


    private void printError(HttpServletResponse response, String message) {
        try (PrintWriter out = response.getWriter()) {
            out.print(message);
        } catch (IOException ignored) {}
    }
}
