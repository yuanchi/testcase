<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="sf"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>

<c:set value="${pageContext.request.contextPath}" var="rootPath"/>
<c:set value="member2" var="moduleName"/>
<c:set value="${moduleName}KendoData" var="kendoDataKey"/>
<c:set value="${rootPath}/${moduleName}" var="moduleBaseUrl"/>
<c:set value="${rootPath}/kendoui/professional.2016.1.226.trial" var="kendouiRoot"/>
<c:set value="${kendouiRoot}/styles" var="kendouiStyle"/>
<c:set value="${kendouiRoot}/js" var="kendouiJs"/>
<c:set value="${rootPath}/angrycat" var="angrycatRoot"/>
<c:set value="${angrycatRoot}/js" var="angrycatJs"/>
<c:set value="${angrycatRoot}/styles" var="angrycatStyle"/>
<c:set value="${rootPath}/bootstrap/3.3.5" var="bootstrapRoot"/>
<c:set value="${bootstrapRoot}/css" var="bootstrapCss"/>
   
<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8"/>
	<meta content="width=device-width, initial-scale=1.0" name="viewport">
	<title>Kendo UI Parameter Grid Test Page</title>
	
	<link rel="stylesheet" href="${kendouiStyle}/kendo.common.min.css">
	<link rel="stylesheet" href="${kendouiStyle}/kendo.default.min.css">
	<link rel="stylesheet" href="${angrycatStyle}/kendo.grid.css">
	
	<link rel="stylesheet" href="${bootstrapCss}/bootstrap.css">
	<link rel="stylesheet" href="${bootstrapCss}/bootstrap-theme.css">
	
	<script type="text/javascript" src="${kendouiJs}/jquery.min.js"></script>
	<script type="text/javascript" src="${kendouiJs}/jquery.cookie.js"></script>
	<script type="text/javascript" src="${kendouiJs}/kendo.web.min.js"></script>
	<script type="text/javascript" src="${kendouiJs}/messages/kendo.messages.zh-TW.min.js"></script>


</head>
<body>

<div>
	<nav role="navigation" class="navbar navbar-default navbar-fixed-top">
		<div class="container">
			<div class="navbar-header">
				<button type="button" data-target="#navbarCollapse" data-toggle="collapse" class="navbar-toggle">
					<span class="sr-only">Toggle navigation</span>
					<span class="icon-bar"></span>
					<span class="icon-bar"></span>
					<span class="icon-bar"></span>
				</button>
				<a href="#" class="navbar-brand">Angrycat</a>
			</div>
			<div id="navbarCollapse" class="collapse navbar-collapse">
				<ul class="nav navbar-nav">
					<li>
						<a href="${rootPath}/parameterGridTest.jsp">參數測試</a>
					</li>										
				</ul>
			</div>		
		</div>
	</nav>
</div>

<div class="container">

<div class="well">
</div>
	<span id="updateInfoWindow" style="display:none;"></span>
	<div id="mainGrid"></div>
	<div id="updateNoti"></div>
</div>	
	
	
	<script type="text/javascript" src="${angrycatJs}/angrycat.js"></script>
	<script type="text/javascript" src="${angrycatJs}/angrycat.kendo.grid.js"></script>	
	<script type="text/javascript">
		(function(angrycat, $, kendo){
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
			
			function fieldsReadyHandler(){
				var context = this,
					hidden = {hidden: true},
					catFieldName = "parameterCategory",
					catField = {
						type: null,
						validation: {
							isEffectiveCat: context.getDefaultFieldAutoCompleteValidation({
								field: catFieldName,
								required: true, // 加上必填檢核
								method: "isEffectiveCat",
								validate: function(opts){
									var val = opts.val;
									return val && !val.id;
								},
								msg: "請選擇有效類別資料"
							})
						}
					},
					catColumn = {
						template: "<span title='#=(parameterCategory ? parameterCategory.name : '')#'>#=(parameterCategory ? parameterCategory.name : '')#</span>",
						filterable: {
							cell: {
								template: function(args){
									context.getDefaultRemoteDropDownList({
										ele: args.element,
										action: "queryParameterCatDropDownList",
										dataTextField: "name",
										dataValueField: "id"
									});
								},
								showOperators: false
							}
						}
					},
					catEditor = context.getAutoCompleteCellEditor({
						textField: "name",
						valueField: "id",
						action: "queryParameterCatAutocomplete", 
						filter: "contains",
						autocompleteFieldsToFilter: ["name"],
						errorMsgFieldName: catFieldName
					}),					
					fields = [
			       		//0fieldName	1column title		2column width	3field type	4column filter operator	5field custom		6column custom		7column editor
						[opts.pk,		"ID",				150,			"string",	"eq",					null,				hidden],
						[catFieldName,	"類別名稱",			150,			"string",	"contains",				catField,			catColumn,			catEditor],
						["code",		"代碼",				150,			"string",	"contains"],
						["nameDefault",	"名稱",				100,			"string",	"contains"],
						["note",		"備註",				100,			"string",	"contains"],
						["sequence",	"順序",				100,			"number",	"eq"],
					];
				return fields;
			}
			
			angrycat.kendoGridService
				.init(opts)
				.fieldsReady(fieldsReadyHandler);
		})(angrycat, jQuery, kendo);			
	</script>
</body>
</html>