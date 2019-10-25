package magit.servlets.merge;

import exceptions.MyFileException;
import exceptions.RepositoryException;
import exceptions.eErrorCodes;
import magit.BlobMap;
import magit.Branch;
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
import java.io.PrintWriter;
import java.util.Map;

@WebServlet(name = "MergeStepTwoServlet", urlPatterns = {"/merge_step_two"})
public class MergeStepTwoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String  targetBranch = request.getParameter("selected_branch"),
                repositoryID = request.getParameter("repository_id"),
                tracking = request.getParameter("rb"),
                userName = request.getParameter("user_id"); // if current user is the owner then the parameter is the string "null" and not null

        if (targetBranch != null && repositoryID != null) {
            User user = WebUI.getUser(request, userName);
            try (PrintWriter out = response.getWriter()){
                Magit magit = user.getRepository(Integer.parseInt(repositoryID));
                Branch target;
                if (tracking != null) {
                    targetBranch = magit.getRemoteRepository().getCurrentRepository().getName() +
                            File.separator + targetBranch;
                    target = magit.findRemoteBranch(targetBranch);
                } else {
                    target = magit.findBranch(targetBranch);
                }
                if (target != null) {
                    Map<String, BlobMap> changes = magit.findChanges(target);
                    if (changes.containsKey(Settings.FAST_FORWARD_MERGE)) {
                        magit.fastForwardMerge(target);
                        out.print(Settings.FAST_FORWARD_MERGE);
                    } else {
                        out.print(magit.CreateFilesDataJSON(changes));
                    }
                } else {
                    throw new RepositoryException(eErrorCodes.BRANCH_NOT_EXIST, targetBranch);
                }
            } catch (RepositoryException | MyFileException e) {
                response.sendError(400, e.getMessage());
            }
        }
    }
}
