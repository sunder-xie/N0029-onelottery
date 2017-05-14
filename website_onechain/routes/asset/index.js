'use strict'
const wrapper = require('co-mysql');
const pool = wrapper(global.pool);
var config = require('../../config');

exports.get = function*() {
	yield getData;
}


function* getData() {
	var sql = "select balance from UserInfo where userId = '" + config.args.userHash + "'";
	var ary = [];
	ary.push(pool.query(sql));
	sql = "select sum(amount) total,sum(fee) fee from TransactionDetail where type = 1";
	ary.push(pool.query(sql));
	sql = "select sum(fee) fee from TransactionDetail where type = 5 and myHash = '" + config.args.userHash + "'";
	ary.push(pool.query(sql));
	sql = "select sum(fee) fee from TransactionDetail where type = 6 and myHash = '" + config.args.userHash + "'";
	ary.push(pool.query(sql));
	sql = "select sum(amount) fee from TransactionDetail where type in (11,14,16)";
	ary.push(pool.query(sql));
	sql = "select sum(fee) fee from TransactionDetail";
	ary.push(pool.query(sql));
	var now = new Date();
	var datas = yield ary;
	yield this.render('asset/index', {
		data: datas,
		date: now.Format('yyyy/MM/dd hh:mm:ss')
	});
}