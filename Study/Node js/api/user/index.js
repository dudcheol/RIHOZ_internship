/*
첫 API 테스트 만들기
성공
	유저 객체를 담은 배열로 응답한다
	최대 limit 갯수만큼 응답한다
실패
	limit이 숫자형이 아니면 400 응답
	offset이 숫자형이 아니면 400 응답
*/

// 코드 리팩토링
// 라우터 사용해서 다른 곳에서 이걸 가져다 쓰는 식(?)
const express = require('express')
const router = express.Router()
const ctrl = require('./user.ctrl')

router.get('/', ctrl.index)

// :를 쓰는 이유?
// :id라고 하면 id값은 변수로 사용 가능
// id를 자동으로 바인딩해준다.
router.get('/:id', ctrl.show)

// delete용 라우팅 생성
router.delete('/:id', ctrl.destroy)

// post용 라우팅 생성
router.post('/', ctrl.create)


// 다른 곳에서 이것을 쓰기 위해 모듈로 만들어야한다!
module.exports = router