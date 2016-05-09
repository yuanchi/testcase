(function(host){"use strict"
	host.angrycat = host.angrycat || {};
	var angrycat = host.angrycat; 

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
	function plusTimezoneOffset(d){
		var hours = d.getHours(),
			mins = d.getMinutes(),
			secs = d.getSeconds(),
			milliSecs = d.getMilliseconds();
		d.setHours(hours, mins+d.getTimezoneOffset(), secs, milliSecs);
		return d;				
	}
	function assert(value, desc){
		var li = document.createElement("li"),
			passColor = "green",
			failColor = "red";
		li.style.color = value ? passColor : failColor;
		li.appendChild(document.createTexeNode(desc));
		var results = document.getElementById("results");
		if(!results){
			results = document.createElement("ul");
			results.id = "results";
		}
		results.appendChild(li);
	}
	angrycat.core = {
		once: once,
		minusTimezoneOffset: minusTimezoneOffset,
		plusTimezoneOffset: plusTimezoneOffset
	};
})(window);