package magit.servlets;

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
import java.util.Map;

@WebServlet(name = "FileViewerServlet", urlPatterns = {"/file_content"})
public class FileViewerServlet extends HttpServlet {
    private static final String regexPath = ",";
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String shaOne = req.getParameter("sha-1"),
                repositoryID = req.getParameter("repository_id"),
                filePath = req.getParameter("file_path"),
                userName = req.getParameter("user_id"); // if current user is the owner then the parameter is the string "null" and not null

        if (shaOne != null && repositoryID != null && filePath != null) {
            User user = WebUI.getUser(req, userName);
            try (PrintWriter out = resp.getWriter()) {
                Magit magit = user.getRepository(Integer.parseInt(repositoryID));
                Commit commit = magit.getCommitData(shaOne);
                BlobMap blobMap = magit.getCurrentRepository().loadDataFromCommit(commit);

                out.print(getFileContent(blobMap, filePath));
            } catch (RepositoryException | MyFileException ignored) {
            }
        }
    }

    private String getFileContent(BlobMap blobMap, String filePath) {
        return findFile(blobMap, filePath).getContent();
    }

    private Blob findFile(BlobMap blobMap, String filePath) {
        String[] parts = filePath.split(regexPath);
        for (Map.Entry<BasicFile, Blob> entry : blobMap.getMap().entrySet()) {
            Blob blob = entry.getValue();
            if (blob.getName().equals(parts[0])) {
                if (blob.getType() == eFileTypes.FOLDER) {
                    return findFile(((Folder)blob).getBlobMap(), createPathWithoutFirst(filePath));
                } else {
                    return blob;
                }
            }
        }

        return null;
    }

    private String createPathWithoutFirst(String filePath) {
        StringBuilder result = new StringBuilder();
        String[] parts = filePath.split(regexPath);

        for (int i = 1; i < parts.length; i++) {
            result.append(parts[i]);
            if (i + 1 != parts.length) {
                result.append(regexPath);
            }
        }

        return result.toString();
    }
}
