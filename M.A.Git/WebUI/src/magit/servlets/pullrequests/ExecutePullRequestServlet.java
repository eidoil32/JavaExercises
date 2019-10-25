package magit.servlets.pullrequests;

import com.google.gson.Gson;
import exceptions.MyFileException;
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
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;

@WebServlet(name = "ExecutePullRequestServlet", urlPatterns = {"/approve_pr"})
public class ExecutePullRequestServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(Settings.APPLICATION_RESPONSE_TYPE);
        String  pr_id = request.getParameter("pr_id"),
                repository_id = request.getParameter("repository_id"),
                username = request.getParameter("user_id");

        User user = WebUI.getUser(request, username);

        try {
            Map<String, String> pullRequest = user.getPullRequest(repository_id, pr_id);
            User prCreatorUser = WebUI.getUser(request, pullRequest.get(Settings.PR_REQUEST_CREATOR));
            Magit localMagit = prCreatorUser.getRepository(
                    Integer.parseInt(pullRequest.get(Settings.PR_LOCAL_REPOSITORY_ID)));
            Magit remote = user.getRepository(Integer.parseInt(repository_id));

            // merge between branches

            String commentPR = pullRequest.get(Settings.PR_COMMENT);
            Set<File> delta = localMagit.getDeltaFilesForChanges(localMagit.getAllCommitsForPR(pullRequest,
                    prCreatorUser.getRepository(
                            Integer.parseInt(pullRequest.get(Settings.PR_LOCAL_REPOSITORY_ID)))));
            Commit localCommit = localMagit.findBranch(pullRequest.get(Settings.PR_LOCAL_BRANCH_NAME)).getCommit();
            Commit remoteCommit = remote.findBranch(pullRequest.get(Settings.PR_REMOTE_BRANCH_NAME)).getCommit();
            delta.add(localMagit.commitFile(localCommit));
            remote.getDeltaFromPR(delta);
            remote.merge(localCommit, remoteCommit,commentPR,
                    remote.getCurrentBranch().getName().equals(pullRequest.get(Settings.PR_REMOTE_BRANCH_NAME)));

            // notify user and remove pull request.
            prCreatorUser.leaveMessageToMe(Settings.language.getString("USER_PULL_REQUEST_KEY"),
                    localMagit.getCurrentRepository().getName(),
                    Settings.language.getString("USER_PULL_REQUEST_APPROVED"),
                    new SimpleDateFormat(Settings.WEB_DATE_FORMAT).format(new Date()),
                    user.getName());
            user.removePullRequest(repository_id, pr_id);
            response.getWriter().print(new Gson().toJson("DONE"));
        } catch (RepositoryException | MyFileException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print(e.getMessage());
        }
    }
}
