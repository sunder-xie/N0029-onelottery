'use strict'
const wrapper = require('co-mysql');
const pool = wrapper(global.pool);

var chain = global.chain;
var config = require('../../config');

exports.get = function*() {
	var sql = "select id,userId,name,amounts,bank,account,accountName,orderNum," +
		" (case state when 1 then '审核中' " +
		" when 2 then '已打款' " +
		" when 3 then '已确认(用户确认)' " +
		" when 4 then '提现失败(申请撤销)' " +
		" when 5 then '客户申诉' " +
		" when 6 then '已确认(申诉驳回)' " +
		" when 7 then '提现失败(申诉成功)' " +
		" when 8 then '已确认(自动确认)' " +
		" end) statu, state, updateTime,appeal,remark from Withdraw where id = ?  ";

	if (this.request.query.withdrawID) {
		var withdraw = (yield pool.query(sql, [this.request.query.withdrawID]))[0];
		withdraw.updateTime = withdraw.updateTime.Format('yyyy-MM-dd hh:mm:ss');
		
		this.response.body = JSON.stringify(withdraw);
		return;
	}
	yield this.render('transferList');
}

