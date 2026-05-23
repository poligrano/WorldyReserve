<jsp:useBean id="error_mx" scope="request" type="java.lang.String"/>
<%@ page contentType="text/html;charset=UTF-8" session="false" isErrorPage="true" %>
<script>
  alert('${error_mx}');
</script>
<%@ include file="../change_password.jsp" %>