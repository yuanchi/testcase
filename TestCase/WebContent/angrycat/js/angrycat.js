(function(host){"use strict"
	var angrycat = host.angrycat || {}; 
	host.angrycat = angrycat;
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
	angrycat.core = {
		once: once,
		minusTimezoneOffset: minusTimezoneOffset,
		plusTimezoneOffset: plusTimezoneOffset
	};
})(window);