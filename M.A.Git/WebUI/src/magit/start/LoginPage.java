package magit.start;

import magit.WebUI;
import settings.Settings;
import usermanager.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "LoginPage", urlPatterns = {"/login"})
public class LoginPage extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        userLogin(request, response);
    }

    private void userLogin(HttpServletRequest request, HttpServletResponse response) {
        String username = request.getParameter("username"),
                password = request.getParameter("password");

        HttpSession session = request.getSession();

        if (WebUI.checkUserAlreadyExist(username)) {
            User user = WebUI.findUser(username);
            if (user.credentials(password)) {
                session.setAttribute(Settings.WSA_USER, user);
                printError(response, "success");
            } else {
                printError(response, Settings.language.getString("WRONG_PASSWORD"));
            }
        } else {
            printError(response, String.format(Settings.language.getString("NO_SUCH_USER"), username));
        }
    }

    private void printError(HttpServletResponse response, String message) {
        try (PrintWriter out = response.getWriter()) {
            out.print(message);
        } catch (IOException ignored) {
        }
    }
}
