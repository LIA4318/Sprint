package lcsfw.fw.controller;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

public class FrontController extends HttpServlet {

    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    private void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ServletContext context = req.getServletContext();
        HashMap<UrlMethode, Mapping> mapping = (HashMap<UrlMethode, Mapping>) context.getAttribute("mapping");
        if (mapping == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Mapping introuvable");
            return;
        }
        String askUrl = req.getRequestURI();
        String contextPath = req.getContextPath();

        askUrl = askUrl.substring(contextPath.length());
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
            Object controller = controllerClass.getDeclaredConstructor().newInstance();
            Object result = method.invoke(controller);

            // Une methode @Json renvoie sa chaine brute et ne passe jamais par une vue.
            if (method.isAnnotationPresent(Json.class)) {
                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                if (result != null) {
                    resp.getWriter().print((String) result);
                }
                return;
            }

            if (result != null) {
                req.getRequestDispatcher((String) result).forward(req, resp);
            }
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException e) {
            throw new ServletException("Erreur (LcsFw) :" + e, e);
        }

    }
}
