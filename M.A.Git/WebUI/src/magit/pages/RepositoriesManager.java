package magit.pages;

import com.google.gson.Gson;
import magit.Magit;
import settings.Settings;
import usermanager.User;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@WebServlet(name = "RepositoriesManager", urlPatterns = {"/pages/repositories"})
public class RepositoriesManager extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("application/json");
        HttpSession session = request.getSession();
        Gson gson = new Gson();
        Map<String, Map<String, List<String>>> results = new HashMap<>();
        User user = (User) session.getAttribute(Settings.WSA_USER);
        Set<Magit> repositories = user.getRepositories();
        int i = 0;
        for (Magit magit : repositories) {
            results.put(createUniqueName(i, magit.getCurrentRepository().getName()), magit.createRepositoryJson());
        }

        session.setAttribute(Settings.WSA_REPOSITORIES, repositories);
        String json = gson.toJson(results);
        System.out.println(json);
        try (PrintWriter out = response.getWriter()) {
            out.print(json);
        } catch (IOException e) {
            System.out.println("Internal error, cannot response to client");
        }
    }

    private String createUniqueName(int i, String name) {
        return i + ") " + name;
    }
}
