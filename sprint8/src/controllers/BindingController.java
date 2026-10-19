package controllers;

import lcsfw.fw.annotation.Controller;
import lcsfw.fw.annotation.UrlMapping;
import lcsfw.fw.http.HttpMethode;
import lcsfw.fw.file.FileUpload;

// Controleur de demonstration du binding d'un objet metier complet.
@Controller
public class BindingController {

    // Le framework construit FileUpload puis le remplit avant l'appel de cette methode.
    @UrlMapping(url = "/binding/saluer", method = HttpMethode.POST)
    public String saluer(FileUpload filemap) {
        // Le controleur peut choisir ici un stockage disque ou une insertion en base.
        return "/WEB-INF/vues/resultat.jsp";
    }
}
