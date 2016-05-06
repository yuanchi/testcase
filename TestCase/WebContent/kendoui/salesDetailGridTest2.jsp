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
		(function(){
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
							isEffectiveMember: function(input){
								if(input.length == 0// 更動某個欄位，如果會一併動到其他欄位，會造成意外的檢核，所以要先判斷是否有抓到正確待檢核的欄位。
								|| !input.is("[data-bind='value:member']")
								){ 
									return true;
								}
								var td = input.closest("td"),
									fieldName = memberFieldName,
									row = input.closest("tr"),
									grid = row.closest("[data-role='grid']").data("kendoGrid"),
									dataItem = grid.dataItem(row),
									val = dataItem.get(fieldName);
								console.log("validate...dataItem val: " + val + ", input val: " + input.val());
								td.find("div").remove();
							
								if(!input.val()){
									return true;
								}
							
								if(val && !val.id){
									input.attr("data-isEffectiveMember-msg", "請選擇有效會員資料");
									var timer = setInterval(function(){// 在設定input上的錯誤訊息後，kendo ui不見得會即時產生錯誤訊息元素，這導致後續移動元素的動作有時成功、有時失敗，所以設定setInterval
										var div = td.find("div");
										if(div.length > 0){
											clearInterval(timer);
											div.detach().appendTo(td).show(); // 解決錯誤訊息被遮蔽的問題 ref. http://stackoverflow.com/questions/1279957/how-to-move-an-element-into-another-element
										}
									},10);
									return false;
								}
							
								return true;
							}
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
						selectExtraAction: function(model, dataItem){
							model.set("fbName", dataItem.fbNickname);
						}
					}),
					fields = [
			       		//0fieldName			1column title		2column width	3field type	4column filter operator	5field custom		6column custom		7column editor
						[opts.pk,			"SalesDetail ID",	150,			"string",	"eq",					null,				hidden],
						["member",			"會員姓名",			150,			"string",	"contains",				memberField,		memberColumn,		memberEditor],
						["salePoint",		"銷售點",				100,			"string",	"eq",					null,				null],
						["saleStatus",		"狀態",				100,			"string",	"eq"],
						["fbName",			"FB名稱/客人姓名",		150,			"string",	"contains"],
						["activity",		"活動",				150,			"string",	"contains"],
						["modelId",			"型號",				150,			"string",	"startswith"],
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
					],
					modelFields = this.getDefaultModelFields(fields),
					columns = this.getDefaultColumns(fields),
					dataSource = this.getDefaultGridDataSource({modelFields: modelFields}),
					toolbar = [
					{
						name: "create",
					},
					{
						text: " 重查",
						name: "reset",
						iconClass: "k-font-icon k-i-undo-large"
					}];
			
				modelFields["rowId"]["editable"] = false;
				if("incell" === opts.editMode){// in relation with batch update
					toolbar.push({name: "save"});
					toolbar.push({name: "cancel"});
				}
				
				var mainGrid = $(opts.gridId).kendoGrid({
					columns: columns,
					dataSource: dataSource,
					autoBind: false,
					toolbar: toolbar,
					editable: {
						create: true, 
						update: true, 
						destroy: true,
						mode: opts.editMode
					},
					scrollable: true,
					pageable: {
						refresh: true,
						pageSizes: ["5","10","15","20","25","30","all"],
						buttonCount: 13
					},
					sortable: {
						mode: "single",
						allowUnsort: false
					},
					resizable: true,
					navigatable: true,
					filterable: {
						mode: "menu, row",
						extra: true
					},
					selectable: "multiple, cell",
					columnMenu: true,
					dataBinding: function(e){
						//var cell = this.tbody.find("tr[row='row'] td:first");
						//this.current(cell);
						//this.table.focus();
					}
					/*edit事件編輯前觸發一次，編輯完、跳出編輯模式後觸發一次
					edit: function(e){
						var container = e.container, // if edit mode is incell, the container is table cell
							sender = e.sender;
						sender.current().focus();
					}*/
				}).data("kendoGrid");
				
				return mainGrid;
			}
			
			angrycat.kendoGridService
				.init(opts)
				.ready(readyHandler);
		})();			
	</script>
</body>
</html>