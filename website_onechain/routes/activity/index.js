'use strict'
const wrapper = require('co-mysql');
const pool = wrapper(global.pool);
const config = require('../../config');
const uuid = require('node-uuid');
const util = require('../../lib/util');
exports.get = function*() {
	if (this.session.user.access.indexOf('activity') == -1) {
		yield this.render('/asset/index', {
			rules: rules,
			json: ['一', '二', '三', '四', '五', '六', '七', '八', '九', '十'],
			data: data,
			list : list,
			date: new Date().Format('yyyy-MM-dd hh:mm')
		});
		return;
	};
	var sql = 'select * from Rule';
	var rules = yield pool.query(sql);
	var now = new Date();
	var ary = [];

	sql = "select count(*) count, type  from " +
		"(select  publisherHash ,(case  when startTime <= ? and closeTime >= ? and curBetAmount != total then '进行中的活动' " +
		" when startTime > ?  then '未开始的活动' " +
		" when closeTime < ? and curBetAmount < total then '失败的活动' " +
		" when curBetAmount = total  then '成功的活动' " +
		"  end) type from OneLottery ) t ";
	var sqlEnd = ' group by type';

	//官方活动（状态）
	ary.push(pool.query(sql + " where publisherHash = ?" + sqlEnd, [now, now, now, now,config.args.userHash]));
	//个人活动（状态）
	ary.push(pool.query(sql + " where publisherHash != ?" + sqlEnd, [now, now, now, now,config.args.userHash]));
	//全部活动（状态）
	ary.push(pool.query(sql + sqlEnd, [now, now, now, now]));

	sql = "select count(*) count ,percentage from (select publisherHash,a.lotteryID,b.percentage,a.creater from OneLottery a  join Rule b where a.ruleId = b.id ) t ";
	sqlEnd = "  group by percentage";
	//官方活动（规则类型)
	ary.push(pool.query(sql + " where publisherHash = ?" + sqlEnd, [config.args.userHash]));
	//个人活动（规则类型)
	ary.push(pool.query(sql + " where publisherHash != ?" + sqlEnd, [config.args.userHash]));
	//全部活动（规则类型）
	ary.push(pool.query(sql + sqlEnd, [now, now, now, now]));

	var data = yield ary;
	data = packedData(data, rules);
	var list = ['未开始的活动', '进行中的活动', '失败的活动', '成功的活动'];
	for (var i = 0; i < rules.length; i++) {
		list.push('获奖者得'+rules[i].percentage+'%');
	}
	yield this.render('activity/index', {
		rules: rules,
		json: ['一', '二', '三', '四', '五', '六', '七', '八', '九', '十'],
		data: data,
		list : list,
		date: new Date().Format('yyyy-MM-dd hh:mm')
	})
}

exports.post = function*() {
	var args = this.request.body;
	args.cost = parseInt(args.cost) * 10000;
	args.total = parseInt(args.total) * 10000;
	args.fee = config.fee * 10000;
	args.ruleType = 'PrizeRule';
	args.maxAttendeeCnt = parseInt(args.maxAttendeeCnt);
	args.minAttendeeCnt = parseInt(args.maxAttendeeCnt);
	args.createTime = new Date().getTime();
	args.publisherName = config.userName;
	// args.publisherName = this.session.user.name;
	// args.publisherHash = pool.query("select * from UserInfo where name = ? ", [this.session.user.name])[0];
	var startTime = new Date(args.startTime);
	args.startTime = startTime.getTime();
	args.pictureIndex = parseInt(args.pictureIndex);
	var closeTime = startTime.setDate(startTime.getDate() + parseInt(args.last));
	args.closeTime = closeTime;

	if (this.request.query.id) {
		args.updateTime = new Date().getTime();
		args.fee = config.feeOther * 10000;
		var insertData = args;
		args.txnID = this.request.query.id;
		args = JSON.stringify(args);
		console.log("session1 "+this.session.user.name);
		console.log("args "+JSON.stringify(args))
		
		try {
			var result = yield chain.invoke('oneLotteryEdit', ['', args]);
			console.log(result)

			if (result.code == 0) {
				util.insertTranscation({
					txId: result.data.newTxnID,
					myHash: config.args.userHash,
					relatedHash: args.txnID,
					amount: 0,
					fee: config.feeOther * 10000,
					type: 6,
					time: new Date(),
					operator: this.session.user.name
				});

				this.response.body = '修改活动成功！';
			} else {
				this.response.body = '修改活动失败！'
			}
		} catch (e) {
			console.log(e);
			this.response.body = '修改活动失败！'
		}
		return;
	}
	var insertData = args;

	args = JSON.stringify(args);
	console.log('创建活动:',this.session.user.name,args);
	try {
		var result = yield chain.invoke('oneLotteryAdd', ['', args]);
		console.log(result)

		if (result.code == 0) {
			this.response.body = '创建活动成功！';
			util.insertTranscation({
					txId: result.data.txnID,
					myHash: config.args.userHash,
					relatedHash: result.data.txnID,
					amount: 0,
					fee: config.fee * 10000,
					type: 5,
					time: new Date(),
					operator: this.session.user.name
				});
		} else {
			console.log('创建活动失败！')
			this.response.body = '创建活动失败！'
		}
	} catch (e) {
		console.log(e);
		this.response.body = '创建活动失败！'
	}
}

//把数据库查找的数据组装成前台页面需要的数据
function packedData(data,rules) {
	var finalData = [];
	var ary = ['未开始的活动', '进行中的活动', '失败的活动', '成功的活动'];
	var officail = [];
	var personal = [];
	var all = [];
	// 活动(状态)
	for (var i = 0; i < ary.length; i++) {
		//官方活动
		var isHave = false;
		for (var j = 0; j < data[0].length; j++) {
			if(data[0][j].type == ary[i]) {
				officail.push(data[0][j].count);
				isHave = true;
			}
		}
		if (!isHave) {
			officail.push(0);
		};
		//个人活动
		var isHave = false;
		for (var j = 0; j < data[1].length; j++) {
			if(data[1][j].type == ary[i]) {
				personal.push(data[1][j].count);
				isHave = true;
			}
		}
		if (!isHave) {
			personal.push(0);
		};
		//全部活动
		var isHave = false;
		for (var j = 0; j < data[2].length; j++) {
			if(data[2][j].type == ary[i]) {
				all.push(data[2][j].count);
				isHave = true;
			}
		}
		if (!isHave) {
			all.push(0);
		};
	};
	//活动(规则)
	for (var i = 0; i < rules.length; i++) {
		//官方活动
		var isHave = false;
		for (var j = 0; j < data[3].length; j++) {
			if (data[3][j].percentage == rules[i].percentage) {
				officail.push(data[3][j].count);
				isHave = true;
			}
		}
		if (!isHave) {
			officail.push(0);
		}
		//个人活动
		var isHave = false;
		for (var j = 0; j < data[4].length; j++) {
			if (data[4][j].percentage == rules[i].percentage) {
				personal.push(data[4][j].count);
				isHave = true;
			}
		}
		if (!isHave) {
			personal.push(0);
		}
		//全部活动
		var isHave = false;
		for (var j = 0; j < data[5].length; j++) {
			if (data[5][j].percentage == rules[i].percentage) {
				all.push(data[5][j].count);
				isHave = true;
			}
		}
		if (!isHave) {
			all.push(0);
		}
	};
	console.log(officail,personal,all)
	return [officail, personal, all]
}