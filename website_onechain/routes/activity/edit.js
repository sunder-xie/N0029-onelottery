'use strict'
const wrapper = require('co-mysql');
const pool = wrapper(global.pool);
var config = require('../../config');

exports.get = function*() {
	var sql = "select * from Rule";
	var rules = yield pool.query(sql);
	if (this.request.query.id) {
		sql = "select * from OneLottery where lotteryId = ? "
		var lottery = (yield pool.query(sql, [this.request.query.id]))[0];
		lottery.last = compareDate(lottery.closeTime.getTime() ,lottery.startTime.getTime());
        lottery.startTime = lottery.startTime.Format("yyyy-MM-dd hh:mm")

		yield this.render('activity/modify', {
			lottery: lottery,
			rules: rules
		});
		return;
	}
	sql = "select name,lotteryId,creater,ruleId,startTime from OneLottery where creater is not null and startTime>now() order by startTime";
	var result = yield pool.query(sql);

	var rule = {};
	for (var i = 0; i < rules.length; i++) {
		rule[rules[i].id] = rules[i].percentage;
	}
	for (var i = 0; i < result.length; i++) {
		result[i].startTime = result[i].startTime.toLocaleString();
		result[i].rule = rule[result[i].ruleId];
	}
	yield this.render('activity/edit', {
		activity: result
	})
}

function compareDate(end, start) {
	console.log(end,start)
	var intervalTime = end - start; //两个日期相差的毫秒数 一天86400000毫秒 
	var Inter_Days = (intervalTime / 86400000); //加1，是让同一天的两个日期返回一天 
	return Inter_Days;
}