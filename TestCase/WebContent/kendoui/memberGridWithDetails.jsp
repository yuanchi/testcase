<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="sf"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>

<c:set value="${pageContext.request.contextPath}" var="rootPath"/>
<c:set value="member" var="moduleName"/>
<c:set value="${moduleName}KendoData" var="kendoDataKey"/>
<c:set value="${rootPath}/${moduleName}" var="moduleBaseUrl"/>
<c:set value="${rootPath}/kendoui/professional.2016.1.226.trial" var="kendouiRoot"/>
<c:set value="${kendouiRoot}/styles" var="kendouiStyle"/>
<c:set value="${kendouiRoot}/js" var="kendouiJs"/>    
<!DOCTYPE html>
<html>
<head>
	<meta content="width=device-width, initial-scale=1.0" name="viewport">
	<title>Kendo UI Grid Details Test</title>

	<!-- Common Kendo UI CSS for web and dataviz widgets -->
	<link rel="stylesheet" href="${kendouiStyle}/kendo.common.min.css">
	<!-- Default Kendo UI theme CSS for web and dataviz widgets -->
	<link rel="stylesheet" href="${kendouiStyle}/kendo.default.min.css">
	
	<script type="text/javascript" src="${kendouiJs}/jquery.min.js"></script>
	<!-- Kendo UI combined JavaScript -->
	<script type="text/javascript" src="${kendouiJs}/kendo.web.min.js"></script>

</head>
<body>
	<div class="container">
		<div id="mainGrid" style="width:100%;"></div>
	</div>
	<script type="text/javascript">
		(function($, kendo, moduleBaseUrl, rootPath){"use strict"
			// ref. http://demos.telerik.com/kendo-ui/grid/hierarchy
			// ref. http://dojo.telerik.com/Onora/7
			// 這個範例重點在呈現Grid的階層關係，所以只與查詢有關，不牽涉修改資料
			var element = $("#mainGrid").kendoGrid({
				dataSource: {
					transport: {
						read: {
							url: moduleBaseUrl + "/queryConditional.json",
							type: "POST",
							dataType: "json",
							contentType: "application/json;charset=utf-8",
							cache: false,
						},
						parameterMap: function(data, type){
							if(type == "read"){
								var conds = $.extend({moduleName: "member"}, {kendoData: data});
								return JSON.stringify({conds: conds});
							}
						}
					},
					schema: {
						type: "json",
						data: function(response){
							// response.results from reading
							// response from adding or updating
							/* 
							不管任何遠端存取的動作，都需要回傳所操作的資料集，包含刪除(destroy)。
							文件上雖然說刪除不需要回傳資料，實際上如果在刪除時沒有回傳被刪除的資料，
							會讓client端的grid元件以為資料沒有commit，所以如果再次saveChange的時候，
							他會再送一次請求，而且cancel還可以恢復原狀，理論上這些都不應該發生。
							如果又加上batchUpdate的狀態下，混用新增或修改資料的動作，一次更新，
							重複saveChange，會造成重複的新增資料，但這不會在畫面上正常顯示，直到下一次更新頁面才看得出來。
							根據這篇http://www.telerik.com/forums/delete-save-changes-revert
							解決這些問題的根本做法，就是提供和讀取資料一樣的格式。
							目前已由後端提供被刪的資料。
							*/
							var results = response.results ? response.results: response;
							return results;
						},
						total: function(response){
							return response.pageNavigator.totalCount;
						},
						model: {
							id: "id",
							fields: {
								name: {type: "string"},
								fbNickname: {type: "string"},
								email: {type: "string"}
							}
						}
					},
					pageSize: 6,
					serverPaging: true,
					serverSorting: true
				},
				height: 600,
				sortable: true,
				pageable: true,
				detailInit: detailInit,
				dataBound: function(){
					// 資料綁定之後，展開主表的第一個列(如果有明細就會顯示)
					this.expandRow(this.tbody.find("tr.k-master-row").first());
				},
				columns: [
				    {
				    	field: "name",
				    	title: "姓名",
				    	width: "110px"
				    },
				    {
				    	field: "fbNickname",
				    	title: "FB暱稱",
				    	width: "110px"
				    } ,
				    {
				    	field: "email",
				    	title: "電子郵件",
				    	width: "110px"
				    } 
				]
			});
		
			function detailInit(e){
				$("<div/>").appendTo(e.detailCell).kendoGrid({
					dataSource: {
						transport: {
							read: {
								url: rootPath + "/vipdiscountdetail/queryConditional.json",
								type: "POST",
								dataType: "json",
								contentType: "application/json;charset=utf-8",
								cache: false,
							},
							parameterMap: function(data, type){
								if(type == "read"){
									var conds = $.extend({moduleName: "member"}, {kendoData: data});
									return JSON.stringify({conds: conds});
								}
							}
						},
						schema: {
							type: "json",
							data: function(response){
								var results = response.results ? response.results: response;
								return results;
							},
							total: function(response){
								return response.pageNavigator.totalCount;
							},
							model: {
								id: "id",
								fields: {
									memberIdNo: {type: "string"},
									effectiveStart: {type: "date"},
									effectiveEnd: {type: "date"}
								}
							}
						},
						serverPaging: true,
						serverSorting: true,
						serverFiltering: true,
						pageSize: 10,
						filter: {
							field: "memberId",
							operator: "eq",
							value: e.data.id
						}
					},
					scrollable: false,
					sortable: true,
					pageable: true,
					columns: [
						{field: "memberIdNo", title: "身分證字號", width: "110px"},
						{field: "effectiveStart", title: "有效起日", width: "110px", format: "{0: yyyy-MM-dd}"},
						{field: "effectiveEnd", title: "有效迄日", width: "110px", format: "{0: yyyy-MM-dd}"}
					]
				});
			}
		})(jQuery, kendo, "${moduleBaseUrl}", "${rootPath}")
	</script>
</body>
</html>