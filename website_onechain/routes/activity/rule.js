'use strict'
const wrapper = require('co-mysql');
const pool = wrapper(global.pool);
const chain = global.chain;

exports.get = function*() {
	var sql = 'select * from Rule';
	var rules = yield pool.query(sql);
	yield this.render('activity/rule', {
		rules: rules,
		json: ['一', '二', '三', '四', '五', '六', '七', '八', '九', '十']
	})
}
exports.post = function*() {
	var args = this.request.body;
	args.hide = args.ruleStatus == '0' ? true : false;
	args.percentage = parseInt(args.percentage);
	args.createTime = new Date().getTime();
	args = JSON.stringify(args);
	if (this.request.query.method == 'edit') {
		try {
			var result = yield chain.invoke('oneLotteryPrizeRuleEdit', ['', args]);
			console.log(result)

			if (result.code == 0) {
				this.response.body = '修改规则成功！'
			} else {
				this.response.body = '修改规则失败！'
			}
		} catch (e) {
			console.log(e);
			this.response.body = '修改规则失败！'
		}
		return;
	}
	try {
		var result = yield chain.invoke('oneLotteryPrizeRuleAdd', ['', args]);

		if (result.code == 0) {
			
			this.response.body = '添加规则成功！'
		} else {
			this.response.body = '添加规则失败！'
		}
	} catch (e) {
		console.log(e);
		this.response.body = '添加规则失败！'
	}
}