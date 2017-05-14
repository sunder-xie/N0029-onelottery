'use strict'
const wrapper = require('co-mysql');
const pool = wrapper(global.pool);
var config = require('../../config');


//充值统计
exports.get = function*() {
	var queryDate = this.request.query.date.split('-');
	console.log("bbbb-------------"+this.request.query.date);
	var startDate = queryDate[0].split('/');
	var endDate = queryDate[1].split('/');
	console.log(startDate, endDate)
	startDate = new Date(startDate[0], startDate[1] * 1 - 1, startDate[2]-1);
	endDate = new Date(endDate[0], endDate[1] * 1 - 1, endDate[2]);
	startDate.setHours(24);
	endDate.setHours(24);
	var length = (endDate.getTime() / 1000 - startDate.getTime() / 1000) / (24 * 3600);
	var sql = "select sum(amount) total,date from  (select amount ,DATE_FORMAT(time,'%m-%d') date from TransactionDetail where time > ? and time < ? and type = 1) as a group by date";
	var data = yield pool.query(sql, [startDate, endDate]);
	var dateAry = getDate(startDate, length);
	var j = 0;
	var ary = [];
	var total = 0;
	for (var i = 0; i < dateAry.length; i++) {
		if (data[j] && data[j].date == dateAry[i]) {
			ary.push(data[j].total / 10000);
			total += data[j].total / 10000;
			j++;
		} else {
			ary.push(0);
		}
	};
	this.response.body = {
		ary1: dateAry,
		ary2: ary,
		avg: Math.round(total / dateAry.length)
	};
}

function getDate(begin, length) {
	var ary = [];
	for (var i = 0; i <= length; i++) {
		ary.push(begin.Format('MM-dd'));
		begin.setDate(begin.getDate() + 1)
	};
	return ary;
}