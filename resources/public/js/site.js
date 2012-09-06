function labelFormat(label, series){
       return '<div style="font-size:8pt;text-align:center;padding:2px;color:'+ series.color +';">'+label+'<br/>'+Math.round(series.percent)+'%</div>';
}
function hitsByOS(d) {
       $.plot($("#hits-by-os"), d,
       {series: {pie: {show: true,
                       label: {show: true,
                               formatter: labelFormat}}},
    legend: {show: false}});
}

function hitsByBrowser(d) {
       $.plot($("#hits-by-browser"), d,
       {series: {pie: {innerRadius: 0.4,
                       show: true,
                       label: {show: true,
                               formatter: labelFormat}}},
    legend: {show: false}});
}

function hitsByCountry(d) {	
	$.plot($("#hits-by-country"), d,
	{series: {pie: {show: true,
			  		combine: {color: '#999',
                              threshold: 0.03}}},
     legend: {show: false}});
}

function hitsByTime(d, div) {
   $.plot(div, [d],
    {xaxis: { mode: "time", minTickSize: [1, "minute"]},
     xaxes: [{position: "bottom", axisLabel: "Time"}],
     yaxes: [{position: "left", axisLabel: "Hits"}]});
}

function hitsByRoute(d) {
       $.plot($("#hits-by-route"), d,
       {series: {pie: {show: true,
                       label: {show: true,
                               formatter: labelFormat},
                       combine: {color: '#999',
                                 threshold: 0.03}}},
    legend: {show: false}});
}


$(document).ready(function(){
    var context = $('#context').val();
    var url = context ? context + '/get-logs' : '/get-logs';	
	$.post(url, 
	       function(data){	       	   
	       	   $("#unique").text("total unique hits: " + data.uniquehits);
	       	   $("#total").text("total hits: " + data.allhits);	       	          
	           hitsByTime(data.time, $("#hits-by-time"));
	           hitsByTime(data.alltime, $("#all-hits-by-time"));
	           hitsByOS(data.os);
	           hitsByRoute(data.route);
	           hitsByBrowser(data.browser);
	           //hitsByCountry(data.country);
	       });		
});

