<%@ page import="edu.fauser.tpsit.progettotpsit.obj.DBConnection" %>
<%@ page contentType="text/html;charset=UTF-8" session="false" errorPage="errorPage/signinError.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:if test='${pageContext.request.getSession(false) != null && pageContext.request.getSession(false).getAttribute("uid") != null}'>
    <c:redirect url="load" />
</c:if>
<html>
<head>
    <title>Progetto TPSIT - Sign In</title>
    <script src="ts/singin.js" defer></script>
</head>
<body>
    <form action="signin" method="post" enctype="multipart/form-data">
        <label>
            Nome:
            <input type="text" name="name" required />
        </label>
        <br />
        <label>
            Cognome:
            <input type="text" name="surname" required />
        </label>
        <br />
        <label>
            E-Mail:
            <input type="email" name="email" required />
        </label>
        <br />
        <label>
            Password:
            <input type="password" name="pass" required />
        </label>
        <br />
        <label>
            Profile Picture:
            <input type="file" id="pfp" name="pfp" accept="image/jpeg, image/png" onchange="pfpOnChange(${DBConnection.MAX_PFP_SIZE})" />
        </label>
        <br />
        <input type="submit" value="Registrati">
        <br />
    </form>
</body>
</html>