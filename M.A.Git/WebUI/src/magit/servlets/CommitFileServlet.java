package magit.servlets;

import com.google.gson.Gson;
import exceptions.MyFileException;
import exceptions.RepositoryException;
import magit.*;
import usermanager.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "CommitFileServlet", urlPatterns = {"/single_commit"})
public class CommitFileServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String shaOne = req.getParameter("sha-1"),
                repositoryID = req.getParameter("repository_id"),
                userName = req.getParameter("user_id"); // if current user is the owner then the parameter is the string "null" and not null

        if (shaOne != null && repositoryID != null) {
            User user = WebUI.getUser(req, userName);

            try (PrintWriter out = resp.getWriter()) {
                Magit magit = user.getRepository(Integer.parseInt(repositoryID));
                Commit commit = magit.getCommitData(shaOne);
                BlobMap blobMap = magit.getCurrentRepository().loadDataFromCommit(commit);
                Map<String, String> filesData = getFilesData(blobMap);
                out.print(new Gson().toJson(filesData));
            } catch (RepositoryException | MyFileException ignored) {
            }
        }
    }

    private Map<String, String> getFilesData(BlobMap blobMap) {
        Map<String, String> map = new HashMap<>();
        for (Map.Entry<BasicFile, Blob> entry : blobMap.getMap().entrySet()) {
            Blob blob = entry.getValue();
            if (blob.getType() == eFileTypes.FOLDER) {
                map.put(blob.getName(), new Gson().toJson(getFilesData(((Folder)blob).getBlobMap())));
            } else {
                map.put(blob.getName(), blob.getContent());
            }
        }

        return map;
    }
}
