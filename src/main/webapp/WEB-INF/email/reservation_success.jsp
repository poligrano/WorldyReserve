<jsp:useBean id="verify_URL" scope="request" type="java.lang.String"/>
<jsp:useBean id="id" scope="request" type="java.lang.Long"/>
<jsp:useBean id="end" scope="request" type="java.lang.String"/>
<jsp:useBean id="start" scope="request" type="java.lang.String"/>
<jsp:useBean id="name" scope="request" type="java.lang.String"/>
<jsp:useBean id="userName" scope="request" type="java.lang.String"/>
<%@ page contentType="text/html;charset=UTF-8" session="false" %>
<html>
<head>
  <title>WordlyReserve - Prenota</title>
  <meta charset="UTF-8">
</head>
<body>
<h1>Hai completato la tua prenotazione a nome di ${userName}</h1>
<h2>Grazie per la tua prenotazione all'hotel ${name} dal ${start} al ${end}</h2>
<h3>Se desideri cancellare la prenotazione vai al seguente <a href="${verify_URL}?id=${id}">link</a></h3>
</body>
</html>