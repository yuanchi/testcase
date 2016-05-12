<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="sf"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>

<c:set value="${pageContext.request.contextPath}" var="rootPath"/>
<c:set value="${rootPath}/kendoui/professional.2016.1.226.trial" var="kendouiRoot"/>
<c:set value="${kendouiRoot}/styles" var="kendouiStyle"/>
<c:set value="${kendouiRoot}/js" var="kendouiJs"/>
<c:set value="member" var="moduleName"/>
<c:set value="${moduleName}KendoData" var="kendoDataKey"/>
<c:set value="${rootPath}/${moduleName}" var="moduleBaseUrl"/>
<!DOCTYPE html>

<html>
<head>
	<meta charset="utf-8"/>
	<meta content="width=device-width, initial-scale=1.0" name="viewport">
	<title>Kendo UI Grid Test Page</title>
	
	<!-- Common Kendo UI CSS for web and dataviz widgets -->
	<link rel="stylesheet" href="${kendouiStyle}/kendo.common.min.css">
	<!-- Default Kendo UI theme CSS for web and dataviz widgets -->
	<link rel="stylesheet" href="${kendouiStyle}/kendo.default.min.css">
	
	<script type="text/javascript" src="${kendouiJs}/jquery.min.js"></script>
	<!-- angualarjs feature supported 
	ref. http://docs.telerik.com/kendo-ui/AngularJS/introduction
	-->
	<script type="text/javascript" src="${kendouiJs}/jquery.cookie.js"></script>
	<!-- 
	<script type="text/javascript" src="${kendouiJs}/angular.min.js"></script>
	 -->
	<!-- ref. http://docs.telerik.com/kendo-ui/intro/installation/what-you-need -->
	<!-- Kendo UI combined JavaScript -->
	<script type="text/javascript" src="${kendouiJs}/kendo.web.min.js"></script>
	<style type="text/css">
		.k-grid tbody tr td {
    		overflow: hidden;
    		text-overflow: ellipsis;
    		white-space: nowrap;
		}
	</style>
</head>
<body>
	<div class="container">
		<div id="form-container">
			Name1:<input data-bind="value: conds.cond_pName"/>
			<!-- without value attribte, just display content via text, not value -->
			<span data-bind="text: conds.cond_pName"></span>
			<br>
			<!-- change trigger event via data-value-update -->
			Name2:<input data-bind="value: conds.cond_pName" data-value-update="keyup"/><span data-bind="text: conds.cond_pName"></span>
			<br>
			IdNo:<input data-bind="value: conds.cond_pIdNo"/>
			<br>
			Mobile:<input data-bind="value: conds.cond_pMobile"/>
		</div>
		<div id="inputGrid"></div>
		<div id="grid"></div>
	</div>
	<script type="text/javascript">
		var moduleName = "${moduleName}",
			moduleBaseUrl = "${moduleBaseUrl}",
			cookieKey = moduleName + "State",
			gridId = "#grid",
			DEFAULT_PAGE_VALUE = 1,
			DEFAULT_PAGESIZE_VALUE = 10,
			DEFAULT_FILTER_VALUE = [],
			DEFAULT_SORT_VALUE = null,
			DEFAULT_GROUP_VALUE = null,
			KENDO_UI_TYPE_DATE = "date",
			KENDO_UI_TYPE_STRING = "string",
			KENDO_UI_TYPE_BOOLEAN = "boolean",
			pk = "id",
			fieldsToFilter = ["nameEng", "id", "fbNickname"],
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
		$(function(){
			// JavaScript Date預設會帶入時區(標記)，譬如台灣就是GMT+0800
			// 不管JSON.stringify()或new Date().toISOString()輸出的時間則是UTC，即移除時區的影響；如果時區是GMT+0800，他就會減掉八個小時
			// var d = new Date()	Fri Apr 01 2016 09:30:00 GMT+0800
			// JSON.stringify(d):	"2016-04-01T01:30:00.000Z"
			// d.toISOString():		2016-04-01T01:30:00.000Z
			// 日期轉UTC字串的時候，在GMT+0800時區下，會自動減掉8小時，所以我們加上(-(-8))8小時，就可以抵銷影響
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
			function getAutoCompleteCellEditor(settings){
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
													kendoData: {
														page: 1,
														pageSize: 20,
														filter: changeFilterToMulti(data.filter, fieldsToFilter) // autocomplete元件只有支援單一filter條件，這裡可以將他轉為多個filter條件
													} 
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
			// MVVM ref. http://demos.telerik.com/kendo-ui/mvvm/remote-binding
			// http://blog.falafel.com/kendo-ui-creating-an-dynamic-input-form-from-an-external-json-viewmodel/
			var viewModel = kendo.observable({
				conds: null
			});
			var modelFields = {
				id: {
					editable: false,
					defaultValue: null
				},
				name: {
					editable: true,
					validation: {
						required: true,
						nameStartWithValidation: function(input){// input is a jQuery object // ref. http://demos.telerik.com/kendo-ui/grid/editing-custom-validation
							if(input.is("[name='name']") && input.val() != ""){// [name='name'] references to fields definition name
								input.attr("data-nameStartWithValidation-msg", "會員名稱開頭應該包含英文大寫"); // define showing message if validate fail
								return /^[A-Z]/.test(input.val());
							}
							return true; // validate success
						},
						nameEndWithValidation: function(input){
							if(input.is("[name='name']") && input.val() != ""){
								input.attr("data-nameEndWithValidation-msg", "會員名稱結尾應該包含英文小寫");
								return /[a-z]$/.test(input.val());
							}
							return true; // validate success
						}
					},
					defaultValue: null,
					type: KENDO_UI_TYPE_STRING
				},
				nameEng: {
					editable: true,
					defaultValue: null
				},
				fbNickname: {
					editable: true,
					defaultValue: null
				},
				birthday: {
					type: KENDO_UI_TYPE_DATE,
					editable: true,
					defaultValue: null
				},
				idNo:{
					editable: true,
					defaultValue: null
				},
				mobile:{
					editable: true,
					defaultValue: null
				},
				important:{
					type: KENDO_UI_TYPE_BOOLEAN,
					editable: true,
					defaultValue: false
				}
			};
			var columns = [{ // defining header title and binding data to model
				field: pk, // 已知相關限制，kendo ui grid僅允許一個欄位做為primary key
				//hidden: true, // 已知相關bug，如果啟用隱藏欄位，當keyboard瀏覽filter和header上下變換的時候，移動位置會錯置一格；在最前和最後位置的轉換會導致焦點消失
				width: "150px",
				title: "Member ID",
				filterable: {
					cell: {
						operator: "startswith",
						showOperators: false
					}
				}
			},
			{
				field: "name",
				title: "姓名",
				width: "170px",
				filterable: {
					cell: {
						operator: "contains" // default filter operator
					},
					ignoreCase: true
				}
			},
			{
				field: "nameEng",
				title: "英文姓名",
				width: "200px",
				filterable: {
					cell: {
						operator: "contains" // default filter operator
					}
				},
				editor: getAutoCompleteCellEditor({
					textField: "name", 
					readUrl: moduleBaseUrl + "/queryConditional.json", 
					filter: "contains", 
					template: "<span>#: id # | #: name # | #: nameEng #</span>"})
			},
			{
				field: "fbNickname",
				title: "臉書名稱",
				width: "200px",
				filterable: {
					cell: {
						operator: "contains" // default filter operator
					}
				}
			},				
			{
				field: "birthday",
				title: "生日",
				width: "150px",
				format:"{0:yyyy-MM-dd}",
				parseFormats:"{0:yyyy-MM-dd}",
				filterable: {
					ui: "datetimepicker",// ref. http://stackoverflow.com/questions/28232575/kendoui-grid-filter-date-format
					cell: {
						operator: "gte"
					}
				}
			},
			{
				field: "idNo",
				title: "身分證字號",
				width: "150px"
			},
			{
				field: "mobile",
				title: "手機",
				width: "150px"
			},
			{
				field: "important",
				title: "是否為VIP",
				width: "100px"
			},			
			{
				command: ["edit", "destroy"], // display delete button and eable this function; if there's edit in command options, and grid.editable.mode as "inline", it would disable batch operation
				width: "150px"
			}];
			// http://docs.telerik.com/kendo-ui/controls/data-management/grid/overview
			// initialize grid widget
			var dataSource = {
				batch: true, // 批次更新 ref. http://docs.telerik.com/kendo-ui/api/javascript/data/datasource
				transport: {// remote communication
					create: {
						url: moduleBaseUrl + "/batchSaveOrMerge.json",
						dataType: "json",
						type: "POST",
						contentType: "application/json;charset=utf-8",
					},
					read: {
						url: moduleBaseUrl + "/queryConditional.json",
						type: "POST",
						dataType: "json",
						contentType: "application/json;charset=utf-8",
						cache: false,
						data: {
							q: 'testData' // additional parameters which are sent to the remote service
						}
					},
					update: {
						url: moduleBaseUrl + "/batchSaveOrMerge.json",
						dataType: "json",
						type: "POST",
						contentType: "application/json;charset=utf-8",
					},
					destroy: {
						url: moduleBaseUrl + "/deleteByIds.json",
						dataType: "json",
						type: "POST",
						contentType: "application/json;charset=utf-8",
					},
					parameterMap: function(data, type){// customize sending parameters to remote
						console.log("parameterMap type: " + type);
						console.log("parameterMap data: " + JSON.stringify(data));
						if(type == "read"){
							if(data.filter && data.filter.filters){
								removeFilterDateTimezoneOffset(data.filter, modelFields);
							}
							var conds = $.extend({moduleName: moduleName}, viewModel.get("conds"), {kendoData: data});
							var r = {
								conds: conds
							};
							return JSON.stringify(r);
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
				//sort: {field: pk, dir: 'desc'},
				serverPaging: true,
				pageSize: DEFAULT_PAGESIZE_VALUE,
				page: DEFAULT_PAGE_VALUE,
				serverSorting: true,
				serverFiltering: true, // there's no filter events
				//serverGrouping: true, // ref. http://docs.telerik.com/kendo-ui/api/javascript/data/datasource#configuration-schema.groups
				schema: {
					type: "json",
					data: function(response){
						var conds = viewModel.get("conds");
						if(!conds && response.conds){
							viewModel.set("conds", response.conds);
							kendo.bind($("#form-container"), viewModel);
						}
						var results = response.results; // read
						if(!results){
							results = response; // add, update
						}
						return results;
					},
					total: function(response){
						return response.pageNavigator.totalCount;
					},
					model: {
						id: pk,
						fields: modelFields
					},
					parse: function(response){ //preprocess the response before use
						return response;
					},
					errors: function(response){
						return response.error;
					}
				},
				change: function(e){console.log('datasource change...');},// triggered after datasource read data
				requestStart: function(e){console.log('datasource before requestStart...');},// triggered before datasource send remote request
				requestEnd: function(e){console.log('datasource after requestEnd...');},
				error: function(e){console.log('datasource error...');},
				push: function(e){console.log('datasource push...');},
				sync: function(e){console.log('datasource sync...');}
			};
			var mainGrid = $(gridId).kendoGrid({
				columns: columns,
				dataSource: dataSource,
				autoBind: false, // grid初始化的時候，是否先去查詢
				dataBinding: function(e){console.log("dataBinding...");}, // triggered before data binding to widget from its datasource
				dataBound: function(e){console.log("dataBound...");}, // triggered when data binding to widget from its datasource
				toolbar: [
					{
						text: "Add new record",
						name: "popupAdd",
						iconClass: "k-icon k-add"
					}, 
					//{name: "save"},
					//{name: "cancel"},
					{
						name: "reset", 
						text: "Reset",
						iconClass: "k-font-icon k-i-undo-large"// kendo font icons ref. http://docs.telerik.com/kendo-ui/styles-and-layout/icons-web
					}
				], // display related operation button
				editable: {// 可編輯: enable functions: create, update, destroy
					create: true,
					update: true,
					destroy: true, // disable the deletion functionality
					mode: "incell" // edit modes: incell, inline, and popup
				}, 
				// groupable: true, // 分組
				scrollable: true,// 捲軸
				pageable: { // 分頁
					refresh: true,
					pageSizes: ['5', '10', '15', '20', '25', '30', 'all'],
					buttonCount: 12
				},
				sortable: { // 排序
					mode: "single",
					allowUnsort: false
				},
				navigatable: true, // 可藉由方向鍵上下左右在grid cell上移動
				filterable: {
					mode: "menu, row", // mode as row default enabling filtering action when typing, ref. http://docs.telerik.com/kendo-ui/controls/data-management/grid/how-to/grid-filter-as-you-type
					extra: true,
					messages: filterableMessages,
					operators: {
						string: filterableStringOperators
					}
				},
				selectable: "multiple, cell", // 可選擇多個grid cell
				columnMenu: true // sorting, hiding or showing columns or filtering
				//resizable: true // column resizing
				// adaptive rendering ref. http://docs.telerik.com/kendo-ui/controls/data-management/grid/adaptive
			}).data("kendoGrid");
			
			$(".k-grid-popupAdd", mainGrid.element).on("click", function(e){
				mainGrid.options.editable.mode = "popup";
				mainGrid.addRow();
				mainGrid.options.editable.mode = "inline";
			});
			
			$(".k-grid-reset")
			.click(function(e){
				/*
				read搭配其他任一直接設定如page,pageSize,sort,group,filter，會觸發兩次請求，譬如:
				ds.page(1);
				ds.read();
				或者:
				ds.page(1);
				ds.pageSize(10);
				ds.read();
				或者:
				ds.page(1);
				ds.pageSize(10);
				ds.sort(null);
				ds.read();
				都是送兩次請求
				*/
							
				var ds = mainGrid.dataSource,
					page = ds.page(),
					pageSize = ds.pageSize(),
					sort = ds.sort(),
					group = ds.group(),
					filter = ds.filter();
				// console.log("filter" + JSON.stringify(filter) + "page: " + JSON.stringify(page) + ", pageSize: " + JSON.stringify(pageSize) + ", sort: " + JSON.stringify(sort) + ", group: " + JSON.stringify(group));
				function clearArray(array){
					while(array.length){
						array.pop();
					}
				}
				/*方案一: 最直接將ds還原預設值的方式，但缺點是在呼叫read的時候會觸發兩次request
				ds.page(DEFAULT_PAGE_VALUE);
				ds.pageSize(DEFAULT_PAGESIZE_VALUE);
				ds.sort(DEFAULT_SORT_VALUE);
				ds.group(DEFAULT_GROUP_VALUE);
				ds.filter(DEFAULT_FILTER_VALUE);
				
				ds.read();
				*/
				/*方案二: 不去動到ds原來的設定，就是原來的物件還在，但直接清空他內部的陣列，或刪除物件屬性；這樣在read的時候，不會觸發兩次request；壞處是譬如page只是數字，沒辦法去改內部屬性
				if(sort && (sort instanceof Array)){
					clearArray(sort);
				}
				if(filter){
					var filters = filter["filters"];
					clearArray(filters);
					delete filter["logic"];
				}
				ds.read();
				*/
				/*方案三: 這種方式也不會額外觸發一次請求*/
				ds.query({
					filter: DEFAULT_FILTER_VALUE,
					page: DEFAULT_PAGE_VALUE,
					pageSize: DEFAULT_PAGESIZE_VALUE,
					sort: DEFAULT_SORT_VALUE,
					group: DEFAULT_GROUP_VALUE
				});
			});
			
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
			/*
			var state = JSON.parse($.cookie(cookieKey));
			if(state){
				if(state.filter){
					parseFilterDates(state.filter, mainGrid.dataSource.options.schema.model.fields);
				}
				mainGrid.dataSource.query(state);
			}else{
				mainGrid.dataSource.read();
			}
			*/
			var lastKendoData = ${sessionScope[kendoDataKey] == null ? "null" : sessionScope[kendoDataKey]};
			console.log("lastKendoData: " + JSON.stringify(lastKendoData));
			if(lastKendoData){
				mainGrid.dataSource.query(lastKendoData);
			}else{
				mainGrid.dataSource.read();
			}
		});
	</script>
	<script type="text/javascript">
		/*
		$(window).unload(function(){
			var dataSource = $(gridId).data("kendoGrid").dataSource,
				state = kendo.stringify({
					page: dataSource.page(),
					pageSize: dataSource.pageSize(),
					sort: dataSource.sort(),
					group: dataSource.group(),
					filter: dataSource.filter()
				});
			console.log("window.unload sort" + JSON.stringify(dataSource.sort()));
			// TODO stored customized conds
			$.cookie(cookieKey, state);
		});
		*/
	</script>
</body>
</html>