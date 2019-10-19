package magit.pages;

import exceptions.MyFileException;
import exceptions.MyXMLException;
import exceptions.RepositoryException;
import magit.Magit;
import magit.Repository;
import settings.Settings;
import usermanager.User;
import utils.FileManager;
import xml.basic.MagitRepository;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Collection;
import java.util.Scanner;

@WebServlet(name = "LoadRepositoryServlet", urlPatterns = {"/load-repository"})
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5 * 5)
public class LoadRepositoryServlet extends HttpServlet {
    private static final String RESPONSE_FILE_UPLOAD = "file_upload=",
                                FILE_UPLOAD_SUCCESSFULLY = RESPONSE_FILE_UPLOAD + "success";
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // need to do something?
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(Settings.APPLICATION_RESPONSE_TYPE);
        Collection<Part> parts = request.getParts();
        StringBuilder fileContent = new StringBuilder();
        String url = request.getHeader(Settings.URL_REFERER);
        if (url.contains(Settings.GET_URL_PARAMETERS_ADDON)) {
            url += Settings.GET_URL_PARAMETERS_ADDON_PLUS;
        } else {
            url += Settings.GET_URL_PARAMETERS_ADDON;
        }

        for (Part part : parts) {
            if (part.getName().equals("field_path"))
                fileContent.append(readFromInputStream(part.getInputStream()));
        }
        try {
            MagitRepository magitRepository = FileManager.deserializeFrom(new StringReader(fileContent.toString()));
            Magit.basicCheckXML(magitRepository);
            Magit magit = new Magit();
            User user = (User) request.getSession().getAttribute(Settings.WSA_USER);
            Integer numOfRepositories = user.countRepositories();
            numOfRepositories = (numOfRepositories == null) ? 0 : numOfRepositories + 1;
            magit.setCurrentRepository(Repository.RepositoryFactory_Web(
                    magitRepository,
                    String.format(Settings.USERS_REPOSITORY_FOLDER, user.getName(), numOfRepositories)));
            magit.afterXMLLayout();
            request.getSession().setAttribute(Settings.WSA_REPOSITORIES_NUMBER, numOfRepositories);
            response.sendRedirect(url + FILE_UPLOAD_SUCCESSFULLY);
        } catch (JAXBException e) {
            response.sendRedirect(url + RESPONSE_FILE_UPLOAD + Settings.INVALID_XML_FILE);
        } catch (RepositoryException | MyFileException | MyXMLException e) {
            response.sendRedirect(url + RESPONSE_FILE_UPLOAD + replaceSpacesWithUnderline(e.getMessage()));
        }
    }

    private String replaceSpacesWithUnderline(String message) {
        message = message.replaceAll(" ", "_");
        message = message.replaceAll(":", "");
        return message;
    }

    private String readFromInputStream(InputStream inputStream) {
        return new Scanner(inputStream).useDelimiter("\\Z").next();
    }

    private void printError(HttpServletResponse response, String message) {
        try (PrintWriter out = response.getWriter()) {
            out.print(message);
        } catch (IOException ignored) {
        }
    }
}
