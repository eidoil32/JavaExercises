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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@WebServlet(name = "RejectPullRequestServlet", urlPatterns = {"/reject_pr"})
public class RejectPullRequestServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(Settings.APPLICATION_RESPONSE_TYPE);
        String comment = request.getParameter("cause"),
                pr_id = request.getParameter("pr_id"),
                repository_id = request.getParameter("repository_id"),
                username = request.getParameter("user_id");

        try {
            User user = WebUI.getUser(request, username);
            Map<String, String> pullRequest = user.getPullRequest(repository_id, pr_id);
            User prCreatorUser = WebUI.getUser(request, pullRequest.get(Settings.PR_REQUEST_CREATOR));
            Magit localMagit = prCreatorUser.getRepository(
                    Integer.parseInt(pullRequest.get(Settings.PR_LOCAL_REPOSITORY_ID)));
            prCreatorUser.leaveMessageToMe(Settings.language.getString("USER_PULL_REQUEST_KEY"),
                    localMagit.getCurrentRepository().getName(),
                    comment,
                    new SimpleDateFormat(Settings.WEB_DATE_FORMAT).format(new Date()),
                    user.getName());
            user.removePullRequest(repository_id, pr_id);
            response.getWriter().print(new Gson().toJson("DONE"));
        } catch (RepositoryException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print(e.getMessage());
        }
    }
}
