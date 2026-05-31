<jsp:useBean id="u" scope="request" type="edu.fauser.tpsit.progettotpsit.entity.User" />
<jsp:useBean id="mx" scope="request" type="java.lang.String"/>
<%@ page import="edu.fauser.tpsit.progettotpsit.obj.DBConnection" %>
<%@ page contentType="text/html;charset=UTF-8" session="false"  %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:if test='${pageContext.request.getSession(false) == null || pageContext.request.getSession(false).getAttribute("uid") == null}'>
  <c:redirect url="index.jsp" />
</c:if>
<!DOCTYPE html>
<html lang="it">
<head>
  <meta name="viewport" content="width=device-width,initial-scale=1.0"/>
  <title>ProgettoTPSIT – Profilo</title>
  <link rel="stylesheet" href="css/change.css">
</head>
<body>
<img class="map-bg" src="svg/change.svg" alt=""/>
<div class="vignette"></div>
<div class="vignette"></div>
<div class="page">
  <div class="error-group" id="error_flash">
    <strong id="error_mx"></strong>
    <button id="error_button">
      <svg height="16" viewBox="0 0 16 16" width="16" fill="#c05c3a">
        <path d="M3.72 3.72a.75.75 0 0 1 1.06 0L8 6.94l3.22-3.22a.749.749 0 0 1 1.275.326.749.749 0 0 1-.215.734L9.06 8l3.22 3.22a.749.749 0 0 1-.326 1.275.749.749 0 0 1-.734-.215L8 9.06l-3.22 3.22a.751.751 0 0 1-1.042-.018.751.751 0 0 1-.018-1.042L6.94 8 3.72 4.78a.75.75 0 0 1 0-1.06Z"></path>
      </svg>
    </button>
  </div>
  <div class="page-header">
    <a href="load" class="back-btn" title="Torna alla home">&#8592;</a>
    <h1>Il tuo profilo</h1>
  </div>
  <form action="changeprofile" method="post" enctype="multipart/form-data">
    <div class="card">
      <div class="card-top-line"></div>
      <div class="card-body">
        <p class="section-label">Foto profilo</p>
          <div class="avatar-row">
            <div class="avatar-wrap">
              <div class="avatar" id="avatarPreview">
                <img id="avatarImg" src="image" alt="!" style="display: block" />
              </div>
              <div class="avatar-edit-btn" id="pfpChangePencil" title="Cambia foto">✎</div>
            </div>
            <div class="avatar-info">
              <p>JPG, PNG o GIF · max 65 KB</p>
              <button class="avatar-upload-btn" type="button" id="pfpChange">
                &#8593; Carica immagine
              </button>
              <input type="file" id="photoInput" name="pfp" accept="image/jpeg, image/png" style="display: none" />
            </div>
          </div>
        </div>
      </div>
    <div class="card">
      <div class="card-top-line"></div>
      <div class="card-body">
        <p class="section-label">Dati personali</p>
        <div class="form-row">
          <div class="form-group">
            <label for="nome">Nome</label>
            <input id="nome" type="text" name="name" placeholder="${u.name()}" value="${u.name()}"/>
          </div>
          <div class="form-group">
            <label for="cognome">Cognome</label>
            <input id="cognome" type="text" name="surname" placeholder="${u.surname()}" value="${u.surname()}"/>
          </div>
        </div>
        <div class="form-group">
          <label for="email">E-mail</label>
          <input id="email" type="email" value="${u.email()}" readonly title="L'email non può essere modificata"/>
        </div>
      </div>
    </div>
    <div class="card">
      <div class="card-top-line"></div>
      <div class="card-body">
        <button class="btn-save">Salva modifiche</button>
      </div>
    </div>
  </form>
</div>
</body>
</html>
<script src="ts/dist/Utils.js"></script>
<script>
  Utils.initAvatar(${DBConnection.MAX_PFP_SIZE});
  <c:if test="${!mx.blank}">
    Utils.initError("${mx}");
  </c:if>
</script>