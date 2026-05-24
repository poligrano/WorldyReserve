<%@ page import="edu.fauser.tpsit.progettotpsit.singleton.EnvVar" %>
<%@ page contentType="text/html;charset=UTF-8" session="false"  %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:if test='${pageContext.request.getSession(false) != null && pageContext.request.getSession(false).getAttribute("uid") != null}'>
    <c:redirect url="load" />
</c:if>
<html>
<head>
    <title>ProgettoTPSIT - Login</title>
</head>
<body>
    <form action="login" method="post">
        <label>
            E-Mail:
            <input type="email" name="email" required />
        </label>
        <br />
        <label>
            Password:
            <input type="password" name="pass" required />
        </label>
        <a href="change_password.jsp">Password dimenticata?</a>
        <br />
        <input type="submit" value="Accedi" />
        <br />
    </form>
    <p>Non hai ancora un profilo? <a href="signin.jsp">Registrati</a></p>
    <script src="https://accounts.google.com/gsi/client" async></script>
    <div id="g_id_onload" data-client_id="${EnvVar.instance().env.get("WEB_CLIENT_ID")}" data-login_uri="${pageContext.request.scheme}://${pageContext.request.serverName}:${pageContext.request.serverPort}/ProgettoTPSIT_war_exploded/google"></div>
    <div class="g_id_signin"></div>
</body>
</html>
