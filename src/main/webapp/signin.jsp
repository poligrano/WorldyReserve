<%@ page contentType="text/html;charset=UTF-8" language="java" session="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:if test='${pageContext.request.getSession(false) != null && pageContext.request.getSession(false).getAttribute("uid") != null}'>
    <c:redirect url="load" />
</c:if>
<html>
<head>
    <title>Progetto TPSIT - Sign In</title>
</head>
<body>
    <form action="signin" method="post" enctype="multipart/form-data">
        <label>
            Nome:
            <input type="text" name="name" required />
        </label>
        <label>
            Cognome:
            <input type="text" name="surname" required />
        </label>
        <label>
            E-Mail:
            <input type="email" name="email" required />
        </label>
        <label>
            Password:
            <input type="password" name="pass" required />
        </label>
        <label>
            Profile Picture:
            <input type="file" name="pfp" accept="image/jpeg" />
        </label>
    </form>
</body>
</html>