<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Upload de fichier - Sprint 8</title>
</head>
<body>
    <h1>Upload d'un fichier</h1>
    <%-- L'enctype est indispensable : il permet l'envoi du contenu binaire en multipart. --%>
    <form method="post" enctype="multipart/form-data" action="${pageContext.request.contextPath}/binding/saluer">
        <p>
            <label for="filemap">Fichier</label>
            <%-- Le nom filemap correspond au parametre FileUpload du controleur. --%>
            <input id="filemap" name="filemap" type="file" required>
        </p>
        <button type="submit">Envoyer</button>
    </form>
</body>
</html>
