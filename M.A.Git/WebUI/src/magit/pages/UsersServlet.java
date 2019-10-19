package magit.pages;

import com.google.gson.Gson;
import magit.WebUI;
import usermanager.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@WebServlet(name = "UsersListServlet", urlPatterns = {"/users"})
public class UsersServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        List<String> result = new LinkedList<>();
        Gson converter = new Gson();

        Set<User> users = WebUI.getUsers();
        for (User user : users) {
            result.add(user.getName());
        }

        String json = converter.toJson(result);
        try (PrintWriter out = response.getWriter()) {
            out.print(json);
            } catch (IOException ignored) {
        }
    }
}
