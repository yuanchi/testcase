<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="sf"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>

<c:set value="${pageContext.request.contextPath}" var="rootPath"/>
<c:set value="user" var="moduleName"/>
<c:set value="${moduleName}KendoData" var="kendoDataKey"/>
<c:set value="${rootPath}/${moduleName}" var="moduleBaseUrl"/>
<c:set value="${rootPath}/kendoui/professional.2016.1.226.trial" var="kendouiRoot"/>
<c:set value="${kendouiRoot}/styles" var="kendouiStyle"/>
<c:set value="${kendouiRoot}/js" var="kendouiJs"/> 
<!DOCTYPE html>
<html>
<head>
	<meta content="width=device-width, initial-scale=1.0" name="viewport">
	<title>Insert title here</title>
	
	<link rel="stylesheet" href="${kendouiStyle}/kendo.common.min.css">
	<link rel="stylesheet" href="${kendouiStyle}/kendo.default.min.css">
</head>
<body>
	<div id="mainGrid">
	
	</div>


	<script type="text/javascript" src="${kendouiJs}/jquery.min.js"></script>
	<script type="text/javascript" src="${kendouiJs}/kendo.web.min.js"></script>
	<script type="text/javascript">
		(function($, kendo, moduleBaseUrl, moduleName){"use strict"
			var ajaxConfig = 
				{
					type: "POST",
					dataType: "json",
					contentType: "application/json;charset=utf-8",
					cache: false
				};
			$("#mainGrid").kendoGrid({
				dataSource: {
					batch: true,
					serverPaging: true,
					pageSize: 5,
					page: 1,
					serverSorting: true,
					serverFiltering: true,
					transport: {
						read: $.extend({url: moduleBaseUrl+"/queryConditional"}, ajaxConfig),
						create: $.extend({url: moduleBaseUrl+"/batchSaveOrMerge"}, ajaxConfig),
						update: $.extend({url: moduleBaseUrl+"/batchSaveOrMerge"}, ajaxConfig),
						destroy: $.extend({url: moduleBaseUrl+"/deleteByIds"}, ajaxConfig),
						parameterMap: function(data, type){
							console.log("parameterMap type: " + type);
							//console.log("parameterMap data: " + JSON.stringify(data));
							if(type === "read"){
								if(data.filter && data.filter.filters){
									//與日期時間無關，所以不需時區轉換
									//minusFilterDateTimezoneOffset(data.filter, modelFields);
								}
								var viewModelConds = {},
									conds = $.extend({moduleName: moduleName}, viewModelConds, {kendoData: data});
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
						model: {
							id: "id",
							fields: {
								userId: {type: "string"},
								password: {type: "string"},
								name: {type: "string"},
								readonly: {type: "boolean"},
								defaultGroup: {},
								groups: {},
								roles: {},
								info: {}
							}
						},
						data: function(response){
							var results = response.results ? response.results: response;
							return results;
						},
						total: function(response){
							return response.pageNavigator.totalCount;
						}
					}
				}, // dataSource end
				toolbar: [
				   "create", "save", "cancel"       
				],
				columns: [
					{field: "userId", title: "使用者代號", width: "100px"},
					{field: "password", title: "密碼", width: "100px"},
					{field: "name", title: "姓名", width: "100px"},
				    {
						field: "defaultGroup",// ManyToOne
						title: "預設群組", 
						width: "100px",
						template: "#= defaultGroup ? defaultGroup.info.name : ''#",
						editor: function(container, options){
							$("<input name='"+options.field+"'>")
								.appendTo(container)
								.kendoDropDownList({
									dataTextField: "info.name",
									dataValueField: "id",
									dataSource: {// 簡化程式，所以只設定local端資料
										data: [
										    {id: "20150623-101451645-oEWAj", info: {name: "系統管理"}},
				    						{id: "20150630-173659311-ZkOmb", info: {name: "使用者群組"}}
										]
									}
								});
						}
					},
				    {
				    	field: "info", //ManyToOne
				    	title: "資訊", 
				    	width: "100px",
				    	template: "#= info ? info.name : ''#",
				    	editor: function(container, options){
				    		$("<input name='"+options.field+"'>")
				    			.appendTo(container)
				    			.kendoDropDownList({
				    				dataTextField: "name",
				    				dataValueField: "id",
				    				dataSource: {// 簡化程式，所以只設定local端資料
				    					data: [
				    						{id: "20151027-150346667-KojOv", name: "陳怡蓁"},
				    						{id: "20150630-173659306-IDnoI", name: "使用者群組"}
				    					]
				    				}
				    			});
				    	}
				    },				    
				    {
				    	field: "groups", //OneToMany
				    	title: "群組", 
				    	width: "100px",
				    	template: "#= groups ? groups.map(function(e){return e.name;}).join(', ') : ''#",
				    	editor: function(container, options){
				    		$("<select multiple='multiple' data-bind='value: groups'>")
				    			.appendTo(container)
				    			.kendoMultiSelect({
				    				dataTextField: "name",
				    				dataValueField: "id",
				    				dataSource: [// 簡化程式，所以只設定local端資料
				    					{id: "20150623-101451645-oEWAj", name: "系統管理"},
				    					{id: "20150630-173659311-ZkOmb", name: "使用者群組"}
				    				]
				    			});
				    	}
				    },
				    {
				    	field: "roles", //OneToMany
				    	title: "角色", 
				    	width: "100px",
				    	template: "<span style='font-size:50%;'>#= roles ? roles.map(function(e){return e.name;}).join(', ') : ''#</span>",
				    	editor: function(container, options){
				    		$("<select multiple='multiple' data-bind='value: roles'>")
				    			.appendTo(container)
				    			.kendoMultiSelect({
				    				dataTextField: "name",
				    				dataValueField: "id",
				    				dataSource: [// 簡化程式，所以只設定local端資料
				    					{id: "20150623-101451551-SrJDh", name: "ROLE_Test_1"},
				    					{id: "20150623-101451571-CLCdc", name: "ROLE_Test_2"},
				    					{id: "20150623-101451572-yizju", name: "ROLE_Test_3"},
				    					{id: "20150623-101451573-hgQpf", name: "ROLE_Test_4"},
				    					{id: "20150623-101451574-fYvQG", name: "ROLE_Test_5"},
				    					{id: "20150623-101451575-BeeNG", name: "ROLE_Test_6"},
				    					{id: "20150623-101451576-FercK", name: "ROLE_Test_7"},
				    					{id: "20150623-101451577-sAFjJ", name: "ROLE_Test_8"}
				    				]
				    			});
				    	},
				    	filterable: {// Multi Select Filter ref. http://docs.telerik.com/kendo-ui/controls/data-management/grid/how-to/multiselect-used-for-column-filtering
				    		ui: function(element){
				    			element.removeAttr("data-bind");
				    			
				    			element.kendoMultiSelect({
				    				dataTextField: "name",
			    					dataValueField: "id",
				    				dataSource: [
										{id: "20150623-101451551-SrJDh", name: "ROLE_Test_1"},
										{id: "20150623-101451571-CLCdc", name: "ROLE_Test_2"},
										{id: "20150623-101451572-yizju", name: "ROLE_Test_3"},
										{id: "20150623-101451573-hgQpf", name: "ROLE_Test_4"},
										{id: "20150623-101451574-fYvQG", name: "ROLE_Test_5"},
										{id: "20150623-101451575-BeeNG", name: "ROLE_Test_6"},
										{id: "20150623-101451576-FercK", name: "ROLE_Test_7"},
										{id: "20150623-101451577-sAFjJ", name: "ROLE_Test_8"}    
				    				],
				    				change: function(e){
				    					var filter = {logic: "or", filters: []};
				    					var values = this.value();
				    					console.log(JSON.stringify(values));
				    					$.each(values, function(i, v){
				    						filter.filters.push({field: "roles", operator: "eq", value: v});
				    					});
				    					$("#mainGrid").data("kendoGrid").dataSource.filter(filter);
				    				}
				    			});
				    		}
				    	}
				    },
				    {field: "readonly", title: "是否唯讀", width: "100px"}
				],
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
					pageSizes: ["5","10","15","20","25","30","all"],
					buttonCount: 13
				},
				sortable: {
					mode: "single",
					allowUnsort: false
				},
				resizable: true,
				navigatable: true,
				filterable: true,
				selectable: "multiple,cell",
				columnMenu: true
			});
		})(jQuery, kendo, '${moduleBaseUrl}', '${moduleName}')
	</script>
</body>
</html>