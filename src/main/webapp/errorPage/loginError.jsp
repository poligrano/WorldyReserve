<jsp:useBean id="error_mx" scope="request" type="java.lang.String"/>
<%@ page contentType="text/html;charset=UTF-8" session="false" isErrorPage="true" %>
<%@ include file="../index.jsp" %>
<script src="ts/dist/Utils.js"></script>
<script>Utils.init_error("${error_mx}")</script>