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
	<title>Kendo UI AutoComplete Test Page</title>
	
	<!-- Common Kendo UI CSS for web and dataviz widgets -->
	<link rel="stylesheet" href="${kendouiStyle}/kendo.common.min.css">
	<!-- Default Kendo UI theme CSS for web and dataviz widgets -->
	<link rel="stylesheet" href="${kendouiStyle}/kendo.default.min.css">	
	
</head>
<body>
	<div id="example">
		<div class="demo-section k-content">
			<h4>Find a member</h4>
			<input id="members" style="width: 100%;"/>
		</div>
	</div>

	<script type="text/javascript" src="${kendouiJs}/jquery.min.js"></script>
	<script type="text/javascript" src="${kendouiJs}/kendo.web.min.js"></script>
	<script type="text/javascript">
		$(document).ready(function(){
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
			
			var fieldsToFilter = ["nameEng", "id", "fbNickname"];
			// 已知bug，在Firefox上，中文以新注音輸入的時候，無法正常驅動，一定配合方向鍵譬如右鍵才會work；在Chrome沒有這個問題。似乎jQuery一般的AutoComplete插件都有這個問題
			$("#members").kendoAutoComplete({
				dataTextField: "name",
				filter: "contains",
				template: "<span>#: id # | #: name # | #: nameEng #</span>",
				// autoBind: false, // 不該使用這個選項，否則會出現錯誤訊息: e._preselect is not a function
				dataSource: {
					serverFiltering: true,
					transport: {
						read:{
							url: "/TestCase/member/queryConditional.json",
							type: "POST",
							dataType: "json",
							contentType: "application/json;charset=utf-8",
							cache: false	
						},
						parameterMap: function(data, type){
							if(type === "read"){
								console.log("data: " + JSON.stringify(data));
								var r = {
									conds: {
										currentPage: 1,
										countPerPage: 20,
										filter: changeFilterToMulti(data.filter, fieldsToFilter)
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
				}
			});
		});
	</script>
</body>
</html>