function hitsByOS(d) {
	$.plot($("#hits-by-os"), d,
	{series: {pie: {show: true}},
     legend: {show: false}});
}

function hitsByTime(d) {
   $.plot($("#hits-by-time"), [d],
    {xaxis: { mode: "time", minTickSize: [1, "minute"]},
     xaxes: [{position: "bottom", axisLabel: "Time"}],
     yaxes: [{position: "left", axisLabel: "Hits"}]});
}

function hitsByRoute(d) {     
	$.plot($("#hits-by-route"), d,
	{series: {pie: {show: true,
	                combine: {color: '#999',
                              threshold: 0.03}}},
     legend: {show: false}});
}

$(document).ready(function(){
    var context = $('#context').val();
    var url = context ? context + '/get-logs' : '/get-logs';	
	$.post(url, 
	       function(data){	       	   
	       	   $("#total").text("total hits: " + data.total);	       	
	           hitsByTime(data.time);
	           hitsByOS(data.os);
	           hitsByRoute(data.route);
	       });		
});

