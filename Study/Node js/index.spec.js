// 테스트 주도 개발
// 테스트 코드 작성
// 보통 테스트 코드가 되는 파일에 .spec을 붙인다
// 검증모듈 assert , should 등
const assert = require('assert')
const should = require('should')
// 슈퍼테스트를 이용해서 자체적으로 테스트하기
const request = require('supertest')
const app = require('./express')

describe('GET /users', () => {
	describe('성공', () => {
		it('배열을 반환한다', (done) => {
			// 1과 1은 같아야만 한다
			//assert.equal(1,0)
			//(1).should.equal(1)
			request(app)
				.get('/users')
				.end((err, res) => {
					// 이 부분은 비동기로 동작. 따라서 이게 마쳤다는 것을 알려줘야함
					//console.log(res.body)
					// 배열인가?
					res.body.should.be.instanceof(Array)
					res.body.forEach(user => {
						// user가 property 'name'을 가지고 있나?
						user.should.have.property('name')
					})

					// test가 다 됐음을 알림
					done()
				})
		})
		it('최대 limit 갯수만큼 응답한다', done => {
			request(app)
				.get('/users?limit=2')
				.end((err,res)=>{
					res.body.should.have.lengthOf(2)
					done()
				})
		})
	})
	describe('실패', () => {
		it('limit이 정수가 아니면 400을 응답한다', done => {
			// limit에 문자가 왔기 때문에 400을 응답하고 그것으로 테스트는 끝난다
			request(app)
				.get('/users?limit=two')
				.expect(400)
				.end(done)
		})
	})
})

describe('GET /users/:id',() => {
	describe('성공', () => {
		it('유저 객체를 반환한다', done => {
			request(app)
			.get('/users/1')
			.end((err, res) => {
				res.body.should.have.property('id',1)
				done()
			})
		})
	})
	describe('실패', () => {
		it('id가 숫자가 아닐 경우 400을 응답한다', (done) => {
			request(app)
				.get('/users/one')
				.expect(400)
				.end(done)
		})
		it('찾을 수 없는 id일 경우 404 응답', (done) => {
			request(app)
				.get('/users/9')
				.expect(404)
				.end(done)
		})
	})
})

describe('DELETE /users/:id', () => {
	describe('성공', () => {
		it('204 응답', done => {
			request(app)
				.delete('/users/3')
				.expect(204)
				.end(done)
		})
	})
	describe('실패', () => {
		it('id가 숫자가 아닐 경우 400', done => {
			request(app)
				.delete('/users/three')
				.expect(400)
				.end(done)
		})
	})
})

describe('POST /users/:id', () => {
	describe('성공', () => {
		it('201을 응답하고 생성한 유저 객체 응답', done => {
			request(app)
				.post('/users')
				// post나 put을 요청할 때에는 body를 많이 사용함
				// body는 send()라는 함수로 보낼 수 있다
				.send({name: 'Daniel'})
				.expect(201)
				.end((err, res) => {
					res.body.should.have.property('name', 'Daniel')
					done()
				})
		})
	})
	describe('실패', () => {
		it('name이 없으면 400 응답', done => {
			request(app)
				.post('/users')
				.send({})
				.expect(400)
				.end(done)
		})
		it('name이 중복이면 409 응답', done => {
			request(app)
				.post('/users')
				.send({name: 'Alice'})
				.expect(409)
				.end(done)
		})
	})
})
// 201 < 자원이 추가되었음