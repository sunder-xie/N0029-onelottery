'use strict'
var config = require('../../config');
const wrapper = require('co-mysql');
const pool = wrapper(global.pool);

//用户增长统计
exports.get = function*() {
	var queryDate = this.request.query.date.split('-');
	var startDate = queryDate[0].split('/');
	var endDate = queryDate[1].split('/');
	console.log(startDate, endDate)
	startDate = new Date(startDate[0], startDate[1] * 1 - 1, startDate[2]-1);
	endDate = new Date(endDate[0], endDate[1] * 1 - 1, endDate[2]);
	startDate.setHours(24);
	endDate.setHours(24);
	var length = (endDate.getTime() / 1000 - startDate.getTime() / 1000) / (24 * 3600);
    var sql = "select count(*) count,date from (select DATE_FORMAT(activationTime,'%m-%d') date from UserInfo where name !='one_chain_admin') t group by date";
	var data = yield pool.query(sql, [startDate, endDate]);
	var dateAry = getDate(startDate, length);
	var j = 0;
	var ary = [];
	var total = 0;
	for (var i = 0; i < dateAry.length; i++) {
		if (data[j] && data[j].date == dateAry[i]) {
			ary.push(data[j].count);
			total += data[j].count;
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