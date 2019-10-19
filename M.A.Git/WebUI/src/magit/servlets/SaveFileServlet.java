package magit.servlets;

import exceptions.RepositoryException;
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

@WebServlet(name = "SaveFileServlet", urlPatterns = {"/save_file"})
public class SaveFileServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getParameter("file_path"),
                repositoryID = request.getParameter("repository_id"),
                content = request.getParameter("file_content"),
                username = request.getParameter("user_id");

        User user = WebUI.getUser(request, username);
        try (PrintWriter out = response.getWriter()) {
            Magit magit = user.getRepository(Integer.parseInt(repositoryID));
            magit.updateFileContent(path, content);
        } catch (RepositoryException ignored) { }
    }
}
