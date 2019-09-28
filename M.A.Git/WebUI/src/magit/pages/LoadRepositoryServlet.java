package magit.pages;

import exceptions.MyFileException;
import exceptions.MyXMLException;
import exceptions.RepositoryException;
import magit.Magit;
import magit.Repository;
import magit.WebUI;
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
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // need to do something?
        response.sendRedirect("fileupload/form.html");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        Collection<Part> parts = request.getParts();
        StringBuilder fileContent = new StringBuilder();

        for (Part part : parts) {
            if (part.getName().equals("field_path"))
                fileContent.append(readFromInputStream(part.getInputStream()));
        }
        try {
            MagitRepository magitRepository = FileManager.deserializeFrom(new StringReader(fileContent.toString()));
            Magit.basicCheckXML(magitRepository);
            Magit magit = new Magit();
            magit.setCurrentRepository(Repository.XML_RepositoryFactory(magitRepository));
            User user = (User) request.getSession().getAttribute(Settings.WSA_USER);
            WebUI.createRepositoryData(user, request, magit);
        } catch (JAXBException | RepositoryException | MyFileException | MyXMLException e) {
            response.sendRedirect("main.html?file_upload=failed");
        }

        response.sendRedirect("main.html?file_upload=success");
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
