package magit.pages;

import com.google.gson.Gson;
import magit.WebUI;
import settings.Settings;
import usermanager.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "UsersListServlet", urlPatterns = {"/users"})
public class UsersServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        Map<String, String> result = new HashMap<>();
        Gson converter = new Gson();

        List<User> users = WebUI.getUsers();
        for (User user : users) {
            result.put(Settings.WSA_USER_NAME, user.getName());
        }

        String json = converter.toJson(result);
        try (PrintWriter out = response.getWriter()) {
            out.print(json);
            } catch (IOException ignored) {
        }
    }
}
