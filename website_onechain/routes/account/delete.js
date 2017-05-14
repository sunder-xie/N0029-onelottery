'use strict'
const wrapper = require('co-mysql');
const pool = wrapper(global.pool);
exports.get = function*() {
	var name = this.request.query.name;
	var sql = "delete from Manager where name = ?";
	yield pool.query(sql, [name]);
	this.redirect('/account');
}