// 상수 const 변수 let
let users = [
	{id:1,name: 'Alice'},
	{id:2,name: 'Bek'},
	{id:3,name: 'Chris'}
]

const index = (req, res) => {
	// localhost:3000/users?limit=1
	// limit에 값이 없으면 10을 쓴다
	req.query.limit = req.query.limit || 10
	// limit가 문자열로 오므로 10진수로 변경
	const limit = parseInt(req.query.limit , 10)

	// limit가 숫자가 아니면 400 응답
	// isNaN() : 문자면 true , Not a Number
	if(Number.isNaN(limit)){
		res.status(400).end()
	}else{
		// users객체를 0부터 limit까지만 싣자
		res.json(users.slice(0, limit))
	}
}
const show = (req, res) => {
	const id = parseInt(req.params.id,10)

	// id가 숫자가 아니면 400 응답
	if(Number.isNaN(id)){
		return res.status(400).end()
	}

	// users에 있는 객체와 입력받은 id를 비교한것중 같은것만 반환해서 저장
	const user = users.filter(user => user.id === id)[0]

	// 찾을 수 없는 id이면 404 응답
	if(!user){
		return res.status(404).end()
	}

	res.json(user)
}
const destroy = (req, res) => {
	const id = parseInt(req.params.id,10)

	// id가 숫자가 아니면 400 응답
	if(Number.isNaN(id)){
		return res.status(400).end()
	}

	// 삭제가 정상적으로 완료되면 204 응답
	// users객체에서 id와 같지 않은 애들만 추려낸다
	users = users.filter(user => user.id !== id)
	res.status(204).end()
}
const create = (req,res) => {
	const name = req.body.name

	// name이 없으면 400 응답
	if(!name){
		return res.status(400).end()
	}

	// name이 중복이면 409 응답
	// 조건문에 0이면 거짓이어서 넘어가게끔 해야하므로 length
	if(users.filter(user => user.name === name).length){
		return res.status(409).end()
	}

	const id = Date.now() // 현재 시각을 초 단위로
	const user = {id , name} // es6 문법. 위 상수 id,name키에 값으로 들어감

	users.push(user)
	res.status(201).json(user)
}

module.exports = {
	index,
	show,
	destroy,
	create
}