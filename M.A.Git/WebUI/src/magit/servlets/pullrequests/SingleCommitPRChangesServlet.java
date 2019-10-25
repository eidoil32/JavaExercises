package magit.servlets.pullrequests;

import com.google.gson.Gson;
import exceptions.MyFileException;
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
import java.io.PrintWriter;
import java.util.Map;

@WebServlet(name = "SingleCommitPRChangesServlet", urlPatterns = {"/commit_changes"})
public class SingleCommitPRChangesServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(Settings.APPLICATION_RESPONSE_TYPE);
        String  commitSHA_ONE = request.getParameter("sha_1"),
                prID = request.getParameter("pr_id"),
                repositoryID = request.getParameter("repo_id"),
                username = request.getParameter("user");

        User user = WebUI.getUser(request, username);
        try (PrintWriter out = response.getWriter()){
            Map<String, String> pullRequest = user.getPullRequest(repositoryID, prID);
            User prCreatorUser = WebUI.getUser(request, pullRequest.get(Settings.PR_REQUEST_CREATOR));
            Magit localMagit = prCreatorUser.getRepository(
                    Integer.parseInt(pullRequest.get(Settings.PR_LOCAL_REPOSITORY_ID)));

            Map<String, String> files = localMagit.getDeltaFromCommits(localMagit.getAllCommitsForPR(pullRequest,
                    localMagit), commitSHA_ONE);
            out.write(new Gson().toJson(files));
        } catch (RepositoryException | IOException | MyFileException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print(e.getMessage());
        }
    }
}