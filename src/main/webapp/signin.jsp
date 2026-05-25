<%@ page import="edu.fauser.tpsit.progettotpsit.obj.DBConnection" %>
<%@ page import="edu.fauser.tpsit.progettotpsit.singleton.EnvVar" %>
<%@ page contentType="text/html;charset=UTF-8" session="false"  %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:if test='${pageContext.request.getSession(false) != null && pageContext.request.getSession(false).getAttribute("uid") != null}'>
    <c:redirect url="load" />
</c:if>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta name="viewport" content="width=device-width,initial-scale=1.0" />
    <title>ProgettoTPSIT – Signin</title>
    <link rel="stylesheet" href="css/logsign.css" />
    <script src="ts/generic_error.js" defer></script>
    <script src="ts/singin.js" defer></script>
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
    <form action="signin" method="post" enctype="multipart/form-data">
        <div class="form-group">
            <label for="pfp">Foto Profilo</label>
            <input id="pfp" type="file" name="pfp" accept="image/jpeg, image/png" onchange="pfpOnChange(${DBConnection.MAX_PFP_SIZE})" />
        </div>
        <div class="form-group">
            <label for="name">Nome</label>
            <input id="name" type="text" name="name" placeholder="Tuo Nome" required />
        </div>
        <div class="form-group">
            <label for="surname">Cognome</label>
            <input id="surname" type="text" name="surname" placeholder="Tuo Cognome" required />
        </div>
        <div class="form-group">
            <label for="email">Indirizzo e-mail</label>
            <input id="email" type="email" name="email" placeholder="nome@esempio.com" required />
        </div>
        <div class="form-group">
            <label for="pass">Password</label>
            <input id="pass" type="password" name="pass" placeholder="••••••••" required />
            <div class="forgot">
                <a href="change_password.jsp">Password dimenticata?</a>
            </div>
        </div>
        <button type="submit" class="btn-primary">Registrati</button>
    </form>
    <div class="divider">oppure</div>
    <script src="https://accounts.google.com/gsi/client" async></script>
    <div id="g_id_onload" data-context="signup" data-client_id="${EnvVar.instance().env.get("WEB_CLIENT_ID")}" data-login_uri="ProgettoTPSIT_war_exploded/google"></div>
    <div class="g_id_signin" data-type="standard"></div>
    <p class="register-link">
        Hai già un profilo? <a href="index.jsp">Accedi</a>
    </p>
</div>
</body>
</html>