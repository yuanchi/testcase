<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="sf"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>

<c:set value="${pageContext.request.contextPath}" var="rootPath"/>
<c:set value="${rootPath}/kendoui/professional.2016.1.226.trial" var="kendouiRoot"/>
<c:set value="${kendouiRoot}/styles" var="kendouiStyle"/>
<c:set value="${kendouiRoot}/js" var="kendouiJs"/> 
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
		var moduleName = 'memberTest',
			cookieKey = moduleName + "State",
			gridId = "#grid";
	</script>
	<script type="text/javascript">
		$(function(){
			function modifyFilterDateVal(filter, modelFields){
				if(!filter){
					return;
				}
				if(filter.filters){
					for(var i = 0; i < filter.filters.length; i++){
						var f = filter.filters[i];
						modifyFilterDateVal(f, modelFields);
					}
				}
				if(filter.field && 'date' == modelFields[filter.field].type && filter.value && (filter.value instanceof Date)){
					var d = filter.value,
					fullYear = d.getFullYear(),
					month = d.getMonth()+1,
					date = d.getDate(),
					hour = d.getHours(),
					min = d.getMinutes(),
					sec = d.getSeconds(),
					milliSec = d.getMilliseconds();
					//console.log('date: ' + d); // Thu Mar 03 2016 00:00:00 GMT+0800
					//console.log('stringify: ' + JSON.stringify(d)); // 2016-03-02T16:00:00.000Z
					var dateStr = fullYear + '-' + (month < 10 ? ('0'+month) : month) + '-' + (date < 10 ? ('0'+date) : date) + 'T' + (hour < 10 ? ('0'+hour) : hour) + ':' + (min < 10 ? ('0'+min) : min) + ':00.000Z';
					filter.value = dateStr;	
				}
			}
			function parseFilterDates(filter, fields){
				if(filter.filters){
					for(var i = 0; i < filter.filters.length; i++){
						parseFilterDates(filter.filters[i], fields);
					}
				}else{
					if(fields[filter.field].type == "date"){
						filter.value = kendo.parseDate(filter.value);
					}
				}
			}
			var filterableMessages = {
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
			}, filterableStringOperators = {
				eq: "等於",
				neq: "不等於",
				isnull: "是空值",
				isnotnull: "不是空值",
				startswith: "開頭是",
				contains: "包含",
				doesnotcontain: "不包含",
				endswith: "結尾是"
			};
			// MVVM ref. http://demos.telerik.com/kendo-ui/mvvm/remote-binding
			// http://blog.falafel.com/kendo-ui-creating-an-dynamic-input-form-from-an-external-json-viewmodel/
			var viewModel = kendo.observable({
				conds: null
			});
			var pk = "id";
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
					type: "string"
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
					type: "date",
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
					type: "boolean",
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
						operator: "contains", // default filter operator
						template: function(args){}
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
				}
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
				width: "150px",
			},
			{
				field: "mobile",
				title: "手機",
				width: "150px",
			},
			{
				field: "important",
				title: "是否為VIP",
				width: "100px",
			},			
			{
				command: ["destroy"], // display delete button and eable this function
				width: "100px",
			}];
			// http://docs.telerik.com/kendo-ui/controls/data-management/grid/overview
			// initialize grid widget
			var dataSource = {
				batch: true, // 批次更新 ref. http://docs.telerik.com/kendo-ui/api/javascript/data/datasource
				transport: {// remote communication
					create: {
						url: "/TestCase/member/batchSaveOrMerge.json",
						dataType: "json",
						type: "POST",
						contentType: "application/json;charset=utf-8",
					},
					read: {
						url: "/TestCase/member/queryConditional.json",
						type: "POST",
						dataType: "json",
						contentType: "application/json;charset=utf-8",
						cache: false,
						data: {
							q: 'testData' // additional parameters which are sent to the remote service
						}
					},
					update: {
						url: "/TestCase/member/batchSaveOrMerge.json",
						dataType: "json",
						type: "POST",
						contentType: "application/json;charset=utf-8",
					},
					destroy: {
						url: "/TestCase/member/deleteByIds.json",
						dataType: "json",
						type: "POST",
						contentType: "application/json;charset=utf-8",
					},
					parameterMap: function(data, type){// customize sending parameters to remote
						console.log("parameterMap type: " + type);
						console.log("parameterMap data: " + JSON.stringify(data));
						if(type == "read"){
							if(data.filter && data.filter.filters){
								modifyFilterDateVal(data.filter, modelFields);
							}
							var conds = $.extend({}, viewModel.get("conds"), {currentPage: data.page, countPerPage: data.pageSize, orderType: data.sort, filter: data.filter});
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
				serverPaging: true,
				pageSize: 10,
				page: 1,
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
				change: function(e){// triggered after datasource read data
					// console.log('datasource change...');
				},
				requestStart: function(e){// triggered before datasource send remote request
					// console.log('datasource before requestStart...');
				}
			};
			var mainGrid = $(gridId).kendoGrid({
				columns: columns,
				dataSource: dataSource,
				autoBind: false, // grid初始化的時候，是否先去查詢
				dataBinding: function(e){console.log("dataBinding...");}, // triggered before data binding to widget from its datasource
				dataBound: function(e){console.log("dataBound...");}, // triggered when data binding to widget from its datasource
				toolbar: ["create", "save", "cancel"], // display related operation button
				editable: {// 可編輯: enable functions: create, update, destroy
					create: true,
					update: true,
					destroy: true // disable the deletion functionality
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