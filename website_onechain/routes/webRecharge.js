'use strict'
var config = require('../config');
const wrapper = require('co-mysql');
const pool = wrapper(global.pool);


var masterSecret = "1e00bf0a-8278-444d-9739-d58516b2c87f";
var appSecret = "5f74570f-5359-4e89-b300-839541db9307";

var sign = require('crypto');


exports.post = function*() {
	var data = this.request.body;
	var str = data.app_id + data.transaction_id + data.transaction_type +
		data.channel_type + data.transaction_fee + masterSecret;
	var signStr = sign.createHash('md5').update(str, 'utf8').digest("hex");
	console.log('signStr', signStr, data.signature);
	if (data.signature !== signStr) {
		this.response.body = 'fail';
		return;
	};
	var optional = data.optional;
	var date = new Date().getTime();
	var mastDuration = 1000 * 60 * 3;
	if ((date - optional.timestamp) > mastDuration) {
		this.response.body = 'fail';
		return;
	}

	var str = optional.walletAddr + optional.timestamp + appSecret;
	console.log(optional.walletAddr, optional.timestamp, appSecret)
	var signOptional = sign.createHash('md5').update(str, 'utf8').digest("hex");
	console.log('signOptional', signOptional, data.transaction_id)
	if (signOptional !== data.transaction_id) {
		this.response.body = 'fail';
		return;
	};
	var sql = "insert into webRecharge set ?";
	console.log({
		transcationId: data.transaction_id,
		channelType: data.channel_type,
		transcationFee: data.transaction_fee,
		messageDetail: JSON.stringify(data.message_detail),
		time: data.timestamp,
		userHash: data.optional.walletAddr
	});
	try {
		yield pool.query(sql, {
			transcationId: data.transaction_id,
			channelType: data.channel_type,
			transcationFee: data.transaction_fee,
			messageDetail: JSON.stringify(data.message_detail),
			time: data.timestamp,
			userHash: data.optional.walletAddr
		});

		var args = {
			"userCertTo": "",
			"nameTo": "",
			"amount": 0,
			"type": 0,
			"userId": "",
			"fee": 100,
			"time": 1
		};
		args.type = 0;
		args.userId = config.userName;
		args.fee = config.feeOther * 10000;
		args.amount = (data.transaction_fee / 100) * 100 * 10000
		args.time = new Date().getTime();
		args.userCertTo = optional.walletAddr;
		var time = args.time;
		args = JSON.stringify(args);
		console.log(args)
		var result = yield chain.invoke('zxCoinTransfer', ['', args]);
		console.log('充值成功')
		this.response.body = 'success';
	} catch (e) {
		console.log(e);
		return;
	}

}