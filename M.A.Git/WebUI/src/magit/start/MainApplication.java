package magit.start;

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

@WebServlet(name = "MainApplication", urlPatterns = {"/mainPage"})
public class MainApplication extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
        if (req.getParameter("x").equals("username")) {
            HttpSession session = req.getSession();
            User user = (User) session.getAttribute(Settings.WSA_USER);
            if (user == null) {
                printError(response, "0001");
            } else {
                printError(response, user.getName());
            }
        }
    }

    private void printError(HttpServletResponse response, String message) {
        try (PrintWriter out = response.getWriter()) {
            out.print(message);
        } catch (IOException ignored) {}
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("in post");
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("application/json");
        System.out.println("Index html test!");
    }
}
