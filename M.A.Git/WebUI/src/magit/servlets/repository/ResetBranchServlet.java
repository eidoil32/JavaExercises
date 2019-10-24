package magit.servlets.repository;

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

@WebServlet(name = "ResetBranchServlet", urlPatterns = {"/reset_branch"})
public class ResetBranchServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String  username = request.getParameter("user_id"),
                repositoryID = request.getParameter("repository_id"),
                commit_SHAONE = request.getParameter("selected_commit_shaone");

        User user = WebUI.getUser(request, username);
        try {
            Magit magit = user.getRepository(Integer.parseInt(repositoryID));
            magit.resetBranch(magit.getCurrentBranch(), commit_SHAONE);
        } catch (RepositoryException | MyFileException e) {
            response.sendError(400, e.getMessage());
        }
    }
}
