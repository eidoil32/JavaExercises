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
import java.io.PrintWriter;

@WebServlet(name = "CreateNewFileServlet", urlPatterns = {"/create_file"})
public class CreateNewFileServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        String  username = request.getParameter("user"),
                repositoryID = request.getParameter("repo_id"),
                filename = request.getParameter("file_name"),
                content = request.getParameter("content"),
                path = request.getParameter("path");

        User user = WebUI.getUser(request, username);

        File fileToCreate = new File(String.format(Settings.USERS_REPOSITORY_FOLDER, user.getName(), repositoryID) + File.separator + path + filename);
        try (PrintWriter writer = new PrintWriter(fileToCreate)) {
            writer.write(content);
        } catch (IOException ignored) {

        }
    }
}
