package magit.pages;

import com.google.gson.Gson;
import magit.Magit;
import magit.WebUI;
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
import java.util.Set;

@WebServlet(name = "UsersListServlet", urlPatterns = {"/users"})
public class UsersServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Map<String, List<String>>> result = new HashMap<>();
        Gson converter = new Gson();

        List<User> users = WebUI.getUsers();
        for (User user : users) {
            Map<String, List<String>> repositories_data = new HashMap<>();
            Set<Magit> repositories = user.getRepositories();

        }

        String json = converter.toJson(request);
        try (PrintWriter out = response.getWriter()) {
            out.print(json);
        } catch (IOException ignored) {}
    }
}
