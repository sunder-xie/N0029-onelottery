'use strict'
var chain = global.chain;
var config = require('../../config');
const wrapper = require('co-mysql');
const pool = wrapper(global.pool);
const uuid = require('node-uuid');

exports.get = function*() {
	if (this.session.user.access.indexOf('transfer') == -1) {
		yield this.render('/asset/index', {
			rules: rules,
			json: ['一', '二', '三', '四', '五', '六', '七', '八', '九', '十'],
			data: data,
			list : list,
			date: new Date().Format('yyyy-MM-dd hh:mm')
		});
		return;
	};
	yield this.render('transfer/index');
}

exports.post = function*() {
	var args = this.request.body;
	args.ModifyTime = new Date().getTime();
	args.UserId = config.userName;
	args.Extras = this.session.user.name;
	args = JSON.stringify(args);
	console.log('args',args)
	var result = yield chain.invoke('zxCoinWithdrawRemitSucces', ['', args]);
	console.log('打款:',result);
	this.response.body = result.code;
	// if (result.code == 0) {
	// 	this.response.body = '打款成功！'
	// } else {
	// 	this.response.body = '打款失败！'
	// }

}