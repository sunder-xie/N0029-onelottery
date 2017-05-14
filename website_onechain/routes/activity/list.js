
'use strict'
const wrapper = require('co-mysql');
const pool = wrapper(global.pool);
var config = require('../../config');



exports.get = function*() {
	var  sql = "select * from OneLottery where lotteryId = ? "
	var spl1 = "SELECT * FROM OneLotteryBet WHERE lotteryId = ? AND betNumbers LIKE ?"
	var sql2 = "SELECT u.name name, c.betcount betcount FROM UserInfo u ,(SELECT SUM(betcount) betcount FROM OneLotteryBet o WHERE o.lotteryId=? AND o.attendeeHash=?) c WHERE userId =?"

	if (this.request.query.lotteryID) {
		var lottery = (yield pool.query(sql, [this.request.query.lotteryID]))[0];
		var rewarduser={};
		if(lottery.lastCloseTime){
			console.log("lottery "+JSON.stringify(lottery));
			console.log("rewardNumbers "+lottery.rewardNumbers)
			var LotteryBet = (yield pool.query(spl1, [this.request.query.lotteryID,'%'+lottery.rewardNumbers+'%']))[0];
			console.log("attendeeHash "+LotteryBet.attendeeHash)
			var Betuser = (yield pool.query(sql2, [this.request.query.lotteryID,LotteryBet.attendeeHash,LotteryBet.attendeeHash]))[0];

			 rewarduser={
				rewardNumbers:lottery.rewardNumbers,
				name:Betuser.name,
				count:Betuser.betcount,
				allcost:parseInt(Betuser.betcount)*parseInt(lottery.cost)/10000
			}

			console.log("rewarduser "+rewarduser)
		}

		lottery.last = compareDate(lottery.closeTime.getTime() ,lottery.startTime.getTime());
		/*lottery.startTime = lottery.startTime.Format("yyyy-MM-dd hh:mm")
		lottery.closeTime = lottery.closeTime.Format("yyyy-MM-dd hh:mm")*/
		sql = "select * from Rule where id = ?";
		var rules = (yield pool.query(sql,lottery.ruleId))[0];
		yield this.render('activity/list', {
			lottery: lottery,
			rules: rules,
			rewarduser:rewarduser
		});
		return;
	}

};
function compareDate(end, start) {
	var intervalTime = end - start; //两个日期相差的毫秒数 一天86400000毫秒
	var Inter_Days = (intervalTime / 86400000); //加1，是让同一天的两个日期返回一天

	return Inter_Days;
}

