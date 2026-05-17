<jsp:useBean id="subtitle" scope="request" type="java.lang.String"/>
<jsp:useBean id="mx" scope="request" type="java.lang.String"/>
<jsp:useBean id="title" scope="request" type="java.lang.String"/>
<%@ page contentType="text/html;charset=UTF-8" session="false" %>
<html>
<head>
    <title>Progetto TPSIT - ${title}</title>
</head>
<body>
  <h1>${subtitle}</h1>
  <h3>${mx}</h3>
</body>
</html>
