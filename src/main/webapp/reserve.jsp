<%@ page contentType="text/html;charset=UTF-8" session="false"  %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:if test='${pageContext.request.getSession(false) == null || pageContext.request.getSession(false).getAttribute("uid") == null}'>
  <c:redirect url="index.jsp" />
</c:if>
<!DOCTYPE html>
<html lang="it">
<head>
  <meta name="viewport" content="width=device-width,initial-scale=1.0" />
  <title>WorldyReserve – Prenota</title>
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
    <h1>Prenota un hotel</h1>
    <p>Completa la tua prenotazione all'hotel ${param.name}</p>
  </div>
  <form action="reserve" method="post" onsubmit="return Utils.onSubmitCheck()">
    <input type="hidden" name="id" value="${param.id}" />
    <input type="hidden" name="name" value="${param.name}" />
    <input type="hidden" name="start-epoch" id="start-val" />
    <input type="hidden" name="end-epoch" id="end-val" />
    <div class="form-group">
      <label for="start">Dal giorno</label>
      <input id="start" name="start" type="datetime-local" required />
    </div>
    <div class="form-group">
      <label for="end">Fino al</label>
      <input id="end" name="end" type="datetime-local" required />
    </div>
    <button type="submit" class="btn-primary">Prenota</button>
  </form>
</div>
</body>
</html>
<script src="ts/dist/Utils.js"></script>
<script>
  Utils.initSendReserve();
</script>