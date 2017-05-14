var smsTypeDesc = {
	oneLotteryEdit: true,
	zxCoinTransfer: true,
	oneLotteryAdd: true,
	oneLotteryDelete: true,
	oneLotteryBet: true,
	oneLotteryBetOver: true,
	oneLotteryPrizeRuleAdd: true,
	oneLotteryPrizeRuleEdit: true,
	oneLotteryRefund: true
};


function EnumaKey() {
	for (var key in smsTypeDesc) {
		if (smsTypeDesc[key]) {

		console.log(key, '--------', smsTypeDesc[key]);
		smsTypeDesc[key] = false;
		};
	}
	if ("undefined" == typeof(smsTypeDesc)) {
		console.log("json对象不存在！");
		return
	};
	var key = 'aa';
	if ("undefined" == typeof(smsTypeDesc[key])) {
		console.log("输入的key:" + key + "， 在json对象中不存在！");
		return;
	}

}

function GetVal() {
	var key = prompt("请输入要查询的key", "4");
	if ("undefined" == typeof(smsTypeDesc)) return;
	if ("undefined" == typeof(smsTypeDesc[key])) {
		alert("输入的key:" + key + "， 在json对象中不存在！");
		return;
	}
	alert("您输入的key是：" + key + "，该key所对应的值是：" + smsTypeDesc[key]);
}

function jsontest() {
	var a=[];
	a.amount=10000;
	a.fee = 100;
	a.k=20;
	console.log(a);
	delete a.k;
	console.log(a);
	delete a.h;
	console.log("a.amount",a.amount,'fee=',a.fee,"json=",{fee:a.amount+a.fee});

	var bal = 123;
	var result = {
		code: 0,
		ban: -bal,
		message: '',
		data: {
			numbers: '10000000'
		}
	}
	result.data.owner='abcde';
	console.log(result);
	var txId="fork_1234";
	var item=[];
	item.attendeeHash='abcdef';
	var rr=(txId + '@' + item.attendeeHash.substr(0, 5));
	console.log({
		txId:txId + '@' + item.attendeeHash.substr(0, 5),
		a: 999
	}, txId.startsWith('fork'));
}

var pool = require('../import/mysqlPool.js');

function getUserName(name) {
	var sql = "select * from UserInfo";
	pool.query(sql,[name], function(err, hash) {
		if (hash.length < 1) {
			console.log('没有用户');
		} else {
			console.log(name,'hash=',hash[0].userId);
			for (var i = hash.length - 1; i >= 0; i--) {
				console.log(hash[i].name,'=====',hash[i].userId);
				if(hash[i].name==='a'){
					console.log('找到了',hash[i].userId);
					break;
				}
			};
		}
	});
}
jsontest();
// getUserName('cytf');
