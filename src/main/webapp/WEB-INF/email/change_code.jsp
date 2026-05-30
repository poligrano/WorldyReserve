<jsp:useBean id="code" scope="request" type="java.lang.String"/>
<%@ page contentType="text/html;charset=UTF-8" session="false" %>
<html>
<head>
  <title>Progetto TPSIT - Verifica</title>
</head>
<body>
<h1>Codice per il cambio della password: ${code}</h1>
<h2 style="color: darkred">Hai circa 5 minuti per completare la verifica</h2>
</body>
</html>