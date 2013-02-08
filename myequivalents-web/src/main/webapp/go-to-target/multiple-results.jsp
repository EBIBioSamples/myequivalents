<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>


<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Insert title here</title>
</head>
<body>
<h1>myEquivalents Web Service</h1>

<p>The source entity ${it.entityId} has multiple equivalents at ${it.targetServiceName}. Please choose one.
<ul><c:forEach var="entity" items = "${it.resultingEntities}" >
  <li><a href = "${entity.URI}">${entity.serviceName}:${entity.accession}</a></li>
</c:forEach></ul>    
</p>
</body>
</html>