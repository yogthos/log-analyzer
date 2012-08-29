function hitsByOS(d) {
	$.plot($("#hits-by-os"), d,
	{series: {pie: {show: true}},
     legend: {show: false}});
}

function hitsByTime(d) {
	$.plot($("#hits-by-time"), [d], { xaxis: { mode: "time", minTickSize: [1, "hour"]}});
}

function hitsByRoute(d) {     
	var options = {
        series: {bars: { show: true, barWidth: 0.3, align:'center'}},
        multiplebars:true};
	$.plot($("#hits-by-route"), d, options);
}

$(document).ready(function(){	
	$.post('/stats-viewer/get-logs', 
	       function(data){	       	
	           hitsByTime(data.time);
	           hitsByOS(data.os);
	           //hitsByRoute(data.route);
	       });		
});

