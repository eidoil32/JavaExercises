package magit.servlets.commit;

import exceptions.MyFileException;
import exceptions.RepositoryException;
import magit.Magit;
import magit.WebUI;
import usermanager.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "ExecuteCommitServlet", urlPatterns = {"/execute_commit"})
public class ExecuteCommitServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("user_id"),
                repositoryID = request.getParameter("repository_id"),
                comment = request.getParameter("comment");

        User user = WebUI.getUser(request, username);
        PrintWriter out = response.getWriter();
        try {
            Magit magit = user.getRepository(Integer.parseInt(repositoryID));
            magit.commitMagit(user.getName(), comment);
            out.print(magit.getCurrentBranch().getCommit().toJSON());
        } catch (RepositoryException | MyFileException e) {
            response.setStatus(400);
            out.print(e.getMessage());
        } finally {
            out.close();
        }
    }
}
