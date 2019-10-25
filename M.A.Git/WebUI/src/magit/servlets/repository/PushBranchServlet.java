package magit.servlets.repository;

import exceptions.RepositoryException;
import magit.Magit;
import magit.WebUI;
import settings.Settings;
import usermanager.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@WebServlet(name = "PushBranchServlet", urlPatterns = {"/push_branch"})
public class PushBranchServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(Settings.APPLICATION_RESPONSE_TYPE);
        String  username = request.getParameter("user"),
                repositoryID = request.getParameter("repo_id"),
                branchName = request.getParameter("branch_name");

        try {
            User user = WebUI.getUser(request, username);
            Magit magit = user.getRepository(Integer.parseInt(repositoryID));
            magit.pushBranch(magit.findBranch(branchName));
            User owner = WebUI.getUser(request,
                    user.getRepositoryPathParameter(magit.getRemoteRepository(),"users"));
            owner.leaveMessageToMe(
                    Settings.language.getString("BRANCH_PUSHED_TO_REPOSITORY_KEY"),
                    magit.getCurrentRepository().getName(),
                    Settings.language.getString("BRANCH_PUSHED_TO_REPOSITORY"),
                    new SimpleDateFormat(Settings.WEB_DATE_FORMAT).format(new Date()),
                    user.getName()
            );
        } catch (RepositoryException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print(e.getMessage());
        }
    }
}
