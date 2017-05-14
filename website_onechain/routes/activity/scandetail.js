'use strict'
const wrapper = require('co-mysql');
const pool = wrapper(global.pool);
var config = require('../../config');
var eachPage = config.eachPage;
exports.get = function*() {
	console.log(this.request.query);
	var param = this.request.query;
	var sql = '';
	var count = 0;
	var data = {};
	var currentPage = 1;
	if (this.request.query.currentPage) {
		currentPage = parseInt(this.request.query.currentPage);
	}
	var date = this.request.query.date.split('-');
	var type = parseInt(this.request.query.type);
	var startDate = new Date(date[0]);
	var endDate = new Date(date[1]);
	endDate.setDate(endDate.getDate() + 1);
	var now = new Date();
	sql = "from (select  lotteryID,name,ruleId,(case when publisherName ='one_chain_admin' then '官方'" +
		" else '个人' end) type," +
		" (case  when startTime <= ? and closeTime >= ? and curBetAmount != total then '进行中' " +
		" when startTime > ?  then '未开始' " +
		" when closeTime < ? and curBetAmount < total then '失败' " +
		" when curBetAmount = total  then '成功' " +
		"  end) statu ,percentage,createTime,publisherName,publisherHash,state,curBetAmount from OneLottery a join Rule b where " +
		" a.ruleId = b.id ) t where createTime > ? and createTime < ?";
	if (param.type != 'all') {
		sql += " and type = '" + param.type + "'";
	};
	if (param.statu != 'all') {
		sql += " and statu = '" + param.statu + "'";
	};

	if (param.ruleId != 'all') {
		sql += " and ruleId = '" + param.ruleId + "'";
	};
	if (param.userId != '') {
		if (param.userId.length > 30) {
			sql += " and publisherHash = '" + param.userId + "'";

		} else {
			sql += " and publisherName = '" + param.userId + "'";
		}
	};
	count = yield pool.query("select count(*) count " + sql, [now, now, now, now, startDate, endDate]);
	count = count[0].count;
	sql += "order by createTime desc limit " + (currentPage - 1) * eachPage + "," + eachPage;
	// console.log("select * " + sql)

	data = yield pool.query("select * " + sql, [now, now, now, now, startDate, endDate]);

	var pageCount = parseInt(count / eachPage) + (count % eachPage == 0 ? 0 : 1);
	yield this.render('activity/scandetail', {
		data: data,
		pageCount: count,
		currentPage: currentPage,
		eachPage:eachPage
	});
}