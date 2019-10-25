package magit.servlets.merge;

import exceptions.MyFileException;
import exceptions.RepositoryException;
import exceptions.eErrorCodes;
import magit.Branch;
import magit.Magit;
import magit.WebUI;
import usermanager.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "EndMerge", urlPatterns = {"/end_merge"})
public class EndMerge extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String[]> parameterMap = new HashMap<>(request.getParameterMap());
        String repositoryID = parameterMap.remove("repository_id")[0],
                selectedBranch = parameterMap.remove("selectedBranch")[0],
                comment = parameterMap.remove("user_comment")[0],
                username = parameterMap.remove("user_id")[0];

        User user = WebUI.getUser(request, username);
        try {
            Magit magit = user.getRepository(Integer.parseInt(repositoryID));
            Branch target = magit.findBranch(selectedBranch);
            if (target != null) {
                magit.finishMerge(target, parameterMap, comment); //parameterMap include now only files names and theirs content.
            } else {
                throw new RepositoryException(eErrorCodes.BRANCH_NOT_EXIST);
            }
        } catch (RepositoryException | MyFileException e) {
            response.sendError(400, e.getMessage());
        }
    }
}
