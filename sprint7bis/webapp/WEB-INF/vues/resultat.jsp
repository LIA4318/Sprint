<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Résultat du binding d'objet - Sprint 7bis</title>
</head>
<body>
    <%!
        // Echappe les caracteres HTML pour afficher sans risque les valeurs saisies.
        private String escapeHtml(String value) {
            if (value == null) return "";
            return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                    .replace("\"", "&quot;").replace("'", "&#39;");
        }
    %>
    <h1>Objet reçu par le contrôleur</h1>
    <%-- personne est l'objet construit et rempli avant l'invocation du controleur. --%>
    <dl>
        <dt>Prénom</dt>
        <dd><%= escapeHtml(((models.Personne) request.getAttribute("personne")).getPrenom()) %></dd>
        <dt>Message</dt>
        <dd><%= escapeHtml(((models.Personne) request.getAttribute("personne")).getMessage()) %></dd>
        <dt>Âge</dt>
        <dd><%= ((models.Personne) request.getAttribute("personne")).getAge() %></dd>
    </dl>
    <p><a href="${pageContext.request.contextPath}/">Retour au formulaire</a></p>
</body>
</html>