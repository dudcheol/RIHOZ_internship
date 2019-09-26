function getData(){
	return new Promise(function(resolve, reject){
		resolve(new UserInfo("박영철","m",25));
	});
}

getData().then(function(result){
	console.log(result.name+", "+result.gender+", "+result.age);
	result.SayHello(result.name);
});


function UserInfo(name, gender, age){
	this.name = name;
	this.gender = gender;
	this.age = age;
}
UserInfo.prototype.SayHello = function(name){
	console.log("안녕하세요~ %s입니다.",name);
};