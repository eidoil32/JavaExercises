package magit.servlets.pullrequests;

import com.google.gson.Gson;
import exceptions.RepositoryException;
import magit.Commit;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "SinglePullRequestServlet", urlPatterns = {"/pullRequest_data"})
public class SinglePullRequestServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(Settings.APPLICATION_RESPONSE_TYPE);
        String  prID = request.getParameter("pr_id"),
                repositoryID = request.getParameter("repository_id"),
                username = request.getParameter("user_id");

        User user = WebUI.getUser(request, username);
        try (PrintWriter out = response.getWriter()){
            Magit magit = user.getRepository(Integer.parseInt(repositoryID));
            Map<String, String> data = new HashMap<>();
            Map<String, String> pullRequest = user.getPullRequest(repositoryID, prID);
            User prCreatorUser = WebUI.getUser(request, pullRequest.get(Settings.PR_REQUEST_CREATOR));
            List<Commit> commits = magit.getAllCommitsForPR(pullRequest,
                    prCreatorUser.getRepository(
                            Integer.parseInt(pullRequest.get(Settings.PR_LOCAL_REPOSITORY_ID))));
            Map<String, String> commitsDetails = new HashMap<>();
            for (Commit commit : commits) {
                commitsDetails.put(commit.getSHA_ONE(), commit.toJSON());
            }
            data.put(Settings.PR_ALL_COMMITS,new Gson().toJson(commitsDetails));
            out.write(new Gson().toJson(data));
        } catch (RepositoryException | IOException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print(e.getMessage());
        }
    }
}