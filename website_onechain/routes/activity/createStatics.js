'use strict'
const wrapper = require('co-mysql');
const pool = wrapper(global.pool);
const config = require('../../config');


exports.get = function*() {
	var sql = "select * from Rule";
	var rules = yield pool.query(sql);
	var queryDate = this.request.query.date.split('-');
	console.log("cccc-------------"+this.request.query.date);
	var startDate = queryDate[0].split('/');
	var endDate = queryDate[1].split('/');
	console.log(startDate, endDate)
	startDate = new Date(startDate[0], startDate[1] * 1 - 1, startDate[2]-1);
	endDate = new Date(endDate[0], endDate[1] * 1 - 1, endDate[2]);
	startDate.setHours(24);
	endDate.setHours(24);
	console.log(startDate, endDate)
	var sqlAry = [];
	var typeAry = ['失败的活动', '成功的活动'];
	var length = (endDate.getTime() / 1000 - startDate.getTime() / 1000) / (24 * 3600);
	//失败的活动
	sql = "select sum(count) total,date from  (select count ,DATE_FORMAT(date,'%m-%d') date from activityStatics where date > ? and date < ? and type = '失败的活动') t group by date";
	sqlAry.push(pool.query(sql, [startDate, endDate]));
	//成功的活动
	sql = "select sum(count) total,date from  (select count ,DATE_FORMAT(date,'%m-%d') date from activityStatics where date > ? and date < ? and type = '成功的活动') t group by date";
	sqlAry.push(pool.query(sql, [startDate, endDate]));
	for (var i = 0; i < rules.length; i++) {
		sql = "select sum(count) total,date from  (select count ,DATE_FORMAT(date,'%m-%d') date from activityStatics where date > ? and date < ? and type = ?) t group by date";
		sqlAry.push(pool.query(sql, [startDate, endDate, rules[i].percentage]));
		typeAry.push('获奖者得' + rules[i].percentage + '%');
	}
	var data = yield sqlAry;
	console.log(data)
	var dateAry = getDate(startDate, length);
	var aryTotal = [];
	var aryAvg = [];
	for (var k = 0; k < data.length; k++) {
		var j = 0;
		var ary = [];
		var total = 0;
		for (var i = 0; i < dateAry.length; i++) {
			if (data[k][j] && data[k][j].date == dateAry[i]) {
				console.log(data[k][j])
				ary.push(data[k][j].total);
				total += data[k][j].total;
				j++;
			} else {
				ary.push(0);
			}
		};
		aryAvg.push(parseInt(total / length));
		aryTotal.push(ary);

	}

	console.log(aryAvg);

	var finalData = [];
	for (var i = 0; i < aryTotal.length; i++) {
		finalData.push({
			name: typeAry[i],
			type: 'line',
			data: aryTotal[i]
		})
	}
	this.response.body = {
		ary1: dateAry,
		ary2: finalData,
		ary3: typeAry,
		avgs: aryAvg
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