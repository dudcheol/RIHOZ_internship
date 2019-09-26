// 2강(https://www.youtube.com/watch?v=VkIHMGnOhtE)에서 하려는 것
// 1. localhost:3000/users 라는 경로를 추가해서 유저 목록을 json형태로 받는 api를 만들 수 있다.
// 2. express JS를 할 수 있다.

// 기본모듈 http 사용
const http = require('http');
const fs = require('fs');

const hostname = '127.0.0.1';
// port 번호사용 . 보통은 80씀
const port = 3000;


// 노드js에서 기본으로 제공하는 양식
const server = http.createServer((req,res) => {
	// === < 자동형변환하지않고 비교
	if(req.url === '/'){
		// 상태코드는 http 상태 문서를 참고하여 설정하면 됨
		res.statusCode = 200;
		res.setHeader('Contetn-type','text/plain');
		// end << 응답바디로 포함해서 표현이 됨
		res.end('Hello PARK!\n');
	}else if (req.url === '/users'){
		const users = [
			{name : 'Alice'},
			{name : 'Beck'},
		]
		res.statusCode = 200;
		// json으로 응답을 할 때에는 text/plain을 아래와 같이 변경해야함
		res.setHeader('Contetn-type','application/json');
		// 주의! end로 응답을 할 때는 문자로!
		res.end(JSON.stringify(users));
	}
	
});



// listen = 서버를 요청대기 상태로 만듦 (서버를 구동한다, 띄운다 할 때 사용)
// 비동기로 활동
server.listen(port, hostname, () => {
	console.log('Server started on port '+port);
});

