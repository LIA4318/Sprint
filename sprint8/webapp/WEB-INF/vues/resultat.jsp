<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Résultat de l'upload - Sprint 8</title>
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
    <h1>Objet fichier reçu par le contrôleur</h1>
    <%-- filemap est l'objet FileUpload construit par le framework avant l'invocation. --%>
    <% lcsfw.fw.file.FileUpload filemap =
            (lcsfw.fw.file.FileUpload) request.getAttribute("filemap"); %>
    <dl>
        <dt>Champ HTML</dt>
        <dd><%= escapeHtml(filemap.getFieldName()) %></dd>
        <dt>Nom du fichier</dt>
        <dd><%= escapeHtml(filemap.getFileName()) %></dd>
        <dt>Extension</dt>
        <dd><%= escapeHtml(filemap.getExtension()) %></dd>
        <dt>Type MIME</dt>
        <dd><%= escapeHtml(filemap.getContentType()) %></dd>
        <dt>Taille</dt>
        <dd><%= filemap.getContent().length %> octets</dd>
    </dl>
    <p><a href="${pageContext.request.contextPath}/">Retour au formulaire</a></p>
</body>
</html>
