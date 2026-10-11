package controllers;

import lcsfw.fw.annotation.Controller;
import lcsfw.fw.annotation.UrlMapping;
import lcsfw.fw.http.HttpMethode;
import models.Personne;

// Controleur de demonstration du binding d'un objet metier complet.
@Controller
public class BindingController {

    // Le framework construit Personne puis le remplit avant l'appel de cette methode.
    @UrlMapping(url = "/binding/saluer", method = HttpMethode.POST)
    public String saluer(Personne personne) {
        // les champs lies => age vaut 0 si le formulaire ne l'a pas envoye.
        return "/WEB-INF/vues/resultat.jsp";
    }
}