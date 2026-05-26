<%@ page contentType="text/html;charset=UTF-8" session="false"  %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:if test='${pageContext.request.getSession(false) != null && pageContext.request.getSession(false).getAttribute("uid") != null}'>
    <c:redirect url="load" />
</c:if>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta name="viewport" content="width=device-width,initial-scale=1.0" />
    <title>ProgettoTPSIT – Recupera password</title>
    <link rel="stylesheet" href="css/style.css" />
</head>
<body>
<div class="card">
    <div class="error-group" id="error_flash">
        <strong id="error_mx"></strong>
        <button id="error_button">
            <svg height="16" viewBox="0 0 16 16" width="16" fill="#c05c3a">
                <path d="M3.72 3.72a.75.75 0 0 1 1.06 0L8 6.94l3.22-3.22a.749.749 0 0 1 1.275.326.749.749 0 0 1-.215.734L9.06 8l3.22 3.22a.749.749 0 0 1-.326 1.275.749.749 0 0 1-.734-.215L8 9.06l-3.22 3.22a.751.751 0 0 1-1.042-.018.751.751 0 0 1-.018-1.042L6.94 8 3.72 4.78a.75.75 0 0 1 0-1.06Z"></path>
            </svg>
        </button>
    </div>
    <div class="card-header">
        <h1>Benvenuto</h1>
        <p>Crea il tuo account ProgettoTPSIT</p>
    </div>
    <form action="change" method="post">
        <div class="form-group">
            <label for="email">Indirizzo e-mail</label>
            <input id="email" type="email" name="email" placeholder="nome@esempio.com" required />
            <button id="send" type="button" onclick="Utils.sendCodeEvent('${pageContext.request.scheme}://${pageContext.request.serverName}:${pageContext.request.serverPort}/ProgettoTPSIT_war_exploded/change', 30)">Manda codice</button>
            <p id="timer"></p>
        </div>
        <div class="form-group">
            <label for="pass">Nuova Password</label>
            <input id="pass" type="password" name="pass" placeholder="••••••••" required />
        </div>
        <div class="form-group">
            <label for="code">Codice</label>
            <input id="code" type="text" name="code" placeholder="12345678" minlength="8" maxlength="8" required>
        </div>
        <button type="submit" class="btn-primary">Cambia password</button>
    </form>
</div>
</body>
</html>
<script src="ts/dist/Utils.js"></script>