<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="sf"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>

<c:set value="${pageContext.request.contextPath}" var="rootPath"/>
<c:set value="salesdetail" var="moduleName"/>
<c:set value="${rootPath}/${moduleName}" var="moduleBaseUrl"/>
<c:set value="${rootPath}/kendoui/professional.2016.1.226.trial" var="kendouiRoot"/>
<c:set value="${kendouiRoot}/styles" var="kendouiStyle"/>
<c:set value="${kendouiRoot}/js" var="kendouiJs"/> 
   
<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8"/>
	<meta content="width=device-width, initial-scale=1.0" name="viewport">
	<title>Kendo UI SalesDetail Grid Test Page</title>
	
	<link rel="stylesheet" href="${kendouiStyle}/kendo.common.min.css">
	<link rel="stylesheet" href="${kendouiStyle}/kendo.default.min.css">
	
	<script type="text/javascript" src="${kendouiJs}/jquery.min.js"></script>
	<script type="text/javascript" src="${kendouiJs}/jquery.cookie.js"></script>
	<script type="text/javascript" src="${kendouiJs}/kendo.web.min.js"></script>
	<script type="text/javascript" src="${kendouiJs}/messages/kendo.messages.zh-TW.min.js"></script>

	<style type="text/css">
	.k-grid tbody tr td {
    	overflow: hidden;
    	text-overflow: ellipsis;
    	white-space: nowrap;
	}
	</style>
</head>
<body>
	<div id="updateInfoWindow"></div>
	<div id="mainGrid">
	</div>
	<script type="text/javascript">
		var moduleName = 'salesDetail',
			cookieKey = moduleName + "State",
			gridId = "#mainGrid",
			updateInfoWindowId = "#updateInfoWindow",
			DEFAULT_PAGE_VALUE = 1,
			DEFAULT_PAGESIZE_VALUE = 15,
			DEFAULT_FILTER_VALUE = null,
			DEFAULT_SORT_VALUE = null,
			DEFAULT_GROUP_VALUE = null,
			KENDO_UI_TYPE_DATE = "date",
			KENDO_UI_TYPE_STRING = "string",
			KENDO_UI_TYPE_BOOLEAN = "boolean",
			KENDO_UI_TYPE_NUMBER = "number",
			pk = "id",
			fieldsToFilter = ["modelId", "productName"],
			filterableMessages = {
				filter: "篩選",
				clear: "清除篩選",					
				and: "而且",
				or: "或",
				isFalse: "不是",
				isTrue: "是",
				selectValue: "請選擇",
				cancel: "清除",
				operator: "運算符號",
				value: "值",
				checkAll: "全選"
			},
			filterableStringOperators = {
				eq: "等於",
				neq: "不等於",
				isnull: "是空值",
				isnotnull: "不是空值",
				startswith: "開頭是",
				contains: "包含",
				doesnotcontain: "不包含",
				endswith: "結尾是"
			};		
	</script>
	<script type="text/javascript">
	function removeTimezoneOffset(d){
		var hours = d.getHours(),
			mins = d.getMinutes(),
			secs = d.getSeconds(),
			milliSecs = d.getMilliseconds();
		d.setHours(hours, mins-d.getTimezoneOffset(), secs, milliSecs); // GMT+0800 timezoneoffset is -480
		return d;
	}
	// 加上timezoneoffset
	function addTimezoneOffset(d){
		var hours = d.getHours(),
			mins = d.getMinutes(),
			secs = d.getSeconds(),
			milliSecs = d.getMilliseconds();
		d.setHours(hours, mins+d.getTimezoneOffset(), secs, milliSecs);
		return d;				
	}
	function removeFilterDateTimezoneOffset(filter, modelFields){
		if(!filter){
			return;
		}
		if(filter.filters){
			for(var i = 0; i < filter.filters.length; i++){
				var f = filter.filters[i];
				removeFilterDateTimezoneOffset(f, modelFields);
			}
		}
		if(filter.field && KENDO_UI_TYPE_DATE == modelFields[filter.field].type && filter.value && (filter.value instanceof Date)){
			filter.value = removeTimezoneOffset(filter.value);
		}
	}
	function parseFilterDates(filter, fields){
		if(filter.filters){
			for(var i = 0; i < filter.filters.length; i++){
				parseFilterDates(filter.filters[i], fields);
			}
		}else{
			if(fields[filter.field].type == KENDO_UI_TYPE_DATE){
				// console.log('store cookie date: ' + filter.value);
				filter.value = addTimezoneOffset(kendo.parseDate(filter.value));
				// console.log('transform cookie date: ' + filter.value);
			}
		}
	}
	function changeFilterToMulti(filterObj, fields){
		if(!filterObj.logic && !filterObj.filters){
			return;
		}
		filterObj.logic = "or";
		var filters = filterObj.filters,
			oriFilter = filters[0];
		for(var i = 0; i < fields.length; i++){
			var field = fields[i];
			filters.push($.extend({}, oriFilter, {field: field}));
		}
		return filterObj;
	}
	function getAutoCompleteEditor(settings){
		var textField = settings.textField,
			readUrl = settings.readUrl
			filter = settings.filter ? settings.filter : "contains",
			template = settings.template;
		return function(container, options){
			$('<input data-text-field="'+ textField +'" data-bind="value:'+ options.field +'"/>')
				.appendTo(container)
				.kendoAutoComplete({
					filter: filter,
					template: template,
					// autoBind: false, // 如果加上這行，會出現e._preselect is not a function錯誤訊息，根據官方說法，這是因為autocomplete沒有支援deferred binding
					valuePrimitive: true, // 如果不設定valuePrimitive，選了值之後，他會顯示[object Object]
					dataTextField: textField,
					dataSource: {
						serverFiltering: true,
						transport: {
							read:{
								url: readUrl,
								type: "POST",
								dataType: "json",
								contentType: "application/json;charset=utf-8",
								cache: false	
							},
							parameterMap: function(data, type){
								if(type === "read"){
									// console.log("data: " + JSON.stringify(data));
									var r = {
										conds: {
											currentPage: 1,
											countPerPage: 20,
											filter: changeFilterToMulti(data.filter, fieldsToFilter) // autocomplete元件只有支援單一filter條件，這裡可以將他轉為多個filter條件
										}
									};
									return JSON.stringify(r);
								}
							}
						},
						schema:{
							type: "json",
							data: function(response){
								var results = response.results; // read
								return results;
							}
						}
					},
					select: function(e){
						var item = e.item;
						var text = item.text(); // text is template result
						//console.log("item: " + JSON.stringify(item) + ", text: " + text);
					}
				});
		};
	}
	function getDefaultModelFields(fields){
		var modelFields = {};
		for(var i = 0; i < fields.length; i++){
			var field = fields[i],
				fieldName = field[0],
				type = field[3],
				customOpt = field[5];
			modelFields[fieldName] = {
				defaultValue: null,
				type: type
			};
			if(fieldName === pk){
				modelFields[fieldName]["editable"] = false;
			}
			if(customOpt){
				$.extend(modelFields[fieldName], customOpt);
			}
		}
		return modelFields;
	}
	function getDefaultColumns(fields){
		var columns = [];
		for(var i = 0; i < fields.length; i++){
			var field = fields[i],
				fieldName = field[0],
				width = field[2];
			var column = {
					field: fieldName,
					width: width+"px",
					title: field[1],
					filterable: {
						cell: {
							operator: field[4]
						}
					},
					template: "<span title='#="+ fieldName +"#'>#="+ fieldName +"#</span>"
				};
			if("date" === field[3]){
				column["format"] = "{0:yyyy-MM-dd}";
				column["parseFormats"] = "{0:yyyy-MM-dd}";
				column["filterable"]["ui"] = "datetimepicker";
				column["template"] = "<span title='#= kendo.toString(" + fieldName +", \"u\")#'>#= kendo.toString("+ fieldName +", \"u\")#</span>";
			}
			if(field[6]){
				$.extend(column, field[6]);
			}
			columns.push(column);
		}
		columns.push({
			command: ["destroy"],
			width: "100px"
		});
		return columns;
	}
	function getDefaultRemoteConfig(moduleBaseUrl, action){
		return {
			url: (moduleBaseUrl + "/" + action + ".json"),
			type: "POST",
			dataType: "json",
			contentType: "application/json;charset=utf-8",
			cache: false
		};
	}
	function getDefaultGridDataSource(options){
		var moduleBaseUrl = options.moduleBaseUrl,
			modelFields = options.modelFields,
			gridId = options.gridId,
			gridWrapper = $(gridId).data("kendoGrid").wrapper,
			updateInfoWindowId = options.updateInfoWindowId,
			viewModel = options.viewModel; // not required		
		return {
			batch: true,
			serverPaging: true,
			pageSize: DEFAULT_PAGESIZE_VALUE,
			page: DEFAULT_PAGE_VALUE,
			serverSorting: true,
			serverFiltering: true,
			transport: {
				create: getDefaultRemoteConfig(moduleBaseUrl, "batchSaveOrMerge"),
				read: getDefaultRemoteConfig(moduleBaseUrl, "queryConditional"),
				update: getDefaultRemoteConfig(moduleBaseUrl, "batchSaveOrMerge"),
				destroy: getDefaultRemoteConfig(moduleBaseUrl, "deleteByIds"),
				parameterMap: function(data, type){
					console.log("parameterMap type: " + type);
					console.log("parameterMap data: " + JSON.stringify(data));
					if(type === "read"){
						if(data.filter && data.filter.filters){
							removeFilterDateTimezoneOffset(data.filter, modelFields);
						}
						var viewModelConds = viewModel ? viewModel.get("conds"): {},
							conds = $.extend({}, viewModelConds, {currentPage: data.page, countPerPage: data.pageSize, orderType: data.sort, filter: data.filter});
						return JSON.stringify({conds: conds});
					}else if(type == "create" || type == "update"){
						var dataModels = data['models'];
						if(dataModels){// batch create enabled
							return JSON.stringify(dataModels);
						}
					}else if(type == "destroy"){
						var dataModels = data['models'];
						if(dataModels){
							var ids = $.map(dataModels, function(element, idx){
								return element.id;
							});
							return JSON.stringify(ids);
						}
					}
				}
			},
			schema: {
				type: "json",
				data: function(response){
					// response.results from reading
					// response from adding or updating
					var results = response.results ? response.results: response;
					return results;
				},
				total: function(response){
					return response.pageNavigator.totalCount;
				},
				model: {
					id: pk,
					fields: modelFields
				}
			},
			error: function(e){
				
			},
			requestStart: function(e){
				kendo.ui.progress($(gridId).data("kendoGrid").wrapper, true);
			},
			requestEnd: function(e){
				// e.response comes from dataSource.schema.data, that is not really returned response
				var type = e.type;
				if("update" === type){
					$(updateInfoWindowId).data("kendoWindow").content("<h3 style='color:red;'>更新成功</h3>").center().open();
				}
				kendo.ui.progress($(gridId).data("kendoGrid").wrapper, false);
			}
		};
	}
	function initDefaultInfoWindow(options){
		var windowId = options.windowId,
			title = options.title;
		$(windowId).kendoWindow({
			width: "600px",
			title: title,
			visible: false,
			modal: true,
			action: [
				"Close" // other options: "Pin", "Minimize", "Maximize"
			]
		});
	}
	</script>
	<script type="text/javascript">
		var hidden = {hidden: true},
			uneditable = {editable: false},
			fields = [
		       //0fieldName			1column title		2column width	3field type	4column filter operator	5field custom		6column custom
				[pk,				"SalesDetail ID",	150,			"string",	"eq",					null,				hidden],
				["memberId",		"會員 ID",			150,			"string",	"eq",					uneditable,			hidden],
				["salePoint",		"銷售點",				100,			"string",	"eq"],
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
			modelFields = getDefaultModelFields(fields),
			columns = getDefaultColumns(fields),
			dataSource = getDefaultGridDataSource({modelFields: modelFields, moduleBaseUrl: "${moduleBaseUrl}", gridId: gridId, updateInfoWindowId: updateInfoWindowId});
		
		modelFields["rowId"]["editable"] = false;
	</script>
	<script type="text/javascript">
		$(document).ready(function(){
			var mainGrid = $(gridId).kendoGrid({
				columns: columns,
				dataSource: dataSource,
				autoBind: false,
				toolbar: [{name: "create"},{name: "save"},{name: "cancel"},{name: "reset", text: " 清空"}],
				editable: {create: true, update: true, destroy: true},
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
				columnMenu: true
			}).data("kendoGrid");
			
			$(".k-grid-reset").click(function(e){
				var ds = mainGrid.dataSource,
					page = ds.page(),
					pageSize = ds.pageSize(),
					sort = ds.sort(),
					group = ds.group(),
					filter = ds.filter();
				/* 方案一: 最直接將ds還原預設值的方式，但缺點是在呼叫read的時候會觸發兩次request
				ds.filter(DEFAULT_FILTER_VALUE);
				ds.page(DEFAULT_PAGE_VALUE);
				ds.pageSize(DEFAULT_PAGESIZE_VALUE);
				ds.sort(DEFAULT_SORT_VALUE);
				ds.group(DEFAULT_GROUP_VALUE);
				ds.read();
				*/
				/*方案二: 不去動到ds原來的設定，就是原來的物件還在，但直接清空他內部的陣列，或刪除物件屬性；這樣在read的時候，不會觸發兩次request；壞處是譬如page只是數字，沒辦法去改內部屬性*/
				function clearArray(array){
					while(array.length){
						array.pop();
					}
				}
				if(sort && (sort instanceof Array)){
					clearArray(sort);
				}
				if(filter){
					var filters = filter["filters"];
					clearArray(filters);
					delete filter["logic"];
				}
				ds.read();
			}).find("span").addClass("k-font-icon k-i-undo-large");
			
			$(document.body).keydown(function(e){
				// ref. http://demos.telerik.com/kendo-ui/grid/keyboard-navigation
				if(e.altKey && e.keyCode == 87){// Alt + W 就可以跳到grid table；搭配navigatable設定，可用上下左右鍵在grid cell上移動；遇到可編輯cell，可以Enter進去編輯，編輯完畢按下Enter
					mainGrid.table.focus();
					// var options = mainGrid.getOptions();
					// console.log("options:\n" + JSON.stringify(options));
				}
				if(e.altKey && e.keyCode == 67){// Alt + C 直接觸發 Save Changes；
					mainGrid.dataSource.sync();
				}
				if(e.altKey && e.keyCode == 81){// Alt + Q 直接觸發 Cancel changes
					mainGrid.dataSource.cancelChanges();
				}
				if(e.altKey && e.keyCode == 82){// Alt + R 直接觸發 Add new record
					mainGrid.dataSource.pushCreate();
				}				
				if(e.ctrlKey && e.altKey && e.keyCode == 65){// Ctrl + Alt + A 執行批次複製, ref. http://stackoverflow.com/questions/24273432/kendo-ui-grid-select-single-cell-get-back-dataitem-and-prevent-specific-cells
					var grid = mainGrid,
					    selection = grid.select(); // 回傳jQuery物件，裡面可能是被選取的cells或rows
					if(!selection){
						return;
					};
				    var startColIdx = selection.index(), // 如果多選column，只會顯示最左邊的欄位index；如果單選column，就是該欄位的index；0 based, 隱藏欄位會被計算
				    	lastColIdx = selection.last().index(), // td的index, ref. http://stackoverflow.com/questions/788225/table-row-and-column-number-in-jquery
				    	columnCount = (lastColIdx - startColIdx + 1), // 橫跨的column數量
				    	selectedCount = selection.size(), // 有幾個cell被選擇
				    	rowCount = (selectedCount / columnCount), // 包含的row數量
				    	columnOpts = grid.options.columns,
				    	colFieldName = grid.options.columns[startColIdx].field, // get column field name
						firstRow = selection.closest("tr"), // 如果多選的時候，只會拿到第一個row
						firstDataItem = grid.dataItem(firstRow), // 如果多選的時候，只會拿到第一個dataItem
						fields = [];
					
				    for(var i = startColIdx; i < (lastColIdx+1); i++){
				    	var field = columnOpts[i].field;
				    	fields.push(field);
				    }
					// 如果多選的時候，要取得所有row的dataItem，要跑迴圈
					// 如果透過jQuery的each函式更新dataItem，會使selection的elements產生變化，以致於接下來的更新動作全部失敗。解決方式是:先取得所有dataItem，然後一次修改他們。
					var dataItems = selection.map(function(idx, cell){
						//alert($(cell).eq(0).text()); // 可直接取得cell值
						if(idx > (columnCount-1)){
							var row = $(cell).closest("tr"),
								dataItem = grid.dataItem(row);
							return dataItem;
						}
					});
					for(var i = 0; i < dataItems.length; i++){
						var dataItem = dataItems[i];
						for(var j = 0; j < fields.length; j++){
							var field = fields[j];
							dataItem.set(field, firstDataItem.get(field));
						}
					};
				}
			});
			
			var state = JSON.parse($.cookie(cookieKey));
			if(state){
				if(state.filter){
					parseFilterDates(state.filter, mainGrid.dataSource.options.schema.model.fields);
				}
				mainGrid.dataSource.query(state);
			}else{
				mainGrid.dataSource.read();
			}
			
			initDefaultInfoWindow({windowId: updateInfoWindowId, title: "更新訊息"});
		});
	</script>
	
	<script type="text/javascript">
		$(window).unload(function(){
			var dataSource = $(gridId).data("kendoGrid").dataSource,
				state = kendo.stringify({
					page: dataSource.page(),
					pageSize: dataSource.pageSize(),
					sort: dataSource.sort(),
					group: dataSource.group(),
					filter: dataSource.filter()
				});
			// TODO stored customized conds
			$.cookie(cookieKey, state);
		});
	</script>
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
</body>
</html>