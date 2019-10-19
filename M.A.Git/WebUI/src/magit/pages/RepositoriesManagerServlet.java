package magit.pages;

import com.google.gson.Gson;
import exceptions.RepositoryException;
import settings.Settings;
import usermanager.User;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@WebServlet(name = "RepositoriesManager", urlPatterns = {"/repositories"})
public class RepositoriesManagerServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType(Settings.APPLICATION_RESPONSE_TYPE);
        HttpSession session = request.getSession();
        Gson gson = new Gson();

        try (PrintWriter out = response.getWriter()) {
            User user = (User) session.getAttribute(Settings.WSA_USER);
            Map<Integer, Map<String, String>> results = user.getRepositoriesMap();
            String json = gson.toJson(results);
            out.print(json);
        } catch (IOException | RepositoryException ignored) {}
    }
}