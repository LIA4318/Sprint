# Sprint 6 : API Web retournant du JSON

## Objectif

Une méthode de contrôleur peut retourner directement une réponse JSON au client, sans utiliser de JSP. Le framework ne construit ni ne sérialise le JSON : la méthode fournit déjà une chaîne JSON, transmise telle quelle dans la réponse HTTP.

## Les éléments du code

### Annotation `@Json`

Dans `src/lcsfw/fw/annotation/Json.java`, l’annotation est réservée aux méthodes (`@Target(ElementType.METHOD)`) et conservée à l’exécution (`@Retention(RetentionPolicy.RUNTIME)`). Cette conservation permet au Front Controller de la détecter par réflexion au moment de traiter la requête.

### Contrôleur d’exemple

Dans `src/controllers/ApiController.java`, la classe porte `@Controller`. La méthode `testJson()` porte `@Json` et `@UrlMapping` :

```java
@Json
@UrlMapping(url = "/api/test", method = HttpMethode.GET)
public String testJson() {
    return "{\"message\":\"Sprint 6 JSON OK\"}";
}
```

La chaîne retournée est déjà du JSON valide. Il ne faut donc pas la passer dans une bibliothèque de sérialisation.

La méthode `accueil()` du même contrôleur fournit un exemple du chemin classique : elle retourne le chemin d’une JSP et ne porte pas `@Json`.

### Enregistrement des routes

Au démarrage de l’application, `FrontControllerListner` lit le paramètre `controller-package` du `web.xml`. La valeur est `controllers`. Il appelle `ScanAnnotation.generateMap`, qui repère les classes `@Controller`, puis leurs méthodes `@UrlMapping`, et enregistre les couples URL/méthode HTTP.

Le `web.xml` déclare également ce listener et associe les requêtes au servlet `lcsfw.fw.controller.FrontController`.

### Traitement de la requête

`FrontController` retire le chemin de contexte de l’URL demandée et cherche la route à partir de l’URL et de la méthode HTTP. Il crée ensuite le contrôleur et invoque la méthode trouvée par réflexion.

Après l’invocation :

- si la méthode porte `@Json`, le servlet définit le type de contenu `application/json`, l’encodage UTF-8, écrit la chaîne dans la réponse et termine le traitement ; aucun dispatch vers une vue n’a lieu ;
- sinon, si le résultat n’est pas nul, il est interprété comme le chemin d’une vue et transmis au `RequestDispatcher`.

## Flux JSON

```text
GET /sprint4/api/test
    -> recherche de la route GET /api/test
    -> invocation de ApiController.testJson()
    -> détection de @Json
    -> écriture de la chaîne JSON telle quelle
    -> réponse HTTP, sans JSP
```

Réponse attendue :

```http
HTTP/1.1 200
Content-Type: application/json;charset=UTF-8

{"message":"Sprint 6 JSON OK"}
```

## Compiler, déployer et tester

`build-deploy.sh` compile les fichiers Java de `src` avec l’API Servlet fournie, assemble un nouveau WAR sans les anciens JAR du dossier `build`, puis le copie dans le dossier `webapps` de Tomcat. Cette étape est importante : démarrer Tomcat seul ne recompile pas le code source.

Depuis la racine du projet :

```sh
sh build-deploy.sh
/tmp/apache-tomcat-10.1.44/bin/catalina.sh run
```

Si Tomcat est déjà lancé, le script redéploie le WAR automatiquement. Tester ensuite l’endpoint :

```sh
curl -i http://localhost:8080/sprint4/api/test
```

L’accueil classique est disponible à `http://localhost:8080/sprint4/` et l’exemple JSON à `http://localhost:8080/sprint4/api/test`.
