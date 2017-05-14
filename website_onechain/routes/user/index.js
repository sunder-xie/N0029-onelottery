'use strict'
var config = require('../../config');
const wrapper = require('co-mysql');
const pool = wrapper(global.pool);


exports.get = function*(){
    var sql = "select count(*) count from UserInfo where name !='one_chain_admin'";
    var userCount = yield pool.query(sql);

	yield this.render('/user/index',{
		userCount:userCount[0].count,
		date : new Date().Format('yyyy/MM/dd hh:mm:ss')
	})
}