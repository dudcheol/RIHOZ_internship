$.ajax({
		type:"GET",
		url:"http://www.kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchWeeklyBoxOfficeList.json?key=430156241533f1d058c603178cc3ca0e&targetDt=20190901",
		success: function(data){
			var movie = JSON.stringify(data);
			console.log(movie);
		}
});

function loadDoc(){
	/*var xhttp = new new XMLHttpRequest();
	xhttp.onreadystatechange = function(){
		if(this.readyState == 4 && this.status == 200){
			var response = JSON.parse(this.responseText);
			document.getElementById("print").innerHTML = response.ip;
		}
	};
	xhttp.open("GET","https://naveropenapi.apigw.ntruss.com/map-place/v1/search?query=선릉역&coordinate=127.034529,37.501503",false);
	xhttp.setRequestHeader("X-NCP-APIGW-API-KEY-ID", "0yfv84wqze");
	xhttp.setRequestHeader("X-NCP-APIGW-API-KEY", "eanDVyUdQiC0D7AYMIec1pHwMKuHSEfVfc33EiL4");
	xhttp.send();*/
}