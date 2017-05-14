'use strict'
const wrapper = require('co-mysql');
const pool = wrapper(global.pool);
var config = require('../../config');


//财富榜
exports.get = function*() {
	var sql = "select name ,balance value from UserInfo where name != 'one_chain_admin' and name != 'null' order by balance desc limit 0,10 ";
	var data = yield pool.query(sql);
	var ary1 = [];
	var ary2 = [];
	var arr = {};
	data.forEach(function(item,index) {
		arr[item.name]=index+1
		ary1.push(item.name);
		ary2.push({
			name: item.name,
			value: item.value/10000
		})
	})
	console.log(arr);
	this.response.body = {
		data: ary2.reverse(),
		time: new Date().Format('yyyy/MM/dd hh:mm:ss'),
		ary: ary1,
		arr:arr
	}
}