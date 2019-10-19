package magit.servlets;

import com.google.gson.Gson;
import exceptions.MyFileException;
import exceptions.RepositoryException;
import magit.Magit;
import magit.WebUI;
import settings.Settings;
import usermanager.User;
import utils.Utilities;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

@WebServlet(name = "SingleRepositoryServlet", urlPatterns = {"/single_repository"})
public class SingleRepositoryServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String repository_id = request.getParameter(Settings.WSA_REPOSITORY_ID);
        String username = request.getParameter(Settings.WSA_USERNAME_KEY);
        User currentUser = WebUI.getUser(request, username);

        Magit magit = new Magit();
        try {
            magit.changeRepo(String.format(Settings.USERS_REPOSITORY_FOLDER, currentUser.getName(), repository_id));
        } catch (RepositoryException ignored) {
        }

        try (PrintWriter out = response.getWriter()) {
            Map<String, List<String>> result = magit.getRepositoryMap();
            result.put(Settings.WSA_SINGLE_REPOSITORY_OWNER_NAME,
                    Utilities.createSingleItemList(currentUser.getName()));
            result.put(Settings.WSA_REPOSITORY_LOCATION,
                    Utilities.createSingleItemList(String.format(Settings.USERS_REPOSITORY_FOLDER, currentUser.getName(), repository_id)));

            Gson gson = new Gson();

            String json = gson.toJson(result);
            out.print(json);
        } catch (RepositoryException | MyFileException ignored) { }
    }
}