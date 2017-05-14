'use strict'
const wrapper = require('co-mysql');
const pool = wrapper(global.pool);
const util = require('../../lib/util');
const config = require('../../config.json');

exports.get = function*() {
	var accounts;
	console.log(this.session.user)
	if (this.session.user.access.indexOf('user') != -1) {
		var sql = "select * from Manager where name != 'admin' and name != ? order by createTime desc ";
		accounts = yield pool.query(sql, [this.session.user.name]);
	} else {
		var sql = "select * from Manager where name = ? order by createTime desc ";
		accounts = yield pool.query(sql, [this.session.user.name]);
	}
	// 如果是子超级管理员，只能看到普通用户
	if (this.session.user.name != 'admin' && accounts.length > 0) {
		for (var i = accounts.length - 1; i >= 0; i--) {
			if (accounts[i].access.indexOf('user') != -1) {

				accounts.splice(i, 1);
			}

		};
	};

	if ( accounts.length > 0) {

		for (var i = accounts.length - 1; i >= 0; i--) {
		    	accounts[i].password = util.decode(accounts[i].password, config.decode);
		};
	};

	var password = (yield pool.query("select password from Manager where name = ? ", [this.session.user.name]))[0].password;
	password = util.decode(password, config.decode);
	var userinfo = this.session.user;
	userinfo.password = password;
	console.log(userinfo,'----userinfo');
	yield this.render('/account/index', {
		accounts: accounts,
		userinfo: userinfo,
		userAccess: this.session.user.access.indexOf('user') != -1,
		json: {
			"user": '员工管理权限',
			"activity": '活动权限',
			"transfer": '充值权限'
		}
	})
}

exports.post = function*() {
	var data = this.request.body;
	var sql = "insert into Manager set ?";
	try {
		yield pool.query(sql, {
			name: data.name,
			password: util.encode(data.pwdadd, config.decode),
			access: data.access.toString(),
			phone: data.phone,
			createTime: new Date()
		})
		this.response.body = '创建成功';
	} catch (e) {
		console.log(e);
		this.response.body = '创建失败';
	}
}