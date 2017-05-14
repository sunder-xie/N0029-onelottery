'use strict'
const wrapper = require('co-mysql');
const pool = wrapper(global.pool);
const util = require('../lib/util');
const config = require('../config.json');

exports.post = function*() {
	var data = this.request.body;
	var sql = "select * from Manager where name = ?";
	var result = (yield pool.query(sql, [data.name]))[0];
	
	// console.log(result.password,data.pwd,util.encode(data.pwd, config.decode));
	if (result.password === util.encode(data.pwd, config.decode)) {
		this.session.user = {
			name: result.name,
			phone: result.phone,
			access: result.access
		};
		this.response.body = 0;
	} else {
		this.response.body = 1;
	}
}

