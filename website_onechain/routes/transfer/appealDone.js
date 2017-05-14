'use strict'
var chain = global.chain;
var config = require('../../config');
const wrapper = require('co-mysql');
const pool = wrapper(global.pool);
const uuid = require('node-uuid');

exports.post = function*() {
	var args = this.request.body;

	console.log('args1---',args);
	args.Result = parseInt(args.Result);
	args.UserId = config.userName;
	args.Extras = this.session.user.name;
	args.ModifyTime = new Date().getTime();
	args = JSON.stringify(args);
	console.log('args',args)
	var result = yield chain.invoke('zxCoinWithdrawAppealDone', ['', args]);
	console.log('申诉处理结果:',result);
	this.response.body = result.code;
	// if (result.code == 0) {
	// 	this.response.body = '申诉处理成功！'
	// } else {
	// 	this.response.body = '申诉处理失败！'
	// }

}