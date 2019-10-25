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
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@WebServlet(name = "ForkRepositoryServlet", urlPatterns = {"/fork_repo"})
public class ForkRepositoryServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String usernameFrom = request.getParameter("user_id"),
                repositoryID = request.getParameter("repository_id");

        response.setContentType(Settings.APPLICATION_RESPONSE_TYPE);

        User userOwner = WebUI.getUser(request, usernameFrom),
                userRequest = WebUI.getUser(request, "null");
        if (userOwner.equals(userRequest)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print(Settings.language.getString("CANNOT_FORK_ITSELF"));
        } else {
            try {
                Magit magit = userOwner.getRepository(Integer.parseInt(repositoryID));
                userOwner.leaveMessageToMe(
                            Settings.language.getString("USER_FORK_REPOSITORY_KEY"),
                            magit.getCurrentRepository().getName(),
                            Settings.language.getString("USER_MESSAGE_FORK"),
                            new SimpleDateFormat(Settings.WEB_DATE_FORMAT).format(new Date()),
                            userRequest.getName()
                        );
                Integer numOfRepositories = userRequest.countRepositories();
                numOfRepositories = (numOfRepositories == null) ? 0 : numOfRepositories + 1;
                File source = new File(String.format(Settings.USERS_REPOSITORY_FOLDER, userOwner.getName(), repositoryID)),
                        dest = new File(String.format(Settings.USERS_REPOSITORY_FOLDER, userRequest.getName(), numOfRepositories));
                magit.magitClone(source, dest);

                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().print(true);
            } catch (RepositoryException e) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }
    }
}
