<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Binding d'objet - Sprint 7bis</title>
</head>
<body>
    <h1>Binding d'un objet</h1>
    <%-- Les noms des champs correspondent aux attributs de l'objet Personne. --%>
    <form method="post" action="${pageContext.request.contextPath}/binding/saluer">
        <p>
            <label for="prenom">Prénom</label>
            <input id="prenom" name="prenom" type="text" required>
        </p>
        <p>
            <label for="message">Message (facultatif)</label>
            <input id="message" name="message" type="text">
        </p>
        <p>
            <label for="age">Âge (facultatif)</label>
            <input id="age" name="age" type="number" min="0">
        </p>
        <button type="submit">Envoyer</button>
    </form>
</body>
</html>