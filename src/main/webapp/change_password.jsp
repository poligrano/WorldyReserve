<%@ page contentType="text/html;charset=UTF-8" session="false" errorPage="errorPage/change_passwordError.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:if test='${pageContext.request.getSession(false) != null && pageContext.request.getSession(false).getAttribute("uid") != null}'>
    <c:redirect url="load" />
</c:if>
<html>
<head>
    <title>Proggetto TPSIT - Recupera</title>
    <script src="ts/change_password.js" defer></script>
</head>
<body>
    <form action="change" method="post">
        <label>
            E-Mail:
            <input type="email" id="email" name="email" required />
        </label>
        <br />
        <label>
            Nuova Password:
            <input type="password" name="pass" required />
        </label>
        <br />
        <label>
            Codice:
            <input type="text" name="code" maxlength="8" minlength="8" required />
        </label>
        <br />
        <input type="button" id="send" value="Manda Codice" onclick="sendCodeEvent('${pageContext.request.scheme}://${pageContext.request.serverName}:${pageContext.request.serverPort}/ProgettoTPSIT_war_exploded/change', 30)" />
        <p id="timer">00:30</p>
        <br />
        <input type="submit" value="Cambia Password" />
    </form>
</body>
</html>
