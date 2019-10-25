package magit.servlets.repository;

import exceptions.MyFileException;
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

@WebServlet(name = "PullHeadServlet", urlPatterns = {"/pull_head"})
public class PullHeadServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(Settings.APPLICATION_RESPONSE_TYPE);
        String  repository_id = request.getParameter("repo_id"),
                username = request.getParameter("user");

        try {
            Magit magit = WebUI.getUser(request,username).getRepository(Integer.parseInt(repository_id));
            magit.pull();
        } catch (RepositoryException | MyFileException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print(e.getMessage());
        }
    }
}
