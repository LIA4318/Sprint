package itu.controller;

import itu.annotation.Url;
import itu.utils.Utils;
import itu.affichage.Affichage; 
import jakarta.servlet.RequestDispatcher; 
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class FrontController extends HttpServlet {

    private List<Class<?>> listeClassController;
    private Map<Class<?>, List<Method>> listeMethode;

    @Override
    public void init() throws ServletException {
        super.init();
        initialiserFramework();
    }

    private void initialiserFramework() {
        System.out.println("=== Initialisation du framework ===");

        listeClassController = Utils.scanControllers();
        System.out.println("Nombre de controllers trouvés : " + listeClassController.size());

        listeMethode = Utils.scanMethodes(listeClassController);

        for (Class<?> classe : listeClassController) {
            System.out.println("\n[Controller] " + classe.getName());
            List<Method> methodes = listeMethode.get(classe);
            if (methodes == null || methodes.isEmpty()) {
                System.out.println("   (aucune URL mappée)");
            } else {
                for (Method m : methodes) {
                    String url = m.getAnnotation(Url.class).value();
                    Url.TYPE type = m.getAnnotation(Url.class).type();
                    System.out.println("   @Url -> " + url + " [Type: " + type + "] [méthode: " + m.getName() + "]");
                }
            }
        }
        System.out.println("\n=== Framework initialisé ! ===");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        processRequest(req, resp);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String chemin = request.getPathInfo() != null ? request.getPathInfo() : request.getServletPath();
        
        if (chemin.endsWith(".jsp")) {
            RequestDispatcher jspDispatcher = request.getServletContext().getNamedDispatcher("jsp");
            jspDispatcher.forward(request, response);
            return;
        }
        
        if (chemin.endsWith(".css") || chemin.endsWith(".js")
                || chemin.endsWith(".png") || chemin.endsWith(".jpg") || chemin.endsWith(".ico")) {
            RequestDispatcher defaultDispatcher = request.getServletContext().getNamedDispatcher("default");
            defaultDispatcher.forward(request, response);
            return;
        }

        String url = request.getRequestURI();
        System.out.println("[FrontController] URL reçue --> " + url);

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("  <meta charset='UTF-8'>");
        out.println("  <title>FrontController</title>");
        out.println("</head>");
        out.println("<body>");

        out.println("<h1>FrontController</h1>");
        out.println("<p>URL reçue : <code>" + url + "</code> (Method: " + request.getMethod() + ")</p>");

        Object[] methodConcernee = verifierUrl(out, request);
        if (methodConcernee == null) {
            out.println("</body></html>");
            return;
        }
        invokeMethod(methodConcernee, request, response);
    }

    private Object[] verifierUrl(PrintWriter out, HttpServletRequest request) {
        Affichage.urlDispoParController(listeMethode, out);
        return Affichage.verifierUrl(listeMethode, out, request);
    }

    private void invokeMethod(Object[] methodeConcernee, HttpServletRequest request, HttpServletResponse response) {
        try {
            Class<?> classe = (Class<?>) methodeConcernee[1];
            Method methode = (Method) methodeConcernee[0];
            Object retour = methode.invoke(classe.getDeclaredConstructor().newInstance());
            String page = String.valueOf(retour);
            if (!page.equals("null") && !page.equalsIgnoreCase("goldFramework")) {
                RequestDispatcher dispatcher = request.getRequestDispatcher("/" + page + ".jsp");
                dispatcher.forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}