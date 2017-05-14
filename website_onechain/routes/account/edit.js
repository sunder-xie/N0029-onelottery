'use strict'
const wrapper = require('co-mysql');
const pool = wrapper(global.pool);
const util = require('../../lib/util');
const config = require('../../config.json');

exports.post = function*() {
	var data = this.request.body;
	var sql = "update Manager set access = ? ,phone = ? ,password = ? where name = ? ";
	console.log("edit:"+data.access);
	if(data.access==undefined){
		data.access="";
	}
	try {
		console.log('修改用户:',data);
		// console.log('admin:',util.encode('admin', config.decode));
		// 如果是普通用户，需要先验证旧的密码，修改成功后还要重新登录
		if (this.session.user.access.indexOf('user') == -1) {
			var qsql = "select * from Manager where name = ? ";
			var result = (yield pool.query(qsql, [this.session.user.name]))[0];

			if (result.password === util.encode(data.pwd, config.decode)) {
				yield pool.query(sql,[data.access.toString(),data.phone,util.encode(data.newpwd,config.decode),data.name]);
				this.response.body = "修改成功";
				this.session=null;
			} else {
				this.response.body = "原密码输入错误";
			}
			
		} else {
			yield pool.query(sql,[data.access.toString(),data.phone,util.encode(data.pwd8,config.decode),data.name]);
			this.response.body = "修改成功";
		}
	} catch (e) {
		console.log(e);
		this.response.body = '修改失败';
	}
}