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
	<title>Kendo UI Base Test Page</title>
	
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
		<label>Datepicker:</label><input id="datepicker"/>
		<br>
		<label>Autocomplete</label><input id="animal"/>
		<label>Autocomplete Binding Event Always Execute Events</label><input id="car"/>
		<br>
		<button>Button1</button>
		<button>Button2</button>
		<br>
		<div id="grid"></div>
	</div>
	<script type="text/javascript">
		$(function(){
			// initialize widgets
			$("#animal").kendoAutoComplete({
				dataSource: ["Ant", "Antilop", "Badger", "Beaver", "Bird"]
			});
			$("#car").kendoAutoComplete({
				dataSource: ["Motor", "Bicycle", "Truck"],
				change: function(e){
					var autoComplete = e.sender; // retrieve widget triggering the event
					alert('always execute change handler!!');
				}
			});
			$("button").kendoButton();
			// return jQuery object that selects dom element which datepicker widget bases on
			var datepicker1 = $("#datepicker").kendoDatePicker(); // jQuery
			
			// retrieve widget
			// return datepicker widget reference with data functioin
			var datepicker2 = $("#datepicker").data("kendoDatePicker");
			// return datepicker widget reference with getXxx function
			var datepicker3 = $("#datepicker").getKendoDatePicker();
			var datepicker4 = $("#datepicker").data("kendoDatePicker").element; // jQuery selecting HTML dom element
			var datepicker5 = datepicker1.element; // HTML dom element
			var dWrapper = $("#datepicker").data("kendoDatePicker").wrapper; // jQuery selecting outermost element
			//alert('datepicker1 return jquery obj:' + (datepicker1 instanceof jQuery) + 'datepicker2 return jquery obj:' + (datepicker2 instanceof jQuery) + 'datepicker3 return jquery obj:' + (datepicker3 instanceof jQuery) + 'datepicker4 return jquery obj:' + (datepicker4 instanceof jQuery) + 'datepicker5 return jquery obj:' + (datepicker5 instanceof jQuery) + 'dWrapper return jquery obj:' + (dWrapper instanceof jQuery));
			// 注意不要在同一個dom元素之上重複初始化元件
			
			$("#grid").kendoGrid();
			//$("#grid").data("kendoGrid").destroy(); // destroy the grid
			//$("#grid").empty(); // empty the grid content(inner HTML)
			//$("#grid").remove(); // remove all grid HTML
			//kendo.destroy(document.body); // destroy multiple kendo ui widgets; all widgets no longer work
			
		});
	</script>
</body>
</html>