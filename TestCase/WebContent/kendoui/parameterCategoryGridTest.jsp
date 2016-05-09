<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="sf"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>

<c:set value="${pageContext.request.contextPath}" var="rootPath"/>
<c:set value="parametercategory" var="moduleName"/>
<c:set value="${moduleName}KendoData" var="kendoDataKey"/>
<c:set value="${rootPath}/${moduleName}" var="moduleBaseUrl"/>
<c:set value="${rootPath}/kendoui/professional.2016.1.226.trial" var="kendouiRoot"/>
<c:set value="${kendouiRoot}/styles" var="kendouiStyle"/>
<c:set value="${kendouiRoot}/js" var="kendouiJs"/>
<c:set value="${rootPath}/angrycat" var="angrycatRoot"/>
<c:set value="${angrycatRoot}/js" var="angrycatJs"/>
<c:set value="${angrycatRoot}/styles" var="angrycatStyle"/> 
   
<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8"/>
	<meta content="width=device-width, initial-scale=1.0" name="viewport">
	<title>Kendo UI SalesDetail Grid Test Page</title>
	
	<link rel="stylesheet" href="${kendouiStyle}/kendo.common.min.css">
	<link rel="stylesheet" href="${kendouiStyle}/kendo.default.min.css">
	<link rel="stylesheet" href="${angrycatStyle}/kendo.grid.css">
	
	<script type="text/javascript" src="${kendouiJs}/jquery.min.js"></script>
	<script type="text/javascript" src="${kendouiJs}/jquery.cookie.js"></script>
	<script type="text/javascript" src="${kendouiJs}/kendo.web.min.js"></script>
	<script type="text/javascript" src="${kendouiJs}/messages/kendo.messages.zh-TW.min.js"></script>


</head>
<body>
	<span id="updateInfoWindow" style="display:none;"></span>
	<div id="mainGrid"></div>
	<div id="updateNoti"></div>
	
	
	
	<script type="text/javascript" src="${angrycatJs}/angrycat.js"></script>
	<script type="text/javascript" src="${angrycatJs}/angrycat.kendo.grid.js"></script>	
	<script type="text/javascript">
		(function($, kendo, angrycat){
			var lastKendoData = ${sessionScope[kendoDataKey] == null ? "null" : sessionScope[kendoDataKey]},
			opts = {
				moduleName: "${moduleName}",
				moduleBaseUrl: "${moduleBaseUrl}",
				gridId: "#mainGrid",
				notiId: "#updateNoti",
				updateInfoWindowId: "#updateInfoWindow",
				page: 1,
				pageSize: 15,
				filter: null,
				sort: null,
				group: null,
				editMode: "incell",
				pk: "id",
				lastKendoData: lastKendoData
			};
			
			function readyHandler(){
				var hidden = {hidden: true},
					uneditable = {editable: false},			
					fields = [
			       		//0fieldName		1column title		2column width	3field type	4column filter operator	5field custom		6column custom		7column editor
						[opts.pk,			"ID",				150,			"string",	"eq",					null,				hidden],
						["name",			"名稱",				150,			"string",	"contains"],
						["type",			"類型",				100,			"number",	"eq",					uneditable,			null],
					];
				
				return fields;
			}
			
			angrycat.kendoGridService
				.init(opts)
				.fieldsReady(readyHandler);
		})(jQuery, kendo, angrycat);			
	</script>
</body>
</html>