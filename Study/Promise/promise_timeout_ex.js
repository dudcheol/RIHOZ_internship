// 비동기 처리를 쉽게해보자!
// 왜? API를 통해 서버에서 데이터 요청해서 받아오는 것을 수행할 때,
// 자바스크립트는 데이터를 받아오기도 전에 마치 데이터를 받아온 것 마냥 화면에 데이터를 표시하려고 한다
// 아직 안받아왔으니까 null 일텐데 받아온 거로 쳐서 다음으로 넘어가버리니까 오류 되겠지?
// 이 문제점을 해결하려고 콜백함수를 쓰는데 이걸 좀 더 쉽게 하기 위해 사용하는 것이 프로미스!!!!!!
	
// 프로미스의 3가지 상태
// new Promise를 생성하고 종료될때 까지 3가지 상태를 가진다 - pending(대기), fulfilled(이행or완료), rejected(실패)
new Promise(function(resolve,reject){
	// 여기서 pending상태 진입
	// 함수 인자로 resolve, reject 접근 가능
	setTimeout(function(){
		// resolve를 실행하면 fulfilled상태가됨
		resolve(1);
	},2000);
})
// resolve()의 결과를 then으로 받을 수 있음
.then(function(result){
	console.log(result);
	return result + 10;
})
// then()으로 여러 개의 프로미스를 연결할 수 있음
.then(function(result){
	console.log(result);
	return result + 20;
})
.then(function(result){
	console.log(result);
});