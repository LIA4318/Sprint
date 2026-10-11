package lcsfw.fw.controller;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lcsfw.fw.annotation.Json;
import lcsfw.fw.http.HttpMethode;
import lcsfw.fw.mapping.Mapping;
import lcsfw.fw.mapping.UrlMethode;
import lcsfw.fw.util.ParameterBinder;

public class FrontController extends HttpServlet {

    // Point d'entree HTTP 
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    private void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Les routes sont calculees une seule fois au demarrage et conservees dans le contexte Servlet.
        ServletContext context = req.getServletContext();
        HashMap<UrlMethode, Mapping> mapping = (HashMap<UrlMethode, Mapping>) context.getAttribute("mapping");
        if (mapping == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Mapping introuvable");
            return;
        }

        // On retire le contexte de l'application pour comparer l'URL avec celle de @UrlMapping.
        String askUrl = req.getRequestURI();
        String contextPath = req.getContextPath();

        askUrl = askUrl.substring(contextPath.length());

        // Une route est identifiee par le couple URL + verbe HTTP 
        HttpMethode methode = HttpMethode.valueOf(req.getMethod());
        UrlMethode urlMethode = new UrlMethode(askUrl, methode);
        Mapping map = mapping.get(urlMethode);
        if (map == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Url introuvable");
            return;
        }

        Class<?> controllerClass = map.getControllerClass();
        Method method = map.getMethod();
        try {
            // Chaque requete cree une instance du controleur associe a la route trouvee.
            Object controller = controllerClass.getDeclaredConstructor().newInstance();

            // Le binder prepare les arguments simples ou construit l'unique objet metier attendu.
            Object[] arguments = ParameterBinder.bind(method, req);
            Parameter[] parameters = method.getParameters();
            for (int index = 0; index < parameters.length; index++) {
                if (!parameters[index].getType().isPrimitive()
                    && !ParameterBinder.isSimpleType(parameters[index].getType())) {
                    // L'objet lie est aussi expose a la JSP sous le nom du parametre de methode.
                    req.setAttribute(parameters[index].getName(), arguments[index]);
                }
            }

            // Appel du controleur avec les valeurs recuperees automatiquement.
            Object result = method.invoke(controller, arguments);

            // Une methode @Json renvoie sa chaine brute et ne passe jamais par une vue JSP.
            if (method.isAnnotationPresent(Json.class)) {
                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                if (result != null) {
                    resp.getWriter().print((String) result);
                }
                return;
            }

            // Sinon, le resultat est interprete comme le chemin de la vue a afficher.
            if (result != null) {
                req.getRequestDispatcher((String) result).forward(req, resp);
            }
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException e) {
            throw new ServletException("Erreur (LcsFw) :" + e, e);
        }

    }
}
