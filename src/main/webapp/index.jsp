<%@ page contentType="text/html;charset=UTF-8" language="java" session="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:if test='${pageContext.request.getSession(false) != null && pageContext.request.getSession(false).getAttribute("uid") != null}'>
    <c:redirect url="login" />
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
            <input type="password" name="password" required />
        </label>
        <br />
        <input type="submit" value="Accedi" />
        <br />
    </form>
</body>
</html>
