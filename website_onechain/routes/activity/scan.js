
'use strict'
const wrapper = require('co-mysql');
const pool = wrapper(global.pool);
var config = require('../../config');



exports.get = function*() {
	var current = 1;

	if(this.query.current){
		current = parseInt(this.query.current);
	}
	var sql = 'select * from Rule';
	var rules = yield pool.query(sql);
	yield this.render('activity/scan', {
		rules: rules
	})

};

