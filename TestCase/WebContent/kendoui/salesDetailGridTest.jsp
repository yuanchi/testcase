<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="sf"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>

<c:set value="${pageContext.request.contextPath}" var="rootPath"/>
<c:set value="salesdetail" var="moduleName"/>
<c:set value="${moduleName}KendoData" var="kendoDataKey"/>
<c:set value="${rootPath}/${moduleName}" var="moduleBaseUrl"/>
<c:set value="${rootPath}/kendoui/professional.2016.1.226.trial" var="kendouiRoot"/>
<c:set value="${kendouiRoot}/styles" var="kendouiStyle"/>
<c:set value="${kendouiRoot}/js" var="kendouiJs"/> 
   
<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8"/>
	<meta content="width=device-width, initial-scale=1.0" name="viewport">
	<title>Kendo UI SalesDetail Grid Test Page</title>
	
	<link rel="stylesheet" href="${kendouiStyle}/kendo.common.min.css">
	<link rel="stylesheet" href="${kendouiStyle}/kendo.default.min.css">
	
	<script type="text/javascript" src="${kendouiJs}/jquery.min.js"></script>
	<script type="text/javascript" src="${kendouiJs}/jquery.cookie.js"></script>
	<script type="text/javascript" src="${kendouiJs}/kendo.web.min.js"></script>
	<script type="text/javascript" src="${kendouiJs}/messages/kendo.messages.zh-TW.min.js"></script>

	<style type="text/css">
	/*
	.k-tooltip-validation {
		margin-top: 0 !important;
		display: block;
		position: static;
		padding: 0;
	} 

	.k-callout {
		display: none;
	}
	*/
	/*
		keep kendo ui grid same height
	*/
	.k-grid tbody tr td {
    	overflow: hidden;
    	text-overflow: ellipsis;
    	white-space: nowrap;
	}
	/*
		keep kendo autocomplete width same as text content
		ref. http://jsfiddle.net/GxSpN/41/
	*/
	.k-autocomplete {
    	width: auto;
	}
	.k-list-container {
    	width: auto !important;
	}
	.k-list-container .k-list .k-item {
    	white-space: nowrap;
	}
	
	</style>
</head>
<body>
	<span id="updateInfoWindow" style="display:none;"></span>
	<div id="mainGrid"></div>
	<div id="updateNoti"></div>
	<script type="text/javascript">
		// on closeCell function, add custom event
		kendo.ui.Grid.fn.closeCell = (function(closeCell){ // ref. http://jsfiddle.net/lhoeppner/nx96g/10/
			return function(cancel){
				var that = this,
					container = that._editContainer;				
				closeCell.call(this, cancel);
				if(!container){
					return;
				}
				var tdIdx = container.index(),
					model = that.dataSource.getByUid(container.closest("tr").attr(window.kendo.attr("uid"))),
					event = {
						container: container,
						model: model,
						preventDefault: function(){
							this.isDefaultPrevented = true;
						}
					};
				var afterCloseHandler = this.options.afterClose;
				if(model && typeof afterCloseHandler === "function"){
					afterCloseHandler.call(this, event);
					if(event.isDefaultPrevented){
						return;
					}
				}
			};
		})(kendo.ui.Grid.fn.closeCell);
	</script>
	<script type="text/javascript">
		var moduleName = "${moduleName}",
			moduleBaseUrl = "${moduleBaseUrl}",
			gridId = "#mainGrid",
			notiId = "#updateNoti",
			updateInfoWindowId = "#updateInfoWindow",
			DEFAULT_PAGE_VALUE = 1,
			DEFAULT_PAGESIZE_VALUE = 15,
			DEFAULT_FILTER_VALUE = null,
			DEFAULT_SORT_VALUE = null,
			DEFAULT_GROUP_VALUE = null,
			DEFAULT_QUERY_OPTIONS = {
				filter: DEFAULT_FILTER_VALUE,
				page: DEFAULT_PAGE_VALUE,
				pageSize: DEFAULT_PAGESIZE_VALUE,
				sort: DEFAULT_SORT_VALUE,
				group: DEFAULT_GROUP_VALUE
			},
			DEFAULT_EDIT_MODE = "incell",
			pk = "id";		
	</script>
	<script type="text/javascript">
	function once(func){
		function empty(){}
		return function(){
			var f = func;
			func = empty;
			f.apply(this, arguments);
		};
	}
	function minusTimezoneOffset(d){
		var hours = d.getHours(),
			mins = d.getMinutes(),
			secs = d.getSeconds(),
			milliSecs = d.getMilliseconds();
		d.setHours(hours, mins-d.getTimezoneOffset(), secs, milliSecs); // GMT+0800 timezoneoffset is -480
		return d;
	}
	// 加上timezoneoffset
	function plusTimezoneOffset(d){
		var hours = d.getHours(),
			mins = d.getMinutes(),
			secs = d.getSeconds(),
			milliSecs = d.getMilliseconds();
		d.setHours(hours, mins+d.getTimezoneOffset(), secs, milliSecs);
		return d;				
	}
	function minusFilterDateTimezoneOffset(filter, modelFields){
		if(!filter){
			return;
		}
		if(filter.filters){
			for(var i = 0; i < filter.filters.length; i++){
				var f = filter.filters[i];
				minusFilterDateTimezoneOffset(f, modelFields);
			}
		}
		if(filter.field && "date" == modelFields[filter.field].type && filter.value && (filter.value instanceof Date)){
			filter.value = minusTimezoneOffset(filter.value);
		}
	}
	function parseFilterDates(filter, fields){
		if(filter.filters){
			for(var i = 0; i < filter.filters.length; i++){
				parseFilterDates(filter.filters[i], fields);
			}
		}else{
			if(fields[filter.field].type == "date"){
				// console.log('store cookie date: ' + filter.value);
				filter.value = plusTimezoneOffset(kendo.parseDate(filter.value));
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
	function addSortsAsc(fields){
		var sortArray = [];
		for(var i = 0; i < fields.length; i++){
			var field = fields[i];
			sortArray.push({field: field, dir: "asc"});
		}
		return sortArray;
	}
	function getModelDataItem(ele){
		row = ele.closest("tr"),
		grid = row.closest("[data-role=grid]").data("kendoGrid"),
		dataItem = grid.dataItem(row);
		return dataItem;
	}
	function getAutoCompleteDefaultTemplate(fields){
		var items = 
			$.map(fields, function(element, idx){
				return "#:" + element + "#";
			});
		var result = items.join("|");
		result = ("<span>" + result + "</span>");
		return result;
	}
	function getAutoCompleteEditor(settings){
		var textField = settings.textField,
			readUrl = settings.readUrl
			filter = settings.filter ? settings.filter : "contains",
			autocompleteFieldsToFilter = settings.autocompleteFieldsToFilter,
			template = settings.template ? settings.template : getAutoCompleteDefaultTemplate(autocompleteFieldsToFilter),
			errorMsgFieldName = settings.errorMsgFieldName,
			selectExtraAction = settings.selectExtraAction;
		return function(container, options){
			var model = options.model,
				field = options.field;
			$('<input data-text-field="'+ textField +'" data-bind="value:'+ field +'"/>')
				.appendTo(container)
				.kendoAutoComplete({
					minLength: 1,
					filter: filter,
					template: template,
					// autoBind: false, // 如果加上這行，會出現e._preselect is not a function錯誤訊息，根據官方說法，這是因為autocomplete沒有支援deferred binding
					valuePrimitive: false, // 如果選定的值，要對應物件，valuePrimitive應設為false，否則選了值之後，他會顯示[object Object]
					/*
					height: 520,
					virtual: {
						itemHeight: 26,
						valueMapper: function(options){
							console.log("valueMapper options.value: " + JSON.stringify(options.value));
							return options.value;
						}
					},*/
					dataSource: {
						serverPaging: true,
						serverFiltering: true,
						pageSize: DEFAULT_PAGESIZE_VALUE,
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
									//console.log("data: " + JSON.stringify(data));
									var r = {
										conds: {
											kendoData: {
												page: 1,
												pageSize: data.pageSize? data.pageSize : DEFAULT_PAGESIZE_VALUE,
												filter: changeFilterToMulti(data.filter, autocompleteFieldsToFilter), // autocomplete元件只有支援單一filter條件，這裡可以將他轉為多個filter條件
												sort: addSortsAsc(autocompleteFieldsToFilter)
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
						//console.log("select item:" + JSON.stringify(item));
						var text = item.text(); // text is template result
						//console.log("item: " + JSON.stringify(item) + ", text: " + text);
						var dataItem = this.dataItem(e.item.index());
						// ref. http://www.telerik.com/forums/autocomplete-update-grid-datasource
						// 這裡要自行綁定model值，因為如果grid該欄位有設定檢核，而且有檢核未過的記錄，第二次以後在autocomplete選到的值都無法正常加到model上
						model.set(field, dataItem);
						if(selectExtraAction && (typeof selectExtraAction === "function")){// 如果有更動其他欄位，會用原來的檢核
							selectExtraAction(model, dataItem);
						}
					},
					filtering: function(e){
						model.set(field, null);
					}
				});
		};
	}
	function getLocalDropDownEditor(settings){
		var dataTextField = settings.dataTextField,
			dataValueField = settings.dataValueField,
			data = settings.data;
		return function(container, options){
			var select = '<input data-text-field="'+dataTextField+'" data-value-field="'+ dataValueField +'" data-bind="value:'+ options.field +'"/>';
			$(select)
				.appendTo(container)
				.kendoDropDownList({
					dataSource: {
						data: data
					}
				});
		};
	}
	function initDefaultNotification(options){
		var centerOnShow = function(e){
				if(e.sender.getNotifications().length == 1){
					var element = e.element.parent(),
						eWidth = element.width(),
						eHeight = element.height(),
						wWidth = $(window).width(),
						wHeight = $(window).height(),
						newTop, newLeft;
					
					newLeft = Math.floor(wWidth / 2 - eWidth / 2);
					newTop = Math.floor(wHeight / 2 - eHeight / 2);
					
					e.element.parent().css({top: newTop, left: newLeft});
				}
			},
			notiId = options.notiId,
			onShow = options.onShow ? options.onShow : centerOnShow;
		$(notiId).kendoNotification({
			stacking: "default",
			width: 300,
			height: 50,
			position: {
				pinned: false,
				bottom: 20,
				right: 20
			}
		});
	}
	function getDefaultModelFields(fields){
		var modelFields = {};
		for(var i = 0; i < fields.length; i++){
			var field = fields[i],
				fieldName = field[0],
				type = field[3],
				customOpt = field[5];
			modelFields[fieldName] = {
				defaultValue: null,
				type: type
			};
			if(fieldName === pk){
				modelFields[fieldName]["editable"] = false;
			}
			if(customOpt){
				$.extend(modelFields[fieldName], customOpt);
			}
		}
		return modelFields;
	}
	function getDefaultColumns(fields){
		var columns = [],
			defaultTemplate = "<span title='#=({field} ? {field} : '')#'>#=({field} ? {field} : '')#</span>",
			defaultFilterTemplate = function(args){// 透過重新定義filter輸入欄位的template，可以阻止onchange事件每次都觸發發送request的預設行為；這樣就剩下onblur才會真的觸發查詢請求
				var parent = '<span tabindex="-1" role="presentation" style="" class="k-widget k-autocomplete k-header k-state-default"></span>';
				args.element
					.css("width", "90%") // 輸入欄位隨欄位寬度變動
					.addClass("k-input") // 讓kendo ui元件認出這是輸入欄位
					.attr("type", "text") // 讓版型更為一致
					.wrap(parent); // 跟原來預設的版型一樣，有圓角，而且與相鄰元件(按鈕)對齊
			};
		columns.push({
			command: ["destroy"], // 刪除欄位最後決定放在最前方，因為如果cloumn太多，更新完後會跳回到最前面欄位位置；
			width: "100px"
		});
		if("incell" !== DEFAULT_EDIT_MODE){
			var idx = columns.length - 1;
			columns[idx].command.push("edit");
			columns[idx].width = "170px";
		}				
		for(var i = 0; i < fields.length; i++){
			var field = fields[i],
				fieldName = field[0],
				width = field[2],
				editor = field[7];
			var column = {
					field: fieldName,
					width: width+"px",
					title: field[1],
					filterable: {
						cell: {
							operator: field[4],
							template: defaultFilterTemplate
						}
					},
					template: defaultTemplate.replace(/{field}/g, fieldName)
				};
			if(editor){
				column["editor"] = editor;
			}
			if("date" === field[3]){
				column["format"] = "{0:yyyy-MM-dd}";
				column["parseFormats"] = "{0:yyyy-MM-dd}";
				column["filterable"]["ui"] = "datetimepicker";
				column["template"] = "<span title='#= kendo.toString(" + fieldName +", \"u\")#'>#= kendo.toString("+ fieldName +", \"u\")#</span>";
			}
			if(field[6]){
				$.extend(column, field[6]);
			}
			columns.push(column);
		}		
		return columns;
	}
	function getDefaultRemoteConfig(action){
		return {
			url: (moduleBaseUrl + "/" + action + ".json"),
			type: "POST",
			dataType: "json",
			contentType: "application/json;charset=utf-8",
			cache: false
		};
	}
	function getDefaultGridDataSource(options){
		var modelFields = options.modelFields,
			viewModel = options.viewModel; // not required		
		return {
			batch: true,
			serverPaging: true,
			pageSize: DEFAULT_PAGESIZE_VALUE,
			page: DEFAULT_PAGE_VALUE,
			serverSorting: true,
			serverFiltering: true,
			transport: {
				create: getDefaultRemoteConfig("batchSaveOrMerge"),
				read: getDefaultRemoteConfig("queryConditional"),
				update: getDefaultRemoteConfig("batchSaveOrMerge"),
				destroy: getDefaultRemoteConfig("deleteByIds"),
				parameterMap: function(data, type){
					console.log("parameterMap type: " + type);
					//console.log("parameterMap data: " + JSON.stringify(data));
					if(type === "read"){
						if(data.filter && data.filter.filters){
							minusFilterDateTimezoneOffset(data.filter, modelFields);
						}
						var viewModelConds = viewModel ? viewModel.get("conds"): {},
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
					id: pk,
					fields: modelFields
				}
			},
			error: function(e){
				var status = (e && e.xhr && e.xhr.status) ? e.xhr.status : null;
				if(status != 200){
					alert("server錯誤訊息: " + JSON.stringify(e));	
				}
			},
			requestStart: function(e){
				kendo.ui.progress($(gridId).data("kendoGrid").wrapper, true);
			},
			requestEnd: function(e){
				// e.response comes from dataSource.schema.data, that is not really returned response
				var grid = $(gridId).data("kendoGrid"),
					messages = grid.options.messages.commands,
					type = e.type,
					action = messages[type];
				/*
				if("update" === type){
					$(updateInfoWindowId).data("kendoWindow").content("<h3 style='color:red;'>更新成功</h3>").center().open();
				}*/
				kendo.ui.progress(grid.wrapper, false);
				if(action){
					$(notiId).data("kendoNotification").show(action + "成功");	
				}
			}
		};
	}
	function initDefaultInfoWindow(options){
		var windowId = options.windowId,
			title = options.title;
		$(windowId).kendoWindow({
			width: "600px",
			title: title,
			visible: false,
			modal: true,
			action: [
				"Close" // other options: "Pin", "Minimize", "Maximize"
			],
			animation:{
				close:{
					effects: "fade:out",
					duration: 2000
				}
			},
			open: function(e){
				var win = this;
				setTimeout(function(){
					win.close();
				},
				3000);
			}
		});
	}
	</script>
	<script type="text/javascript">
		var hidden = {hidden: true},
			uneditable = {editable: false},
			memberFieldName = "member",
			memberField = {
				type: null,
				validation: {
					isEffectiveMember: function(input){
						if(input.length == 0// 更動某個欄位，如果會一併動到其他欄位，會造成意外的檢核，所以要先判斷是否有抓到正確待檢核的欄位。
						|| !input.is("[data-bind='value:member']")
						){ 
							return true;
						}
						var td = input.closest("td"),
							fieldName = memberFieldName,
							row = input.closest("tr"),
							grid = row.closest("[data-role='grid']").data("kendoGrid"),
							dataItem = grid.dataItem(row),
							val = dataItem.get(fieldName);
						console.log("validate...dataItem val: " + val + ", input val: " + input.val());
						td.find("div").remove();
						
						if(!input.val()){
							return true;
						}
						
						if(val && !val.id){
							input.attr("data-isEffectiveMember-msg", "請選擇有效會員資料");
							var timer = setInterval(function(){// 在設定input上的錯誤訊息後，kendo ui不見得會即時產生錯誤訊息元素，這導致後續移動元素的動作有時成功、有時失敗，所以設定setInterval
								var div = td.find("div");
								if(div.length > 0){
									clearInterval(timer);
									div.detach().appendTo(td).show(); // 解決錯誤訊息被遮蔽的問題 ref. http://stackoverflow.com/questions/1279957/how-to-move-an-element-into-another-element
								}
							},10);
							return false;
						}
						
						return true;
					}
				}
			},
			memberColumn = {template: "<span title='#=(member ? member.name : '')#'>#=(member ? member.name : '')#</span>"},
			memberEditor = getAutoCompleteEditor({
				textField: "name",
				valueField: "id",
				readUrl: moduleBaseUrl + "/queryMemberAutocomplete.json", 
				filter: "contains", 
				//template: "<span>#: name # | #: nameEng #</span>",
				autocompleteFieldsToFilter: ["name", "nameEng", "idNo"],
				errorMsgFieldName: memberFieldName,
				selectExtraAction: function(model, dataItem){
					model.set("fbName", dataItem.fbNickname);
				}
			}),
			fields = [
		       //0fieldName			1column title		2column width	3field type	4column filter operator	5field custom		6column custom		7column editor
				[pk,				"SalesDetail ID",	150,			"string",	"eq",					null,				hidden],
				["member",			"會員姓名",			150,			"string",	"contains",				memberField,		memberColumn,		memberEditor],
				["salePoint",		"銷售點",				100,			"string",	"eq",					null,				null],
				["saleStatus",		"狀態",				100,			"string",	"eq"],
				["fbName",			"FB名稱/客人姓名",		150,			"string",	"contains"],
				["activity",		"活動",				150,			"string",	"contains"],
				["modelId",			"型號",				150,			"string",	"startswith"],
				["productName",		"明細",				150,			"string",	"contains"],
				["price",			"定價",				100,			"number",	"gte"],
				["memberPrice",		"會員價(實收價格)",		100,			"number",	"gte"],
				["priority",		"順序",				150,			"string",	"eq",					null,				hidden],
				["orderDate",		"銷售日期",			150,			"date",		"gte"],
				["otherNote",		"其他備註",			150,			"string",	"contains",				null,				hidden],
				["checkBillStatus",	"對帳狀態",			150,			"string",	"contains"],
				["idNo",			"身份證字號",			150,			"string",	"contains"],
				["discountType",	"折扣説明",			150,			"string",	"contains"],
				["arrivalStatus",	"已到貨",				150,			"string",	"eq",					null,				hidden],
				["shippingDate",	"出貨日",				150,			"date",		"gte"],
				["sendMethod",		"郵寄方式",			150,			"string",	"eq"],
				["note",			"備註",				150,			"string",	"contains",				null,				hidden],
				["payDate",			"付款日期",			150,			"date",		"gte"],
				["contactInfo",		"郵寄地址電話",		150,			"string",	"contains",				null,				hidden],
				["registrant",		"登單者",				150,			"string",	"contains",				null,				hidden],
				["rowId",			"Excel序號",			150,			"string",	"contains",				uneditable,			hidden]
			],
			modelFields = getDefaultModelFields(fields),
			columns = getDefaultColumns(fields),
			dataSource = getDefaultGridDataSource({modelFields: modelFields}),
			toolbar = [
			{
				name: "create",
			},
			{
				text: " 重查",
				name: "reset",
				iconClass: "k-font-icon k-i-undo-large"
			}];
		
		modelFields["rowId"]["editable"] = false;
		if("incell" === DEFAULT_EDIT_MODE){// in relation with batch update
			toolbar.push({name: "save"});
			toolbar.push({name: "cancel"});
		}
		/*
		dataSource.change = function(e){
			var field = e.field,
				items = e.items;
			//console.log("field: " + field + ", items: " + JSON.stringify(items));
			console.log("view: " + JSON.stringify(this.view()));
		};*/
	</script>
	<script type="text/javascript">
		$(document).ready(function(){
			var mainGrid = $(gridId).kendoGrid({
				columns: columns,
				dataSource: dataSource,
				autoBind: false,
				toolbar: toolbar,
				editable: {
					create: true, 
					update: true, 
					destroy: true,
					mode: DEFAULT_EDIT_MODE
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
				filterable: {
					mode: "menu, row",
					extra: true
				},
				selectable: "multiple, cell",
				columnMenu: true,
				dataBinding: function(e){
					//var cell = this.tbody.find("tr[row='row'] td:first");
					//this.current(cell);
					//this.table.focus();
				}
				/*edit事件編輯前觸發一次，編輯完、跳出編輯模式後觸發一次
				edit: function(e){
					var container = e.container, // if edit mode is incell, the container is table cell
						sender = e.sender;
					sender.current().focus();
				}*/
			}).data("kendoGrid");			

			/* 在新增的時候，切換編輯模式為...
			function recoverToDefaultEditMode(){
				mainGrid.options.editable.mode = DEFAULT_EDIT_MODE;
			}
			$(".k-grid-popupAdd", mainGrid.element).on("click", function(e){
				mainGrid.options.editable.mode = "inline";
				mainGrid.addRow();
				var current = mainGrid.current();
				current.closest(".k-grid-update").one("click", recoverToDefaultEditMode);
				current.closest(".k-grid-cancel").one("click", recoverToDefaultEditMode);				
				// mainGrid.options.editable.mode = DEFAULT_EDIT_MODE;
			});
			*/
			$(".k-grid-reset").click(function(e){
				var ds = mainGrid.dataSource;
				ds.query(DEFAULT_QUERY_OPTIONS);
			});
			
			
			var tdTotal = 0;
			mainGrid.tbody.on("keydown", "td[data-role='editable'] input", function(e){
				$target = $(e.target);
				if(e.keyCode == 13){
					/* not work!!
					$(gridId).data("kendoGrid").one("afterClose", function(e){
						var $td = e.container,
							tdIdx = $td.index(),
							$tr = $td.closest("tr");
					
						do{
							var nextCell = $tr.find("td:eq("+ (++tdIdx) +")");
						}while(nextCell.css("display") === "none");
				
						if(nextCell.length === 0){
							return;
						}
						setTimeout(function(){
							var grid = $(gridId).data("kendoGrid");
							grid.current(nextCell);
							grid.editCell(nextCell);
						},0);
					});
					*/
					mainGrid.options["afterClose"] = once(function(e){ // 不得已直接從kendo ui widget的options加入事件處理器
						var $td = e.container,
							tdIdx = $td.index(),
							$tr = $td.closest("tr");
						if(!tdTotal){
							tdTotal = $tr.find("td").length;
						}
						do{
							if(tdIdx+1 >= tdTotal){// 如果已經是該行最後一欄，從下一行前面開始
								$tr = $tr.next("tr");
								tdIdx = 0;// TODO 第一列可能是或不是可編輯儲存格
							}
							var nextCell = $tr.find("td:eq("+ (++tdIdx) +")");
						}while(nextCell.css("display") === "none"); // 如果是隱藏欄位就跳下一筆
					
						if(nextCell.length === 0){
							return;
						}
						setTimeout(function(){
							var grid = $(gridId).data("kendoGrid");
							grid.current(nextCell);
							grid.editCell(nextCell);
						},0);
					});
				}
			});
			
			$(document.body).keydown(function(e){
				var altKey = e.altKey,
					keyCode = e.keyCode;
				// ref. http://demos.telerik.com/kendo-ui/grid/keyboard-navigation
				if(altKey && keyCode == 87){// Alt + W 就可以跳到grid table；搭配navigatable設定，可用上下左右鍵在grid cell上移動；遇到可編輯cell，可以Enter進去編輯，編輯完畢按下Enter
					mainGrid.table.focus();
				}
				if(altKey && keyCode == 82){// Alt + R 直接觸發 Add new record
					mainGrid.addRow();
				}
				if(altKey && keyCode == 67){// Alt + C 直接觸發 Save Changes；
					mainGrid.dataSource.sync();
				}
				if(altKey && keyCode == 81){// Alt + Q 直接觸發 Cancel changes
					mainGrid.dataSource.cancelChanges();
				}
				if(altKey && keyCode == 68){// Alt + D 直接觸發 Delete；
					mainGrid.current().closest(".k-grid-delete").click();
				}
				/*
				if(e.altKey && e.keyCode == 69){// Alt + E 直接觸發 Edit；
					mainGrid.current().closest(".k-grid-edit");
				}
				if(e.altKey && e.keyCode == 85){// Alt + U 直接觸發 Update；
					mainGrid.current().closest(".k-grid-update").click();
				}	
				if(e.altKey && e.keyCode == 67){// Alt + C 直接觸發 Cancel；
					mainGrid.current().closest(".k-grid-cancel").click();
				}
				*/				
				if(e.ctrlKey && altKey && keyCode == 65){// Ctrl + Alt + A 執行批次複製, ref. http://stackoverflow.com/questions/24273432/kendo-ui-grid-select-single-cell-get-back-dataitem-and-prevent-specific-cells
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
			var lastKendoData = ${sessionScope[kendoDataKey] == null ? "null" : sessionScope[kendoDataKey]}; // sync with session
			if(lastKendoData){
				mainGrid.dataSource.query(lastKendoData);
			}else{
				mainGrid.dataSource.read();
			}
			// initDefaultInfoWindow({windowId: updateInfoWindowId, title: "更新訊息"});
			initDefaultNotification({notiId: notiId});
			
		});
	</script>
</body>
</html>