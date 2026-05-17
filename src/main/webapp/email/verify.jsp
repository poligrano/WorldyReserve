<jsp:useBean id="verify_URL" scope="request" type="java.lang.String"/>
<jsp:useBean id="code" scope="request" type="java.lang.String"/>
<%@ page contentType="text/html;charset=UTF-8" session="false" %>
<html>
<head>
    <title>Progetto TPSIT - Verifica</title>
</head>
<body>
    <em>Clicca il seguente <a href="${verify_URL}?code=${code}">link</a> per procedere alla verifica della tua e-mail</em>
    <h2 style="color: darkred">Hai circa 20 minuti per completare la verifica</h2>
</body>
</html>
