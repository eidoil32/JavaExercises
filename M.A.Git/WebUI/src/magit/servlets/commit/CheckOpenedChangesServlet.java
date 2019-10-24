package magit.servlets.commit;

import exceptions.MyFileException;
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

@WebServlet(name = "CheckOpenedChangesServlet", urlPatterns = {"/check_opened_changes"})
public class CheckOpenedChangesServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String  username = request.getParameter("user_id"),
                repositoryID = request.getParameter("repository_id");

        User user = WebUI.getUser(request, username);
        try (PrintWriter out = response.getWriter()){
            Magit magit = user.getRepository(Integer.parseInt(repositoryID));
            if (magit.getCurrentRepository().scanRepository(magit.getCurrentUser()) == null) {
                out.print(true);
            } else {
                out.print(false);
            }
        } catch (RepositoryException | MyFileException e) {
            response.sendError(400, e.getMessage());
        }
    }
}
