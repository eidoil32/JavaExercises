package magit.servlets.pullrequests;

import com.google.gson.Gson;
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
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@WebServlet(name = "PullRequestCreatorServlet", urlPatterns = {"/create_pr"})
public class PullRequestCreatorServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(Settings.APPLICATION_RESPONSE_TYPE);
        String comment = request.getParameter("comment"),
                lr_branchName = request.getParameter("lr_Branch"),
                rr_branchName = request.getParameter("rr_Branch"),
                repository_id = request.getParameter("repository_id"),
                username = request.getParameter("user_id");

        User user = WebUI.getUser(request, username);
        try {
            Magit magit = user.getRepository(Integer.parseInt(repository_id));
            Map<String, String> data = new HashMap<>();
            data.put(Settings.PR_LOCAL_BRANCH_NAME, lr_branchName);
            data.put(Settings.PR_REMOTE_BRANCH_NAME, new File(rr_branchName).getName());
            data.put(Settings.PR_COMMENT, comment);
            data.put(Settings.PR_DATE_CREATION, new SimpleDateFormat(Settings.WEB_DATE_FORMAT).format(new Date()));
            data.put(Settings.PR_REQUEST_CREATOR, user.getName());
            data.put(Settings.PR_REMOTE_REPOSITORY_ID,
                    user.getRepositoryPathParameter(magit.getRemoteRepository(), "repositories").replaceFirst(Settings.SINGLE_REPOSITORY_PREFIX,""));
            data.put(Settings.PR_LOCAL_REPOSITORY_ID, repository_id);
            data.put(Settings.PR_STATUS, Settings.PR_STATUS_OPEN);
            User remoteOwner = WebUI.getUser(request, user.getRepositoryPathParameter(magit.getRemoteRepository(), "users"));
            remoteOwner.createPullRequest(data);
            remoteOwner.leaveMessageToMe(
                    Settings.language.getString("USER_PULL_REQUEST_KEY"),
                    magit.getRemoteRepository().getCurrentRepository().getName(),
                    Settings.language.getString("USER_MESSAGE_PULL_REQUEST"),
                    new SimpleDateFormat(Settings.WEB_DATE_FORMAT).format(new Date()),
                    user.getName()
            );
            response.getWriter().print(new Gson().toJson("DONE"));
        } catch (RepositoryException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print(e.getMessage());
        }

    }

}
