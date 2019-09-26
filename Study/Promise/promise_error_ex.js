// promiseptj ,, Reject란 실패 메서드!

function getData(){
	return new Promise(function(resolve, reject){
		reject(new Error("Opps!"));
	});
}

getData().then().catch(function(err){
	//여기에 에러가 난 이유가 나옴
	console.log(err);
});