package magit.servlets.repository;

import exceptions.RepositoryException;
import magit.Magit;
import magit.WebUI;
import settings.Settings;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "PushBranchServlet", urlPatterns = {"/push_branch"})
public class PushBranchServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(Settings.APPLICATION_RESPONSE_TYPE);
        String  username = request.getParameter("user"),
                repositoryID = request.getParameter("repo_id"),
                branchName = request.getParameter("branch_name");

        try {
            Magit magit = WebUI.getUser(request, username).getRepository(Integer.parseInt(repositoryID));
            magit.pushBranch(magit.findBranch(branchName));
        } catch (RepositoryException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print(e.getMessage());
        }
    }
}
