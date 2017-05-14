// var Promise = require('bluebird').Promise;

// var exa = new Promise(function(reslove,reject){
// 	setTimeout(function(){
// 		reslove();
// 	})
// })
var str = '16 27 9a 97 b0 98 f8 c7 80 62 e4 32 c4 66 86 2e de 25 12 06 68 05 ad 8b b1 50 2d 9a b6 f3 44 bf'
console.log(str.replace(/\s/g, ""));

console.log(new Date('2017-01-26 23:03'))
console.log("update OneLottery set curBetAmount = curBetAmount + " + 1000 + " and curBetAmount = curBetAmount + " + 10 + " where lotteryID = ?")
var str = '?currentPage=2';

console.log(str.indexOf('currentPage') !=-1)
var url = ''
var search = str.split('=');
var length = search[search.length - 1].length;
url += (str.substr(0, str.length - length) + 5);
console.log(url)