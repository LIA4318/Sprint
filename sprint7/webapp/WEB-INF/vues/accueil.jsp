<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Binding des paramètres - Sprint 7</title>
</head>
<body>
    <h1>Binding des paramètres</h1>
    <form method="post" action="${pageContext.request.contextPath}/binding/saluer">
        <p>
            <label for="prenom">Prénom</label>
            <input id="prenom" name="prenom" type="text" required>
        </p>
        <p>
            <label for="message">Message (facultatif)</label>
            <input id="message" name="message" type="text">
        </p>
        <button type="submit">Envoyer</button>
    </form>
</body>
</html>