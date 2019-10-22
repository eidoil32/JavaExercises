package magit.start;

import settings.Settings;
import usermanager.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "LogoutUserServlet", urlPatterns = {"/logout"})
public class LogoutUserServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            User user = (User) request.getSession().getAttribute(Settings.WSA_USER);
            if (user != null) {
                user.clearMessages();
                request.getSession().removeAttribute(Settings.WSA_USER);
            }
        } catch (IOException ignored) {
        }
    }
}
