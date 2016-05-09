<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="sf"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>

<c:set value="${pageContext.request.contextPath}" var="rootPath"/>
<c:set value="salesdetail" var="moduleName"/>
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
	<title>Kendo UI SalesDetail Grid Test Page</title>
	
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
					memberFieldName = "member",
					memberField = {
						type: null,
						validation: {
							isEffectiveMember: this.getDefaultFieldAutoCompleteValidation({
								field: memberFieldName,
								method: "isEffectiveMember",
								validate: function(opts){
									var val = opts.val;
									return val && !val.id;
								},
								msg: "請選擇有效會員資料"
							})
						}
					},
					memberColumn = {template: "<span title='#=(member ? member.name : '')#'>#=(member ? member.name : '')#</span>"},
					memberEditor = this.getAutoCompleteEditor({
						textField: "name",
						valueField: "id",
						readUrl: opts.moduleBaseUrl + "/queryMemberAutocomplete.json", 
						filter: "contains", 
						//template: "<span>#: name # | #: nameEng #</span>",
						autocompleteFieldsToFilter: ["name", "nameEng", "idNo"],
						errorMsgFieldName: memberFieldName,
						selectAction: function(model, dataItem){
							model.set(memberFieldName, dataItem);
							model.set("fbName", dataItem.fbNickname);
							model.set("idNo", dataItem.idNo);
						}
					}),
					modelIdFieldName = "modelId",
					modelIdEditor = this.getAutoCompleteEditor({
						textField: "modelId",
						readUrl: opts.moduleBaseUrl + "/queryProductAutocomplete.json", 
						filter: "contains", 
						autocompleteFieldsToFilter: ["modelId", "nameEng"],
						selectAction: function(model, dataItem){
							model.set(modelIdFieldName, dataItem.modelId);
							model.set("productName", dataItem.nameEng);
						}
					}),				
					fields = [
			       		//0fieldName		1column title		2column width	3field type	4column filter operator	5field custom		6column custom		7column editor
						[opts.pk,			"SalesDetail ID",	150,			"string",	"eq",					null,				hidden],
						[memberFieldName,	"會員姓名",			150,			"string",	"contains",				memberField,		memberColumn,		memberEditor],
						["salePoint",		"銷售點",				100,			"string",	"eq",					null,				null],
						["saleStatus",		"狀態",				100,			"string",	"eq"],
						["fbName",			"FB名稱/客人姓名",		150,			"string",	"contains"],
						["activity",		"活動",				150,			"string",	"contains"],
						[modelIdFieldName,	"型號",				150,			"string",	"startswith",			null,				null,				modelIdEditor],
						["productName",		"明細",				150,			"string",	"contains"],
						["price",			"定價",				100,			"number",	"gte"],
						["memberPrice",		"會員價(實收價格)",		100,			"number",	"gte"],
						["priority",		"順序",				150,			"string",	"eq",					null,				hidden],
						["orderDate",		"銷售日期",			150,			"date",		"gte"],
						["otherNote",		"其他備註",			150,			"string",	"contains",				null,				hidden],
						["checkBillStatus",	"對帳狀態",			150,			"string",	"contains"],
						["idNo",			"身份證字號",			150,			"string",	"contains"],
						["discountType",	"折扣説明",			150,			"string",	"contains"],
						["arrivalStatus",	"已到貨",				150,			"string",	"eq",					null,				hidden],
						["shippingDate",	"出貨日",				150,			"date",		"gte"],
						["sendMethod",		"郵寄方式",			150,			"string",	"eq"],
						["note",			"備註",				150,			"string",	"contains",				null,				hidden],
						["payDate",			"付款日期",			150,			"date",		"gte"],
						["contactInfo",		"郵寄地址電話",		150,			"string",	"contains",				null,				hidden],
						["registrant",		"登單者",				150,			"string",	"contains",				null,				hidden],
						["rowId",			"Excel序號",			150,			"string",	"contains",				uneditable,			hidden]
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