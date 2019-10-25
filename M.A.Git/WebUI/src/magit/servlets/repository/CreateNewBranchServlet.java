package magit.servlets.repository;

import exceptions.RepositoryException;
import magit.Branch;
import magit.Magit;
import magit.WebUI;
import settings.Settings;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

@WebServlet(name = "CreateNewBranchServlet", urlPatterns = {"/create_branch"})
public class CreateNewBranchServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(Settings.APPLICATION_RESPONSE_TYPE);
        String  repository_id = request.getParameter("repo_id"),
                trackingAfter = request.getParameter("tracking-after"),
                username = request.getParameter("user_id"),
                commit_SHA1 = request.getParameter("commit"),
                branchName = request.getParameter("branch_name");

        try {
            Magit magit = WebUI.getUser(request,username).getRepository(Integer.parseInt(repository_id));
            if (!trackingAfter.equals("none")) {
                String remoteName = new File(trackingAfter).getName();

                magit.tryCreateNewRemoteTrackingBranch(remoteName, new Branch(remoteName));
                Branch branch = magit.findBranch(remoteName);
                branch.setCommit(magit.getCommitData(commit_SHA1),magit.getCurrentRepository().getBranchesPath().toString());
            } else {
                magit.tryCreateNewBranch(branchName, magit.getCommitData(commit_SHA1));
            }
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().print(Settings.language.getString("FX_BRANCH_CREATED_SUCCESSFULLY"));
        } catch (RepositoryException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print(e.getMessage());
        }
    }
}
