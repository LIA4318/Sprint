# Sprint 7 : binding des paramètres

## But du sprint

Ce sprint ajoute le binding automatique des paramètres HTTP vers une méthode de contrôleur. Si une méthode déclare `String prenom`, le framework cherche un paramètre HTTP nommé `prenom`, récupère sa valeur et la lui passe lors de l'invocation.

Le projet Sprint 7 est autonome : il reprend le framework et les routes du Sprint 6, puis ajoute un formulaire et un contrôleur de démonstration.

## Fichiers importants

- `src/lcsfw/fw/controller/FrontController.java` reçoit les requêtes, trouve le mapping et construit les arguments de la méthode.
- `src/lcsfw/fw/controller/FrontControllerListner.java` initialise les mappings au démarrage en scannant les contrôleurs.
- `src/controllers/BindingController.java` déclare la route POST `/binding/saluer` et la méthode `saluer(String prenom, String message)`.
- `webapp/WEB-INF/vues/accueil.jsp` contient le formulaire.
- `webapp/WEB-INF/vues/resultat.jsp` est la vue affichée après l'invocation du contrôleur.
- `webapp/WEB-INF/web.xml` déclare le listener et associe les requêtes au Front Controller.
- `build-deploy.sh` compile les sources et copie le WAR dans le dossier `webapps` de Tomcat.

## Déroulement d'une requête

1. Le navigateur demande `GET /sprint7/`.
2. Le Front Controller retire le chemin de contexte `/sprint7`, puis cherche le mapping `GET /`.
3. Le mapping de la page d'accueil est trouvé et `accueil.jsp` s'affiche.
4. L'utilisateur remplit le formulaire. Les attributs `name="prenom"` et `name="message"` donnent les noms des paramètres HTTP.
5. Le bouton envoie une requête `POST /sprint7/binding/saluer`.
6. Le Front Controller cherche le mapping correspondant au couple URL et méthode HTTP : `POST /binding/saluer`.
7. Il lit les paramètres de la méthode avec la réflexion Java. Pour chaque paramètre, il récupère son nom et appelle `request.getParameter(nom)`.
8. Les valeurs sont placées dans un tableau dans le même ordre que les paramètres de la méthode, puis transmis à `Method.invoke`.
9. La méthode renvoie le chemin de `resultat.jsp`; le Front Controller transfère la requête à cette vue.

Exemple du contrôleur :

```java
@UrlMapping(url = "/binding/saluer", method = HttpMethode.POST)
public String saluer(String prenom, String message) {
    return "/WEB-INF/vues/resultat.jsp";
}
```

Le formulaire utilise `method="post"` et `action="${pageContext.request.contextPath}/binding/saluer"`. Ainsi, l'adresse demandée correspond bien au mapping POST du contrôleur.

## Valeurs reçues

- Si `prenom=Naina` est envoyé, l'argument `prenom` reçoit la chaîne `Naina`.
- Si le paramètre `message` est complètement absent, `request.getParameter("message")` renvoie `null` et l'argument reçoit `null`.
- Si la requête contient `message=`, l'argument reçoit une chaîne vide, pas `null`.
- Ce sprint transmet les valeurs en `String` et ne convertit pas les nombres ni les types primitifs. Un paramètre absent ne peut pas être passé à un paramètre primitif tel que `int`, car un primitif ne peut pas contenir `null`.

Les noms Java des paramètres ne sont disponibles par réflexion que si le code est compilé avec `javac -parameters`. Le script de build utilise cette option. Si les noms n'ont pas été conservés, le Front Controller renvoie une erreur explicite au lieu de faire un matching par nom incorrect.

## Pourquoi un GET direct renvoie 404

La méthode `saluer` est annotée avec `HttpMethode.POST` uniquement. Saisir `http://localhost:8080/sprint7/binding/saluer` dans la barre d'adresse envoie un GET. Le Front Controller cherche donc `GET /binding/saluer`, ne trouve aucun mapping correspondant et renvoie `404 Url introuvable`.

Ce 404 est attendu et ne signifie pas que le POST ne fonctionne pas. Il faut ouvrir le formulaire puis le soumettre, ou envoyer explicitement une requête POST avec `curl`.

## Compiler, déployer et démarrer

La machine de développement utilisée pour ce sprint a Tomcat 10.1.46 dans `/Users/mac/Documents/apache-tomcat-10.1.46`. Le script a un chemin par défaut pour un autre environnement; définir `TOMCAT_HOME` permet d'indiquer le bon emplacement.

Depuis la racine du projet :

```sh
cd sprint7
TOMCAT_HOME=/Users/mac/Documents/apache-tomcat-10.1.46 sh build-deploy.sh
```

Le script compile le code avec `-parameters`, assemble `sprint7.war` et le copie dans le dossier `webapps` de Tomcat. Démarrer ensuite Tomcat une seule fois :

```sh
/Users/mac/Documents/apache-tomcat-10.1.46/bin/catalina.sh run
```

Laisser ce terminal ouvert pendant les tests. Si Tomcat tourne déjà, ne pas lancer une deuxième instance; reconstruire et redéployer le WAR suffit.

## Tester

Ouvrir la page du formulaire dans un navigateur :

```text
http://localhost:8080/sprint7/
```

Ou tester les requêtes depuis un autre terminal :

```sh
curl -i http://localhost:8080/sprint7/
curl -i -X POST -d 'prenom=Naina&message=Bonjour' http://localhost:8080/sprint7/binding/saluer
curl -i -X POST -d 'prenom=Naina' http://localhost:8080/sprint7/binding/saluer
```

Les trois réponses doivent avoir le statut `HTTP/1.1 200`. La dernière requête teste le cas où `message` est absent. La vue résultat est volontairement générique : elle confirme que la méthode a été invoquée, mais n'affiche pas les valeurs des arguments. Pour observer les valeurs elles-mêmes, il faudrait ajouter leur affichage à la vue ou une journalisation temporaire dans le contrôleur.