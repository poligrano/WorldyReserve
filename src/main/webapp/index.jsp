<%@ page contentType="text/html;charset=UTF-8" session="false" errorPage="errorPage/loginError.jsp" %>
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
</body>
</html>
