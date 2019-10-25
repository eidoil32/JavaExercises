package magit.servlets.general;

import magit.WebUI;
import settings.Settings;
import usermanager.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

@WebServlet(name = "DeleteFileServlet", urlPatterns = {"/delete_file"})
public class DeleteFileServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String  username = request.getParameter("user"),
                repositoryID = request.getParameter("repo_id"),
                path = request.getParameter("path");

        User user = WebUI.getUser(request, username);

        File fileToDelete = new File(String.format(Settings.USERS_REPOSITORY_FOLDER, user.getName(), repositoryID) + File.separator + path);
        if (fileToDelete.exists()) {
            fileToDelete.delete();
        }
    }
}
