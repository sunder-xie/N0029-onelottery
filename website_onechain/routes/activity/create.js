'use strict'
const wrapper = require('co-mysql');
const pool = wrapper(global.pool);
var config = require('../../config');

exports.get = function*(){
	var sql = 'select * from Rule';
	var rules = yield pool.query(sql);
	yield this.render('activity/create',{
		rules:rules
	})
}