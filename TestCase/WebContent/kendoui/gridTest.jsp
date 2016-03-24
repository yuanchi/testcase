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
		<div id="grid"></div>
	</div>
	<script type="text/javascript">
		$(function(){
			// http://docs.telerik.com/kendo-ui/controls/data-management/grid/overview
			// initialize grid widget
			$("#grid").kendoGrid({
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
					title: "生日"
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
				dataSource: {
					transport: {// remote communication
						create: {
							url: "/TestCase/member/save.json",
							dataType: "json"
						},
						read: {
							url: "/TestCase/member/queryAll.json",
							type: "POST",
							dataType: "json",
							data: {
								q: 'testData' // send the value to the remote service
							}
						},
						update: {
							url: "/TestCase/member/queryAll.json"
						},
						destroy: {
							url: "/TestCase/member/delete.json",
							dataType: "json"
						},
						parameterMap: function(data, type){// customize sending parameters to remote
							if(type == "read"){
								return {
									$top: data.take,
									$skip: data.skip
								};
							}
						}
					},
					serverPaging: true,
					pageSize: 10,
					page: 1,
					schema: {
						type: "json",
						data: function(response){
							return response.data.results;
						},
						total: function(response){
							return response.data.pageNavigator.totalCount;
						},
						model: {
							id: "id",
							fields: {
								id: {
									editable: false
								},
								name: {
									editable: true,
									validation: {required: true}
								},
								birthday: {
									type: "date",
									editable: true
								},
								idNo:{
									editable: true
								},
								mobile:{
									type: "number",
									editable: true
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
				},
				toolbar: ["create", "save", "cancel"], // display related operation button
				editable: true, // 可編輯: enable functions: create, update, destroy 
				groupable: true, // 分組
				scrollable: true,// 捲軸
				sortable: true, // 排序
				pageable: true // 分頁
			});
			
		});
	</script>
</body>
</html>