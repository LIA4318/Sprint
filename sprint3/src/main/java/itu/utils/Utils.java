package itu.utils;

import itu.annotation.Controller;
import itu.annotation.Url;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {

    private static final String BASE_PACKAGE = "itu"; 

    public static List<Class<?>> scanControllers() {
        List<Class<?>> controllers = new ArrayList<>();
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = classLoader.getResources(BASE_PACKAGE.replace(".", "/"));
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                File dossier = new File(resource.getFile());
                if (dossier.exists() && dossier.isDirectory()) {
                    scannerDossier(dossier, BASE_PACKAGE, controllers);
                }
            }
        } catch (IOException e) {
            System.err.println("[Framework] Erreur scan classpath : " + e.getMessage());
        }
        return controllers;
    }

    private static void scannerDossier(File dossier, String packageCourant, List<Class<?>> controllers) {
        File[] fichiers = dossier.listFiles();
        if (fichiers == null) return;
        for (File fichier : fichiers) {
            if (fichier.isDirectory()) {
                scannerDossier(
                        fichier,
                        packageCourant + "." + fichier.getName(),
                        controllers
                );
            } else if (fichier.getName().endsWith(".class")) {
                String nomClasse = null;
                try {
                    nomClasse = packageCourant + "." + fichier.getName().replace(".class", "");
                    Class<?> classe = Class.forName(nomClasse, false, Thread.currentThread().getContextClassLoader());
                    if (isController(classe)) {
                        controllers.add(classe);
                        System.out.println("[Framework] Controller trouvé : " + nomClasse);
                    }
                } catch (ClassNotFoundException | NoClassDefFoundError e) {
                    System.err.println("[Framework] Classe ignorée : " + nomClasse);
                }
            }
        }
    }

    /**
     * Scanne les méthodes annotées @Url dans chaque controller.
     */
    public static Map<Class<?>, List<Method>> scanMethodes(List<Class<?>> listController) {
        Map<Class<?>, List<Method>> resultat = new HashMap<>();
        for (Class<?> classe : listController) {
            List<Method> methodesMappees = new ArrayList<>();
            for (Method methode : classe.getDeclaredMethods()) {
                if (methode.isAnnotationPresent(Url.class)) {
                    methodesMappees.add(methode);
                    String url = methode.getAnnotation(Url.class).value();
                    Url.TYPE type = methode.getAnnotation(Url.class).type();
                    System.out.println("[Framework] @Url trouvé : "
                            + classe.getSimpleName() + "#" + methode.getName()
                            + " -> " + url + " [" + type + "]");
                }
            }
            resultat.put(classe, methodesMappees);
        }
        return resultat;
    }

    private static boolean isController(Class<?> classe) {
        if (classe == null) return false;
        try {
            return classe.isAnnotationPresent(Controller.class);
        } catch (Throwable t) {
            return false;
        }
    }
}