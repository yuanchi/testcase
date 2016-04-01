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
					date = d.getDate();
					//console.log('date: ' + d); // Thu Mar 03 2016 00:00:00 GMT+0800
					//console.log('stringify: ' + JSON.stringify(d)); // 2016-03-02T16:00:00.000Z
					var dateStr = fullYear + '-' + (month < 10 ? ('0'+month) : month) + '-' + (date < 10 ? ('0'+date) : date);
					filter.value = dateStr;	
				}
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
				}
			};
			var columns = [{ // defining header title and binding data to model
				field: "id",
				title: "Member ID",
				filterable: {
					cell: {
						showOperators: false
					}
				}
			},
			{
				field: "name",
				title: "Name",
				filterable: {
					cell: {
						operator: "contains" // default filter operator
					}
				}
			},
			{
				field: "birthday",
				title: "生日",
				format:"{0:yyyy-MM-dd}",
				template: '#= kendo.toString(birthday, "yyyy-MM-dd") #',
				filterable: {
					ui: function(element){
						element.kendoDatePicker();
					},
					cell: {
						operator: "gte"
					}
				}
			},
			{
				field: "idNo",
				title: "身分證字號"
			},
			{
				field: "mobile",
				title: "手機"
			},
			{
				command: "destroy" // display delete button and eable this function
			}];
			var dataSample = {};
			for(var i = 0; i < columns.length; i++){
				var col = columns[i],
					field = col.field;
				if(field){
					dataSample[field] = null;
				}
			}
			var inputGridId = "#inputGrid";
			// http://docs.telerik.com/kendo-ui/controls/data-management/grid/overview
			// initialize grid widget
			var gridId = "#grid";
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
				serverFiltering: true,
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
						id: "id",
						fields: modelFields
					},
					parse: function(response){ //preprocess the response before use
						return response;
					},
					errors: function(response){
						return response.error;
					}
				}
			};
			$(gridId).kendoGrid({
				columns: columns,
				dataSource: dataSource,
				toolbar: ["create", "save", "cancel"], // display related operation button
				editable: {// 可編輯: enable functions: create, update, destroy
					create: true,
					update: true,
					destroy: true // disable the deletion functionality
				}, 
				groupable: true, // 分組
				scrollable: true,// 捲軸
				pageable: {
					refresh: true,
					pageSizes: true,
					buttonCount: 5
				}, // 分頁
				sortable: { // 排序
					mode: "single",
					allowUnsort: false
				},
				navigatable: true, // 可藉由方向鍵上下左右在grid cell上移動
				filterable: {
					mode: "row"
				},
				columnMenu: true// sorting, hiding or showing columns
				//resizable: true // column resizing
			});
			$(document.body).keydown(function(e){
				if(e.altKey && e.keyCode == 87){// Alt + W 就可以跳到grid table；搭配navigatable設定，可用上下左右鍵在grid cell上移動；遇到可編輯cell，可以Enter進去編輯，編輯完畢按下Enter
					$(gridId).data("kendoGrid").table.focus();
				}
				if(e.altKey && e.keyCode == 67){// Alt + C 跳到Save Changes；
					$("a.k-grid-save-changes").focus();
				}
			});
			
			var inputGridColumns = columns.slice(),
				last = inputGridColumns[inputGridColumns.length-1];
			if(last.command){
				delete last.command;
				last.title = "";
			}
			$(inputGridId).kendoGrid({
				columns: inputGridColumns,
				edit: function(e){
					var grid = this,
						fieldName = grid.columns[e.container.index()].field, // current editing field name
						model = e.model,
						oldVal = model[fieldName],
						input = e.container.find(".k-input");
					$("[name='"+fieldName+"']", e.container).one("blur", function(){// 只執行一次就unbind
						var inputVal = input.val(), // get lastest value
							dataItems = $(gridId).data("kendoGrid").dataSource.data();
						for(var i = 0; i < dataItems.length; i++){
							var dataItem = dataItems[i];
							var old = dataItem.get(fieldName);
							dataItem.set(fieldName, inputVal);
						}
					});
				},
				dataSource: {
					data: [dataSample],
					schema:{
						model: {
							id: "id",
							fields: modelFields
						}
					}
				},
				editable: {
					update: true
				}
			});
		});
	</script>
</body>
</html>