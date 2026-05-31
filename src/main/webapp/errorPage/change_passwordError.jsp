<jsp:useBean id="error_mx" scope="request" type="java.lang.String"/>
<%@ page contentType="text/html;charset=UTF-8" session="false" isErrorPage="true" %>
<%@ include file="../change_password.jsp" %>
<script>Utils.initError("${error_mx}")</script>