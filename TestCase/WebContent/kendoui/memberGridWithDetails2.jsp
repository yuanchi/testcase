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
			// ref. http://jsbin.com/ubiruc/2/edit?html,output
			// 這個範例重點在呈現Grid的階層關係下的編輯模式
			// 此處主表與子表與後端連動更新的操作，是各自獨立
			// 這個模式有缺點，就是主表與子表的更新要個別動作，如果要一鍵驅動，要手動
			var mainGrid = $("#mainGrid").kendoGrid({
				dataSource: {
					batch: true, // 允許批次修改
					transport: {
						read: {
							url: moduleBaseUrl + "/queryConditional.json",
							type: "POST",
							dataType: "json",
							contentType: "application/json;charset=utf-8",
							cache: false,
						},
						create: {
							url: moduleBaseUrl + "/batchSaveOrMerge.json",
							type: "POST",
							dataType: "json",
							contentType: "application/json;charset=utf-8",
							cache: false,
						},
						update: {
							url: moduleBaseUrl + "/batchSaveOrMerge.json",
							type: "POST",
							dataType: "json",
							contentType: "application/json;charset=utf-8",
							cache: false,
						},
						destroy: {
							url: moduleBaseUrl + "/deleteByIds.json",
							type: "POST",
							dataType: "json",
							contentType: "application/json;charset=utf-8",
							cache: false,
						},
						parameterMap: function(data, type){
							if(type == "read"){
								var conds = $.extend({moduleName: "member"}, {kendoData: data});
								return JSON.stringify({conds: conds});
							}else if(type == "create" || type == "update"){
								return JSON.stringify(data['models']);
							}else if(type == "destroy"){
								var ids = $.map(data['models'], function(d, idx){
									return d.id;
								});
								return JSON.stringify(ids);
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
				editable: "incell",
				navigatable: true, // navigatable設為true，可透過鍵盤在cell上移動，附帶作用是當啟用incell編輯模式時，Enter鍵即為確認完成編輯、ESC即為取消修改並退出編輯模式
				saveChanges: function(e){
					var $mainGrid = $("#mainGrid"),
						kendoGrid = $mainGrid.data("kendoGrid"),
						$masterRows = $mainGrid.find('.k-master-row');
					$masterRows.each(function(i, masterRow){
						var $sibling = $(masterRow).next();
						if($sibling.is('.k-detail-row')){
							var dataItems = $sibling.find('.k-grid').data('kendoGrid').dataSource.data();
							console.log('dataItems: ' + JSON.stringify(dataItems));
							var masterData = kendoGrid.dataItem(masterRow)
							masterData.vipDiscountDetails = dataItems;
							console.log('masterData: ' + JSON.stringify(masterData));
						}
					});
					kendoGrid.dataSource.sync();
					e.preventDefault();
					/*
					$("#mainGrid").data("kendoGrid").dataSource.sync()
						.then(function(){
							var details = mainGrid.find('.k-detail-row').find('.k-grid'),
								len=details.length;
							for(var i=0; i<len; i++){
								var $detail = $(details[i]),
									detailGrid = $detail.data('kendoGrid');
								if(detailGrid.dataSource.hasChanges()){
									detailGrid.dataSource.sync();// 直接呼叫dataSouce的sync，就不用在event loop內競爭，但sync回應時間較久，導致頁面會先render一遍；render之後，detail row會被收起來；但太早開啟detail row，則容易查到舊資料
									// detailGrid.saveChanges(); // saveChanges會觸發其他事件，導致不一定在event queue優先執行
								}
							}
						});*/
				},
				//selectable: "multiple, cell",
				sortable: true,
				pageable: true,
				detailInit: detailInit,
				dataBound: function(){
					// 資料綁定之後，展開主表的第一個列(如果有明細就會顯示)
					// this.expandRow(this.tbody.find("tr.k-master-row").first());
				},
				toolbar: ['create', 'save', 'cancel'],
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
				var masterDateItem = e.data;
				// ref. http://docs.telerik.com/kendo-ui/api/javascript/ui/grid#events-detailInit
				$("<div/>").appendTo(e.detailCell).kendoGrid({
					dataSource: {
						batch: true,
						transport: {
							read: {
								url: rootPath + "/vipdiscountdetail/queryConditional.json",
								type: "POST",
								dataType: "json",
								contentType: "application/json;charset=utf-8",
								cache: false,
							},
							create: {
								url: rootPath + "/vipdiscountdetail/batchSaveOrMerge.json",
								type: "POST",
								dataType: "json",
								contentType: "application/json;charset=utf-8",
								cache: false,
							},
							update: {
								url: rootPath + "/vipdiscountdetail/batchSaveOrMerge.json",
								type: "POST",
								dataType: "json",
								contentType: "application/json;charset=utf-8",
								cache: false,
							},
							destroy: {
								url: rootPath + "/vipdiscountdetail/deleteByIds.json",
								type: "POST",
								dataType: "json",
								contentType: "application/json;charset=utf-8",
								cache: false,
							},
							parameterMap: function(data, type){
								if(type == "read"){
									var conds = $.extend({moduleName: "vipdiscountdetail"}, {kendoData: data});
									return JSON.stringify({conds: conds});
								}else if(type == "create"){
									var dataModels = data['models'];
									return JSON.stringify(dataModels);
								}else if(type == "update"){
									return JSON.stringify(data['models']);
								}else if(type == "destroy"){
									var ids = $.map(data['models'], function(d, idx){
										return d.id;
									});
									return JSON.stringify(ids);
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
							value: masterDateItem.id
						}
					},
					scrollable: false,
					sortable: true,
					pageable: true,
					editable: "incell",
					navigatable: true,
					edit: function(event){
						if(event.model.isNew()){// 新增的時候，要把Foreign Key帶入
							event.model.memberId = masterDateItem.id;
						}
					},
					toolbar: ['create', 'save'], // 明細的工具列不用cancel，因為主表的cancel可以連動影響子表
					columns: [
						{field: "memberIdNo", title: "身分證字號", width: "110px"},
						{field: "effectiveStart", title: "有效起日", width: "110px", format: "{0: yyyy-MM-dd}"}, // 使用這種格式設定日期，會在新增的時候，自動帶入當天日期
						{field: "effectiveEnd", title: "有效迄日", width: "110px", format: "{0: yyyy-MM-dd}"}
					]
				});
			}
		})(jQuery, kendo, "${moduleBaseUrl}", "${rootPath}")
	</script>
</body>
</html>