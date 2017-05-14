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
	// 状态 1申请中 2已打款 3已确认 4提现失败（申请被拒） 5客户申诉 6确认到账（申诉驳回）7提现失败（退回众享币） 8后台自动确认
	sql = "select id,userId,name,amounts," +
		" (case state when 1 then '审核中' " +
		" when 2 then '已打款' " +
		" when 3 then '已确认(用户确认)' " +
		" when 4 then '提现失败(申请撤销)' " +
		" when 5 then '客户申诉' " +
		" when 6 then '已确认(申诉驳回)' " +
		" when 7 then '提现失败(申诉成功)' " +
		" when 8 then '已确认(自动确认)' " +
		" end) state, createTime ";

	var wherecls = " from Withdraw ";
	
	var hasState = false;
	if (param.statu != 'all') {
		var sta;
		switch (param.statu) {
			case '审核中':
				wherecls += " where state = 1 ";
				break;
			case '已打款':
				wherecls += " where state = 2 ";
				break;
			case '已确认':
				wherecls += " where state in (3,6,8) ";
				break;
			case '提现失败':
				wherecls += " where state in (4,7) ";
				break;
			case '客户申诉':
				wherecls += " where state = 5 ";
				break;
		}

		hasState = true;
	};

	if (param.name != '') {
		if (!hasState) {
			wherecls += ' where ';
		} else {
			wherecls += ' and ';
		}
		if (param.name.length > 30) {
			wherecls += " userId = ? ";
		} else {
			wherecls += " name = ? ";
		}
	};

	console.log("select count(*) count " + wherecls);
	count = yield pool.query("select count(*) count " + wherecls, [param.name]);
	count = count[0].count;

	sql += wherecls;
	sql += (" order by createTime desc limit " + (currentPage - 1) * eachPage + "," + eachPage);
	console.log("查找提现记录 " + sql)

	data = yield pool.query(sql, [param.name]);

	var pageCount = parseInt(count / eachPage) + (count % eachPage == 0 ? 0 : 1);
	yield this.render('transfer/withdrawlist', {
		data: data,
		pageCount: count,
		currentPage: currentPage,
		eachPage: eachPage
	});
}