'use strict'
const wrapper = require('co-mysql');
const pool = wrapper(global.pool);
const config = require('../../config');

exports.get = function*() {
	var args = {};
	args.txnID = this.request.query.id;
	args.updateTime = new Date().getTime();
	args = JSON.stringify(args);
	console.log(args)
	try {
		var result = yield chain.invoke('oneLotteryDelete', ['', args]);
		console.log(result)

		if (result.code == 0) {
			var sql = "delete from OneLottery where lotteryID = '" + this.request.query.id +" '";
			yield pool.query(sql);
			/*this.redirect('/activity');*/
			this.response.body="删除成功";
			return;
		}
		this.response.body="删除失败";
	} catch (e) {
		console.log(e);
		this.response.body="删除失败";
	}
}

function getDate(str) {
	var date = str.replace(/-/g, ',');
	date = date.replace(/:/g, ',');
	date = date.replace(/T/g, ',')
	date = date.split(',');
	console.log(date)
	date = new Date(date[0], parseInt(date[1]) - 1, date[2], date[3], date[4]);
	return date;
}