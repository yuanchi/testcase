<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="sf"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>

<c:set value="${pageContext.request.contextPath}" var="rootPath"/>
<c:set value="${rootPath}/kendoui/professional.2016.1.226.trial" var="kendouiRoot"/>
<c:set value="${kendouiRoot}/styles" var="kendouiStyle"/>
<c:set value="${kendouiRoot}/js" var="kendouiJs"/>
<c:set value="${moduleName}KendoData" var="kendoDataKey"/>
<c:set value="${rootPath}/${moduleName}" var="moduleBaseUrl"/>    
<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8"/>
	<meta content="width=device-width, initial-scale=1.0" name="viewport">
	<title>Insert title here</title>

	<link rel="stylesheet" href="${kendouiStyle}/kendo.common.min.css">
	<link rel="stylesheet" href="${kendouiStyle}/kendo.default.min.css">
</head>
<body>
	<div class="container" id="mainGrid"></div>


	<script type="text/javascript" src="${kendouiJs}/jquery.min.js"></script>
	<script type="text/javascript" src="${kendouiJs}/kendo.web.min.js"></script>
	<script type="text/javascript">
		(function($, kendo, opts){"use strict"
			var gridSelector = "#mainGrid",
				ajaxConfig = {
					type: "POST",
					dataType: "json",
					contentType: "application/json;charset=utf-8",
					cache: false
				},
				moduleName = opts.moduleName,
				moduleBaseUrl = opts.moduleBaseUrl,
				pk = opts.pk,
				stockOpts = opts.stockOpts,
				stockModelFields = {},
				stockColumns = [],
				stockFieldNames = [];
			for(var p in stockOpts){
				if(stockOpts.hasOwnProperty(p)){
					stockModelFields[p] = {type: "number"};// 為了讓庫存在編輯及篩選的時候，可以使用數值欄位，這裡要針對個別項目設定；這種設定的負面影響是，修改送出請求的時候，會多了這些定義的欄位名稱，造成後端無法處理
					stockColumns.push({
						field: p, 
						title: stockOpts[p], 
						width: "70px"
					});
					stockFieldNames.push(p);
				}
			}
			var fields = {
				productCategory: {},
				modelId: {type: "string"},
				suggestedRetailPrice: {type: "number"},
				name: {type: "string"},
				nameEng: {type: "string"},
				seriesName: {type: "string"},
				barcode: {type: "string"},
				productInventories: {},
			}
			$.extend(fields, stockModelFields);
			var cutIdx = 3,
				columns = [
				{field: "productCategory.code", title: "產品類別", width: "100px"},
				{field: "modelId", title: "型號", width: "100px;"},
				{field: "suggestedRetailPrice", title: "零售價", width: "100px"},
				{field: "name", title: "產品名稱", width: "100px"},
				{field: "seriesName", title: "系列名", width: "100px"},
				{field: "barcode", title: "條碼", width: "100px"}
			];
			if(stockColumns.length>0){
				columns = columns.slice(0, cutIdx).concat(stockColumns).concat(columns.slice(cutIdx));
			}
			$(gridSelector).kendoGrid({
				dataSource: {
					batch: true, // one http request with multi operation
					serverPaging: true, // need to configure schema.total
					pageSize: 10, // items per page
					page: 1, // specify the current page
					serverFiltering: true, // use the parameterMap to send the filter option in a different formats
					serverSorting: true,
					transport: {
						read: $.extend({url: moduleBaseUrl+"/queryConditional"}, ajaxConfig),
						create: $.extend({url: moduleBaseUrl+"/batchSaveOrMerge"}, ajaxConfig),
						update: $.extend({url: moduleBaseUrl+"/batchSaveOrMerge"}, ajaxConfig),
						read: $.extend({url: moduleBaseUrl+"/queryConditional"}, ajaxConfig),
						parameterMap: function(data, type){
							console.log("parameterMap data:"+JSON.stringify(data));
							if(data.models){
								var dataModels = data.models;
								for(var i=0; i<dataModels.length; i++){
									var dataModel = dataModels[i];
									for(var prop in dataModel){
										if(stockFieldNames.indexOf(prop)>-1){
											delete dataModel[prop];
										}
									}
								}
							}
							if(type === "read"){
								if(data.filter && data.filter.filters){
									// 最主要是轉換時間格式
								}
								var conds = {moduleName: moduleName, kendoData: data};
								return JSON.stringify({conds: conds});
							}else if(type === "create" || type === "update"){
								var dataModels = data.models;
								if(dataModels){
									return JSON.stringify(dataModels);
								}
							}else if(type === "destroy"){
								var dataModels = data.models;
								if(dataModels){
									var ids = 
										$.map(dataModels, function(element, idx){
											return element.id;
										});
									return JSON.stringify(ids);
								}
							}
						}
					},
					schema: {
						type: "json",// the type of the response
						model: {
							id: pk,
							fields: fields
						},
						data: function(response){
							var results = response.results ? response.results : response;
							return results;
						},
						total: function(response){
							return response.pageNavigator.totalCount;
						}
					}
				},// dataSource end
				toolbar: [
					"create", "save", "cancel"     
				],
				columns: columns,
				autoBind: true,
				editable: {
					create: true,
					update: true,
					destroy: true,
					mode: "incell"
				},
				scrollable: true,
				pageable: {
					refresh: true,
					pageSizes: ["10","15","20","all"],
					buttonCount: 12
				},
				sortable: {
					mode: "single",
					allowUnsort: false
				},
				resizable: true,
				navigatable: true,
				filterable: {
					mode: "menu, row"
				},
				columnMenu: true,
				selectable: "multiple,cell"
			});
		})(jQuery, kendo, {
			moduleName: "${moduleName}",
			moduleBaseUrl: "${moduleBaseUrl}",
			pk: "id",
			stockOpts: ${stockOpts}
		})
	</script>
</body>
</html>