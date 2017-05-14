var pool = require('../../import/mysqlPool');
var co = require('co');

function getFailedLottery() {
	try {
		console.log('开始退款', new Date().Format('yyyy-MM-dd hh:mm:ss'));
		//1.查询全部失败的活动，并且有实际投注的,而且退款状态不是已退款的
		var sql = "select * from OneLottery where closeTime < now() and curBetCount < maxAttendeeCnt "
		 + " union select * from OneLottery where closeTime < date_sub(now(), interval 24 hour) and curBetCount = maxAttendeeCnt  and state < 3 ";

		pool.query(sql, function(err, data) {
			if (err) {
				console.log('查询活动失败', err);
			} else {
				console.log(data.length)
				if (data.length > 0) {
					// 遍历全部失败活动，
					var sequence = Promise.resolve();
					data.forEach(function(item) {
						sequence = sequence.then(function() {
                           return refund(item);
						});
					})
				};
			}

		});
	} catch (e) {
		console.log('err:', e, lottery.name, " 退款失败");
	}

}
function refund(lottery) {
	var args = {
		lotteryID: lottery.lotteryID,
		currentTime: new Date().getTime()
	};
	args = JSON.stringify(args);
	return chain.invoke('oneLotteryRefund', ['', args]).then(function(data) {
		console.log(data.message, lottery.name, " 退款成功");
	});
}
// 设定定时器，执行自动退款功能。
setInterval(function() {
	getFailedLottery();
}, 1000 * 60*60*2); // 60 minutes