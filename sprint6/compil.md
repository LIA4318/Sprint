Reconstruire et deployer l'application depuis les sources du Sprint 6 :

```sh
sh build-deploy.sh
```

Demarrer Tomcat (si le serveur n'est pas deja lance) :

```sh
/tmp/apache-tomcat-10.1.44/bin/catalina.sh run
```

Tester l'accueil et la reponse JSON :

```sh
curl -i http://localhost:8080/sprint4/
curl -i http://localhost:8080/sprint4/api/test
```