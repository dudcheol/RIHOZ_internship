// express는 기본으로 제공되는 것이 아니므로
// npm을 이용하여 사용자가 스스로 추가하여야 함.
// 명령어: npm i(install) express
// 참고: www.npmjs.com

const express = require('express')
const logger = require('morgan') // npm i morgan
const bodyParser = require('body-parser')
const app = express()
// 라우터 기능 사용하여 모듈로된 유저 정보를 가져옴
const user = require('./api/user')


app.use(logger('dev'))
// express의 body부분 문서에서 제공하는 양식 사용
// express에서 body는 지원하지 않아 body-parser라는 미들웨어를 사용해야함
app.use(bodyParser.json()) // json 형식으로 body를 받는다
app.use(bodyParser.urlencoded({extended: true})) // 인코딩을 해준다

// users라는 모든 경로에 대해서는 user라우터를 사용하겠다 라는 의미
// users 경로에 대해서 라우팅할 때에는 user라우터가 담당한다.
app.use('/users', user)



module.exports = app

