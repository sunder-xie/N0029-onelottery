var schedule = require("node-schedule");
var pool = require('../../import/mysqlPool');
var config = require('../../config');
var async = require('async');

var rule = new schedule.RecurrenceRule();
rule.hour = 0;

var j = schedule.scheduleJob(rule, function() {
	faileActivity();
	successActivity();
	ruleActivity();
	confirmWithdraw();
	console.log("执行任务");
});
// faileActivity();
// successActivity();
// ruleActivity();

// var rule1 = new schedule.RecurrenceRule();
// var times1 = [1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25, 27, 29, 31, 33, 35, 37, 39, 41, 43, 45, 47, 49, 51, 53, 55, 57, 59];
// rule1.minute = times1;
// schedule.scheduleJob('0 * * * * *', function(){
// schedule.scheduleJob(rule1, function() {
	// testtt();
// })
// 设定定时器，执行提现自动确认功能。
// setInterval(function() {
// 	console.log("提现自动确认开启",new Date());
// 	confirmWithdraw();
// }, 1000 * 60); // 60 s

// function testtt() {
// 	console.log('time:',new Date());
// }

function faileActivity() {
	var date = new Date();
	console.log(date)
	var endDate = new Date(date.getFullYear(), date.getMonth(), date.getDate());
	var startDate = new Date(date.getFullYear(), date.getMonth(), date.getDate());
	startDate.setDate(startDate.getDate() - 1);
	console.log(startDate, endDate);
	var sql = "select count(*)count from " +
		"(select  (case   " +
		" when closeTime > ? and closeTime < ? and curBetAmount < total then '失败的活动' " +
		"  end) type, closeTime from OneLottery ) t where type='失败的活动'";
	pool.query(sql, [startDate, endDate], function(err, data) {
		console.log(err, data);
		var sql = "insert into activityStatics set ?";
		pool.query(sql, {
			date: startDate,
			type: '失败的活动',
			count: data[0].count
		}, function(err) {
			if (err) {
				console.log(err);
			} else {
				console.log('失败的活动统计成功！')
			}
		})
	})
}
//成功的活动
function successActivity() {
	var date = new Date();
	console.log(date)
	var endDate = new Date(date.getFullYear(), date.getMonth(), date.getDate());
	var startDate = new Date(date.getFullYear(), date.getMonth(), date.getDate());
	startDate.setDate(startDate.getDate() - 1);
	console.log(startDate, endDate);
	var sql = "select count(*)count from " +
		"(select  (case   " +
		" when curBetAmount = total and lastCloseTime > ? and lastCloseTime < ? then '成功的活动' " +
		"  end) type, closeTime from OneLottery ) t where type='成功的活动'";
	pool.query(sql, [startDate, endDate], function(err, data) {
		console.log(err, data);
		var sql = "insert into activityStatics set ?";
		pool.query(sql, {
			date: startDate,
			type: '成功的活动',
			count: data[0].count
		}, function(err) {
			if (err) {
				console.log(err);
			} else {
				console.log('成功的活动统计成功！')
			}
		})
	})
}
//活动规则统计
function ruleActivity() {
	var date = new Date();
	console.log(date)
	var endDate = new Date(date.getFullYear(), date.getMonth(), date.getDate());
	var startDate = new Date(date.getFullYear(), date.getMonth(), date.getDate());
	startDate.setDate(startDate.getDate() - 1);
	console.log(startDate, endDate);
	var sql = "select count(*)count, percentage from " +
		"(select  percentage  from OneLottery a join Rule b where a.ruleId = b.id and createTime > ? and createTime < ?) t group by percentage ";
	pool.query(sql, [startDate, endDate], function(err, data) {
		console.log(err, data);
		async.map(data, function(item,cb) {
			var sql = "insert into activityStatics set ?";
			pool.query(sql, {
				date: startDate,
				type: item.percentage,
				count: item.count
			}, function(err) {
				if (err) {
					console.log(err);
				} else {
					console.log(item.percentage, '活动统计成功！');

				}
				cb(null);
			})
		})


	})
}


//自动确认5天后的已打款提现申请
function confirmWithdraw() {
	var date = new Date();
	var startDate = new Date(date.getFullYear(), date.getMonth(), date.getDate(), date.getHours(), date.getMinutes(), date.getSeconds());
	startDate.setDate(startDate.getDate() - 5);
	console.log(startDate);
	
	var sql = " select * from Withdraw where state = 2 and updateTime < ? ";
	pool.query(sql, [startDate], function(err, data) {
		if (err) {
			console.log('Withdraw fetch err:',err);
		} else {
			console.log(data.length)
			if (data.length > 0) {
				// 遍历全部已打款的提现
				async.map(data, function(item, cb) {
					dowithdraw(item, function() {
						cb();
					})
				}, function(err) {
					console.log('Withdraw auto confirm err:', err);
				})

				// var sequence = Promise.resolve();
				// data.forEach(function(item) {
				// 	sequence = sequence.then(function() {
	   //                 return withdraw(item);
				// 	});
				// })
			};
		}

	})
}


function dowithdraw(withdraw, cb) {
	console.log('withdraw item:',withdraw.id);
	var args = {
		TxId: withdraw.id,
		UserId: config.userName,
		Extras: config.userName,
		ModifyTime: new Date().getTime()
	};
	args = JSON.stringify(args);
	console.log('withdraw args:',args);

	chain.invoke('zxCoinWithdrawConfirm', ['', args]).then(function(data, err) {
		console.log('withdraw args1:',args);
		if (err || data.code != 0) {
			console.log("提现 " + withdraw.id + " 后台自动确认失败", err, 'result:', data);
			cb();
		} else {
			console.log("提现 " + withdraw.id + " 后台自动确认成功");
			cb();
		}
	});
}

function withdraw(withdraw) {
	var args = {
		withdrawID: withdraw.id,
		username: config.userName,
		Extras: config.userName
	};
	args = JSON.stringify(args);
	return chain.invoke('zxCoinWithdrawConfirm', [args]).then(function(err, data) {
		if (err) {
			console.log("提现 " + withdraw.id + " 后台自动确认失败", err);
		} else {
			var sql = "update Withdraw set ? where id = ?";
			pool.query(sql, [{
				state: 8,
				updateTime: new Date()
			}, withdraw.id], function(err, data) {
				if (err) {
					console.log("提现 " + withdraw.id + " 后台自动确认失败", err);
				} else {
					console.log("提现 " + withdraw.id + " 后台自动确认成功");
				}
			})

		}
	});
}
