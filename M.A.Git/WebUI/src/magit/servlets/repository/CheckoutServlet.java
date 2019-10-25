package magit.servlets.repository;

import exceptions.MyFileException;
import exceptions.RepositoryException;
import magit.Magit;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "CheckoutServlet", urlPatterns = {"/changeHeadBranch"})
public class CheckoutServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String  locationPath = request.getParameter("location"),
                targetBranch = request.getParameter("branchSelector");

        Magit magit = new Magit();
        try (PrintWriter out = response.getWriter()){

            magit.changeRepo(locationPath);
            out.print(magit.tryCheckout(targetBranch));
        } catch (RepositoryException | MyFileException ignored) {
        }
    }
}
