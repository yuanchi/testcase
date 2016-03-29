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
		<div id="grid"></div>
	</div>
	<script type="text/javascript">
		$(function(){
			// MVVM ref. http://demos.telerik.com/kendo-ui/mvvm/remote-binding
			// http://blog.falafel.com/kendo-ui-creating-an-dynamic-input-form-from-an-external-json-viewmodel/
			var viewModel = kendo.observable({
				conds: null
			});
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
						if(type == "read"){
							var s = "";
							for(var prop in data){
								if(prop == "sort"){
									var sortObj = data[prop];
									s = JSON.stringify(sortObj);
								}
							}
							console.log(s);
							var conds = $.extend({}, viewModel.get("conds"), {currentPage: data.page, countPerPage: data.pageSize, orderType: data.sort});
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
				schema: {
					type: "json",
					data: function(response){
						var conds = viewModel.get("conds");
						if(conds == null){
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
						fields: {
							id: {
								editable: false,
								defaultValue: null
							},
							name: {
								editable: true,
								validation: {required: true},
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
						}
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
				columns:[{ // defining header title and binding data to model
					field: "id",
					title: "Member ID"
				},
				{
					field: "name",
					title: "Name"
				},
				{
					field: "birthday",
					title: "生日",
					template: '#= kendo.toString(birthday, "yyyy-MM-dd") #'
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
				}],
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
			
		});
	</script>
</body>
</html>