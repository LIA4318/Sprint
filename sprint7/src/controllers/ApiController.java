package controllers;

import lcsfw.fw.annotation.Controller;
import lcsfw.fw.annotation.Json;
import lcsfw.fw.annotation.UrlMapping;
import lcsfw.fw.http.HttpMethode;

@Controller
public class ApiController {

    // Route GET d'accueil : affiche le formulaire de binding.
    @UrlMapping(url = "/", method = HttpMethode.GET)
    public String accueil() {
        return "/WEB-INF/vues/accueil.jsp";
    }

    // Exemple conserve du Sprint 6 : une reponse JSON est ecrite directement, sans JSP.
    @Json
    @UrlMapping(url = "/api/test", method = HttpMethode.GET)
    public String testJson() {
        // Cette valeur est deja du JSON : le framework la transmet sans conversion.
        return "{\"message\":\"Sprint 6 JSON OK\"}";
    }
}