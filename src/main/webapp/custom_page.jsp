<jsp:useBean id="subtitle" scope="request" type="java.lang.String"/>
<jsp:useBean id="mx" scope="request" type="java.lang.String"/>
<jsp:useBean id="title" scope="request" type="java.lang.String"/>
<%@ page contentType="text/html;charset=UTF-8" session="false"  %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta name="viewport" content="width=device-width,initial-scale=1.0" />
    <title>ProgettoTPSIT – ${title}</title>
    <link rel="stylesheet" href="css/style.css" />
</head>
<body>
<div class="card">
    <div class="card-header">
        <h1>${subtitle}</h1>
        <p class="full-divider"></p>
        <p>${mx}</p>
    </div>
</div>
</body>
</html>