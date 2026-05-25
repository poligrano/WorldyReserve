<jsp:useBean id="error_mx" scope="request" type="java.lang.String"/>
<%@ page contentType="text/html;charset=UTF-8" session="false" isErrorPage="true" %>
<%@ include file="../index.jsp" %>
<script src="ts/generic_error.js"></script>
<script>init_error("${error_mx}")</script>