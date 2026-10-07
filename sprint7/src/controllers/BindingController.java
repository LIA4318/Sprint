package controllers;

import lcsfw.fw.annotation.Controller;
import lcsfw.fw.annotation.UrlMapping;
import lcsfw.fw.http.HttpMethode;

@Controller
public class BindingController {

    // Cette route accepte un formulaire POST, pas une ouverture directe par GET dans la barre d'adresse.
    @UrlMapping(url = "/binding/saluer", method = HttpMethode.POST)
    public String saluer(String prenom, String message) {
        // Les noms prenom et message correspondent aux attributs name du formulaire.
        // Le framework passe null si un de ces parametres est absent; cette demo ne les affiche pas encore.
        return "/WEB-INF/vues/resultat.jsp";
    }
}