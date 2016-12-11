<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Admin Panel</title>
    <link rel="stylesheet" type="text/css" href="<c:url value='/css/bootstrap.min.css'/>">
    <!-- Optional Bootstrap theme -->
    <link rel="stylesheet" href="<c:url value='/css/bootstrap-theme.min.css'/>">
</head>
<body>
	<div class="container">
    	<h1>Admin Panel</h1>
	    <div class="row">
	        <div class="col-lg-12">
				<form action="" method="POST" enctype="application/x-www-form-urlencoded">
				    <div class="form-group">
<c:if test="${not empty requestScope[dropdownOptionsAttributeName]}">
				        <label for="inputParamOne">Example Context Param</label>
		                <select class="form-control" id="inputParamOne" name="paramOne">
	<c:forEach var="option" items="${requestScope[dropdownOptionsAttributeName]}">
		                    <option value="${option.value}"${(option.selected) ? ' selected="selected"' : ''}${(option.disabled) ? ' disabled' : ''}>${option.label}</option>
	</c:forEach>
		                </select>
</c:if>
				    </div>
				    <button type="submit" class="btn btn-primary">Submit</button>
				</form>
	        </div>
	    </div>
	</div>
	<script src="<c:url value='/js/jquery-3.1.1.min.js'/>"></script>
    <script src="<c:url value='/js/bootstrap.min.js'/>"></script>
</body>
</html>