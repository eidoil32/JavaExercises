package magit.servlets;

import com.google.gson.Gson;
import exceptions.RepositoryException;
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
import java.util.Map;

@WebServlet(name = "SingleUserServlet", urlPatterns = {"/single_user"})
public class SingleUserServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("doGet: in single user servlet");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        getSingleUserDetails(req, resp);
    }

    private void getSingleUserDetails(HttpServletRequest request, HttpServletResponse response) {
        String username = request.getParameter(Settings.WSA_USERNAME_KEY);
        User selected = WebUI.findUser(username);

        if (selected != null) {
            Gson gson = new Gson();

            try (PrintWriter out = response.getWriter()) {
                Map<Integer, Map<String, String>> responseMap = selected.getRepositoriesMap();
                String json = gson.toJson(responseMap);
                out.print(json);
            } catch (IOException | RepositoryException ignored) { }
        }
    }
}