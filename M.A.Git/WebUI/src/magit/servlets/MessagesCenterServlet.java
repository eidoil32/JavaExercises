package magit.servlets;

import settings.Settings;
import usermanager.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "MessagesCenterServlet", urlPatterns = {"/check_messages"})
public class MessagesCenterServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute(Settings.WSA_USER);
        try (PrintWriter out = response.getWriter()) {
            String messages = user.readMessages();
            out.print(messages);
        }
    }
}
