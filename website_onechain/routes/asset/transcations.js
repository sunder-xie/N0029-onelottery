'use strict'
const wrapper = require('co-mysql');
const pool = wrapper(global.pool);
var config = require('../../config');
var eachPage = config.eachPage;

exports.get = function*() {
	console.log(this.request.query)
	var sql = '';
	var count = 0;
	var data = {};
	var currentPage = 1;
	if (this.request.query.currentPage) {
		currentPage = parseInt(this.request.query.currentPage);
	}
	var date = this.request.query.date.split('-');
	console.log("aaaaa-------------"+this.request.query.date);
	var type = parseInt(this.request.query.type);
	var startDate = new Date(date[0]);
	var endDate = new Date(date[1]);
	endDate.setDate(endDate.getDate()+1);
	console.log(startDate,endDate,type)
	sql += "from (select b.name,b.publisherHash myHash,a.type,a.amount,a.fee,a.time,a.operator  from "+
	       "TransactionDetail a  join OneLottery b where a.relatedHash = b.lotteryID "+
	       "and a.myHash = ? union all select relatedHash name,myHash,type,amount,fee,time,operator "+
	       "from TransactionDetail where myHash = ? and type =1  or type in (11,14,16) ) t where time > ? and time < ? ";
	if (type != 0) {
		if (type == 9) {
			sql += " and type in (11,14,16) ";
		} else {
			sql += " and type = ? ";
		}
	};
	count = yield pool.query("select count(*) count " + sql, [config.args.userHash, config.args.userHash, startDate, endDate, type]);
	count = count[0].count;
	sql += " order by time desc limit " + (currentPage-1)*eachPage + "," + eachPage;
	data = yield pool.query("select * " + sql, [config.args.userHash, config.args.userHash, startDate, endDate, type]);
	// console.log('sql',sql);
	// console.log('data',data);

	var pageCount = parseInt(count / eachPage) + (count % eachPage == 0 ? 0 : 1);
	yield this.render('asset/transcations', {
		data: data,
		json: {
			'1': "充值",
			'5': "创建活动",
			'6': "修改活动",
			'7': "活动分成",
            '11': "用户已确认",
            '14': "申诉驳回",
            '16': "后台自动确认",
		},
		pageCount: count,
		currentPage: currentPage,
		eachPage:eachPage
	});
}