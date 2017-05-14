var pool = require('./mysqlPool.js');
var async = require('async');
var decode = require('./grpc').decode;
var uuidV4 = require('uuid/v4');
var config = require('../config');
var parseFun = {
	oneLotteryEdit: oneLotteryEdit,
	zxCoinTransfer: zxCoinTransfer,
	oneLotteryAdd: oneLotteryAdd,
	oneLotteryDelete: oneLotteryDelete,
	oneLotteryBet: oneLotteryBet,
	oneLotteryBetOver: oneLotteryBetOver,
	oneLotteryPrizeRuleAdd: oneLotteryPrizeRuleAdd,
	oneLotteryPrizeRuleEdit: oneLotteryPrizeRuleEdit,
	oneLotteryRefund: oneLotteryRefund,
    //退款
    zxCoinWithdraw: zxCoinWithdraw,
	zxCoinWithdrawFail: zxCoinWithdrawFail,
	zxCoinWithdrawRemitSucces: zxCoinWithdrawRemitSucces,
	zxCoinWithdrawConfirm: zxCoinWithdrawConfirm,
	zxCoinWithdrawAppeal: zxCoinWithdrawAppeal,
	zxCoinWithdrawAppealDone: zxCoinWithdrawAppealDone
}

function parse(block, blockNumber, callback) {
	var txs = block.transactions;
	var results = block.nonHashData.chaincodeEvents;
	console.log(txs.length, blockNumber);
	var parseDatas = [];
	for (var i = 0; i < txs.length; i++) {
		var data = [txs[i], results[i]];
		parseDatas.push(data);
	}

	async.map(parseDatas, function(item, cb) {
		parseTx(item, function() {
			cb();
		})
	}, function(err) {
		var value = {
			blockNum: blockNumber,
			time: new Date()
		};
		pool.query('update SyncState SET ? where id = 1', value, function(err, rs) {
			if (err) {
				console.log('更新同步表失败', err);
			} else {
				callback();
			}
		})
	})

}

function parseTx(tx, callback) {
	try {
		if (tx[0].type.toString() === "CHAINCODE_DEPLOY") {
			callback(null);
			return;
		};
		var result = JSON.parse(tx[1].payload.toString());
		var fnc = tx[1].eventName;

		if (fnc === 'oneLotteryBet' || fnc === 'ConsensusOver') {
			fnc = 'oneLotteryBet';
		}
		if (fnc === 'oneLotteryBetOver' || fnc === 'BetOver') {
			fnc = 'oneLotteryBetOver';
		}
		console.log(fnc);
		if (parseFun[fnc] && result.code == 0) {
			var data = decode(tx[0].payload);
			var base64_string = new Buffer(tx[0].cert).toString('base64');
			var userHash = getSha(base64_string);
			// var userHash = getSha(data[0].buffer.slice(data[0].offset, data[0].limit).toString());
			var query = JSON.parse(data[1].buffer.slice(data[1].offset, data[1].limit).toString());

			parseFun[fnc](query, result, tx[0].txid, userHash, function() {

			});
		} else {
			callback(null);
			return;
		}
	} catch (e) {
		console.log(e);
	}

	callback();
}

function oneLotteryRefund(query, result, txId, userHash, callback) {
	/*
	query {
		lotteryID: 'd460db6f-4b36-46a4-ae88-3a53f3a9345a',
		currentTime: 1491473883811
	}
	错误的: 122
	result {
		code: 0,
		message: '',
		data: 'eyJjb2RlIjowLCJtZXNzYWdlIjoiU3VjY2VzcyIsImRhdGEiOnsiYXJyYXkiOlt7Im93bmVyIjoiMmYwYTA0MzZiMjlmODdkMzFhZDg0N2Q2ZGY3Mjc1ODMxN2EwMGVlNDNjYjEzYmYyNWM4MDRkYjMiLCJvcHBpc2l0ZSI6IjJmMGEwNDM2YjI5Zjg3ZDMxYWQ4NDdkNmRmNzI3NTgzMTdhMDBlZTQzY2IxM2JmMjVjODA0ZGIzIiwiYW1vdW50IjoyMDAwMH1dLCJsb3R0ZXJ5SUQiOiJkNDYwZGI2Zi00YjM2LTQ2YTQtYWU4OC0zYTUzZjNhOTM0NWEiLCJjdXJyZW50VGltZSI6MTQ5MTQ3Mzg4MzgxMX19'
	}
	正确的: 434，394
	result {
		code: 0,
		message: 'Success',
		data: {
			array: [{
				owner: 'c5d0300f616b35f676ad2066ac417828b8f9d699075ef9b450d43f2f',
				oppisite: '6c5a14b304f4ec360a114240ccfff2c54eb75972b4c116f95c8d3b8f',
				amount: 50000
			}],
			lotteryID: '75864736-edc9-456a-b409-b76a612d85a1',
			currentTime: 1491557044000
		}
	}
	txId 65f65e86-7f25-4172-b337-fc0dbfce8e0d
	userHash c9064f82288f5755eedd476811919f51f89d363a901ef4b8d4a58ceb
	*/

	// console.log('oneLotteryRefund query',query);
	// console.log('oneLotteryRefund txId',txId);
	// console.log('oneLotteryRefund userHash',userHash);

	// console.log('oneLotteryRefund result',result);
	// console.log('oneLotteryRefund result.data.array=', result.data.array)

	//更新活动状态为退款
	var sql = "update OneLottery set ? where lotteryID = ?";
	pool.query(sql, [{
		state: 4
	}, query.lotteryID], function(err, data) {
		if (err) {
			console.log('活动退款', err);
		} else {
			console.log('活动退款--------------');
		}
	});
	//建议用result.data.array内的内容来退参与用户的钱，而不是用本地投注列表来自己统计。 
	//不过要结合客户端确认一下array是不是已经将投注者的分批投注已经组合到一起了，否则就自己做一个数组去好了
	//查询该活动的所有投注者
	var sql1 = "select sum(betCost) amount,attendeeName from OneLotteryBet where lotteryID = ? group by attendeeName";
	pool.query(sql1, [result.data.lotteryID], function(err, data1) {
		if (err) {
			console.log('统计每个用户投注花费费用', err);
		}

		var total = 0;
		async.map(data1, function(item, cb) {
			total += item.amount;
			//插入交易记录,向多个用户退款时，这个交易ID添加relatedHash前几个字符
			var sql2 = "select attendeeHash from OneLotteryBet where attendeeName = ? limit 1";
			pool.query(sql2, [item.attendeeName], function(err, data2) {
				if (err) {
					console.log('查询投注表用户hash', err);
				}
				data2=data2[0];

				insertTranscation({
					txId: (txId + '=' + data2.attendeeHash.substr(0, 5)),
					myHash: data2.attendeeHash,
					myName: item.attendeeName,
					relatedHash: query.lotteryID,
					amount: item.amount,
					fee: 0,
					type: 3,
					time: new Date()//是不是可以采用result或者query中的时间呢？
				});
				var sql3 = "update UserInfo set balance = balance + " + item.amount + " where userId = ?";
				pool.query(sql3, [data2.attendeeHash], function(err) {
					if (err) {
						console.log('更新用户余额',err)
					};
					cb(err);
				})
			})
		}, function(err) {
			if (err) {
				console.log('活动退款更新投注者余额', err);
			} else {
				console.log('活动退款更新投注者余额------------');
				//更新活动发布者冻结金额
				var sql4 = "select publisherHash from OneLottery where lotteryID = ?";
				// callback();
				// return;
				pool.query(sql4, [query.lotteryID], function(err, pubHash) {
					if(err){
						console.log('查询退款活动',err);
					} else if (pubHash.length > 0) {
						//直接将reserved清零，不再用上面的total的话，只针对用户可以创建一个进行中的活动
						//如果可以建立多个活动，还是用total好些
						var sql5 = "update UserInfo set reserved = reserved - " + total + " where userId = ?";
						// var sql = "update UserInfo set reserved = 0 where userId = ?";

						pool.query(sql5, [pubHash[0].publisherHash], function(err) {
							if (err) {
								console.log('更新活动发布者冻结金额', err);
							} else {
								console.log('更新活动发布者冻结金额---------------');
							}
						})
					}
				})
			};
		})
		
	})
	
	callback();
}

function oneLotteryDelete(query, result, txId, userHash, callback) {
	/*
	query {
		txnID: '823425f3-ca45-43c4-90d3-fafe83e18a12',
		updateTime: 1491465242
	}
	result {
		code: 0,
		message: '',
		data: '823425f3-ca45-43c4-90d3-fafe83e18a12'
	}
	txId 67b23826-e202-4119-bff2-77cc6531f304
	userHash 39325 fac23a874399793aed0060806c47083469949c02f56c5f7e417
	*/
	// 返回的时间还是秒，不是毫秒
	var sql = "delete from OneLottery where lotteryID = ?";
	pool.query(sql, [result.data], function(err) {
		if (err) {
			console.log('删除活动失败',err);
		} else {
			console.log('删除活动成功')
		}
		callback();
	})

}

function oneLotteryPrizeRuleAdd(query, result, txId, userHash, callback) {
	/*
	query {
		percentage: 100,
		ruleId: '',
		ruleStatus: '1',
		hide: false,
		createTime: 1491460989343
	}
	result {
		code: 0,
		message: '',
		data: {
			ruleID: '93a76d9c-75fa-44d3-8312-b9e5a0ddc66d',
			percentage: 100,
			hide: false,
			createTime: 1491460989343
		}
	}
	txId 93a76d9c-75fa-44d3-8312-b9e5a0ddc66d
	userHash 2 af5ae007e3b57c0b1bd75bb96234e55f009bac5fb8e7515c3ad9467
	*/
	var sql = "insert into Rule set ?";
	pool.query(sql, {
		id: result.data.ruleID,
		percentage: result.data.percentage,
		status: result.data.hide == true ? 0 : 1
	}, function(err) {
		if (err) {
			console.log('规则添加失败', err);
		} else {
			console.log('规则添加成功');
			//callback需要在成功时候才执行吗？
			callback();
		}
	})
}

function oneLotteryPrizeRuleEdit(query, result, txId, userHash, callback) {
	var sql = "update  Rule set status = ? where id = ?";
	pool.query(sql, [result.data.hide == true ? 0 : 1, result.data.ruleID], function(err) {
		if (err) {
			console.log('规则修改成功', err);
		} else {
			console.log('规则修改成功');
			//callback需要在成功时候才执行吗？
			callback();
		}
	})
}

function oneLotteryBetOver(query, result, txId, userHash, callback) {
	/*
	query {
		lotteryID: '74fa0ac1-5269-4c4b-843a-d4a1ee882a68',
		currentTime: 1491463220349
	}
	result {
		code: 0,
		message: '',
		data: {
			txnID: '31e823c5-fbb0-4253-8ed8-f2a4c00274e7',
			attendee: '2f0a0436b29f87d31ad847d6df72758317a00ee43cb13bf25c804db3',
			attendeeName: 'Nn',
			numbers: '10000003',
			amount: 90000,
			CreateTime: 1491463220349,
			lotteryID: '74fa0ac1-5269-4c4b-843a-d4a1ee882a68'
		}
	}
	txId def92733-e021-4c81-bc65-68d964deddc2
	userHash fc9df9975fbd3d04c469e1e5867301cab37a85be2b92a55ac9298f6d
	*/
	var time = new Date(result.data.CreateTime);
	//更新活动状态及中奖号码
	var sql = "update OneLottery set ? where lotteryID = ?";
	pool.query(sql, [{
		state: 3,
		rewardNumbers: result.data.numbers,
		lastCloseTime: time
	}, result.data.lotteryID], function(err, data) {
		if (err) {
			console.log('开奖活动更新', err);
		} else {
			console.log('开奖活动更新--------------');

		}
	});
	//更新相关账户金额
	//1.查询活动规则ID及活动总金额
	sql = "select ruleId, total, publisherHash, publisherName from OneLottery where lotteryID = ?";
	pool.query(sql, [result.data.lotteryID], function(err, onelot) {
		if (err) {
			console.log('查询活动失败', err);
		}
		onelot = onelot[0];
		if (onelot) {
			//2.查询规则
			sql = "select percentage from Rule where id = ?";
			pool.query(sql, [onelot.ruleId], function(err, _rule) {
				if (err) {
					console.log('查询规则失败', err);
				}
				_rule = _rule[0];
				//更新中奖用户余额
				sql = "update UserInfo set balance = balance + ? where userId = ?";
				pool.query(sql, [onelot.total * _rule.percentage / 100, result.data.attendee], function(err) {
					if (err) {
						console.log('更新中奖用户余额', err);
					} else {
						console.log('更新中奖用户余额----------')

					}
				});
				//更新活动发布者用户余额
				sql = "update UserInfo set balance = balance + ?,reserved = reserved - ? where userId = ?";
				pool.query(sql, [onelot.total * (100 - _rule.percentage) / 100, onelot.total, onelot.publisherHash], function(err) {
					if (err) {
						console.log('更新活动发布者用户余额', err);
					} else {
						console.log('更新活动发布者用户余额----------')

					}
				});
				//中奖者交易记录
				// 用uuid，不用交易ID呢。。
				insertTranscation({
					txId: uuidV4(),
					myHash: result.data.attendee,
					myName: result.data.attendeeName,
					relatedHash: result.data.lotteryID,
					amount: onelot.total * _rule.percentage / 100,
					fee: 0,
					type: 4,
					time: time
				});
				//活动发布者交易记录
				insertTranscation({
					txId: uuidV4(),
					myHash: onelot.publisherHash,
					myName: onelot.publisherName,
					relatedHash: result.data.lotteryID,
					amount: onelot.total * (100 - _rule.percentage) / 100,
					fee: 0,
					type: 7,
					time: time
				})
				callback();
			});
		} else {
			console.log('没有查询到开奖的活动');
			callback();
		}

	});
}

function oneLotteryEdit(query, result, txId, userHash, callback) {
	/*
	query {
		txnID: '2cc32765-11b7-41a1-83df-5934650f99b1',
		name: '修改',
		fee: 100,
		publisherName: 'zy',
		ruleType: 'PrizeRule',
		pictureIndex: 3,
		ruleId: 'fde486ab-a659-4155-92c9-4cfae17295e8',
		CreateTime: 1491387699300
		updateTime: 1491466088823,
		startTime: 1491638820000,
		closeTime: 1491725220000,
		minAttendeeCnt: 100,
		maxAttendeeCnt: 100,
		cost: 10000,
		Total: 1000000,
		description: ''
	}
	result {
		code: 0,
		message: '',
		data: {
			txnID: '2cc32765-11b7-41a1-83df-5934650f99b1',
			newTxnID: 'cb7d48e9-a2aa-4c05-bb43-493d0b4eaef5',
			name: '修改',
			version: 1,
			publisherName: 'zy',
			publisherHash: '08e94b4867202d6b8b8e7e2b42c39f8bebe2141e98a38c2f0643b244'
		}
	}
	txId cb7d48e9-a2aa-4c05-bb43-493d0b4eaef5
	userHash ba81f7997008e3f8bf79d66dc3f5e62edd62509aff3a318dd47b4722
	*/
	console.log('修改活动--------------');
	if (query.createTime) {
		query.createTime = new Date(query.createTime);
	} else if (query.CreateTime) {
		query.CreateTime = new Date(query.CreateTime);
	}
	query.startTime = new Date(query.startTime);
	query.closeTime = new Date(query.closeTime);
	query.updateTime = new Date(query.updateTime);
	query.lotteryID = query.txnID;
	var fee = query.fee
	delete query.last;
	delete query.txnID;
	delete query.fee;
	//有可能是修改后，这个lotteryID就是 result.data.txnID 错误 ？ or query.txnID才对呢
	var sql = "update OneLottery set ? where lotteryID = ?";
	pool.query(sql, [query, result.data.txnID], function(err, data) {
		if (err) {

			console.log('修改活动', err);
		} else {
			console.log('修改活动入库--------------');

		}
	})
	insertTranscation({
		txId: txId,
		myHash: result.data.publisherHash,
		myName: result.data.publisherName,
		relatedHash: result.data.txnID,
		amount: 0,
		fee: fee,
		type: 6,
		time: query.updateTime
	});

	//扣除修改活动费用
	next({
		fee: parseInt(fee)
	}, {
		data: {
			owner: result.data.publisherHash
		}
	});
	callback();

}

function oneLotteryAdd(query, result, txId, userHash, callback) {
	/*
	query {
		name: 'gh',
		fee: 10000,
		publisherName: 'cytf',
		ruleType: 'PrizeRule',
		ruleId: 'fde486ab-a659-4155-92c9-4cfae17295e8',
		pictureIndex: 2,
		createTime: 1491461130565,
		startTime: 1491461130566,
		closeTime: 1491547530566,
		minAttendeeCnt: 1,
		maxAttendeeCnt: 1,
		cost: 10000,
		Total: 10000,
		description: ''
	}
	result {
		code: 0,
		message: '',
		data: {
			txnID: '015f31f3-7d37-4db3-a4dd-55f02c34ec9e',
			newTxnID: '',
			name: 'gh',
			version: 0,
			publisherName: 'cytf',
			publisherHash: 'e39fc090e72d796a088fb5831d93efcf1ea4778755b415153fff90c9'
		}
	}
	txId 015f31f3-7d37-4db3-a4dd-55f02c34ec9e
	userHash 816ab1e3f2c06b1589d9f0e39dbdc3a8e0a16802d1aceb443b8d9459
	*/
	//重要：返回了version，但是creater是需要web来填充了。不过，如果是只限于web当时的操作，如果是倒数据，那就没creater了。
	// PS: 虽然query返回了Total,但是mysql对列名大小写不敏感
	console.log('添加活动--------------');
	var fee = query.fee;
	query.startTime = new Date(query.startTime);
	query.createTime = new Date(query.createTime);
	query.closeTime = new Date(query.closeTime);
	query.lotteryID = result.data.txnID;
	query.publisherHash = result.data.publisherHash;
	delete query.updateTime
	delete query.last;
	delete query.txnID;
	delete query.fee;
	var sql = "insert into OneLottery set ?";
	pool.query(sql, [query], function(err, data) {
		if (err) {
			console.log(query.name,'活动入库', err)
		} else {
			console.log(query.name,'活动入库--------------');

		}
	})
	insertTranscation({
		txId  		: txId,
		myHash 		: result.data.publisherHash,
		myName 		: result.data.publisherName,
		relatedHash : result.data.txnID,
		amount 		: 0,
		fee 		: fee,
		type 		: 5,
		time 		: query.createTime
	});
	updateUserInfo(query.publisherHash, {
		name: query.publisherName
	});
	//重要：根据这个name去更新下投注表的hash，然后后面投注表入库的时候就先查用户表，找到hash
	updateBetUserName(query.publisherName, {
		attendeeHash: result.data.publisherHash
	});
	//重要：根据这个name去更新下交易表的hash，然后后面投注表入库的时候就先查用户表，找到hash
	updateTranUserName(query.publisherName, {
		myHash: result.data.publisherHash
	});

	//扣除发布活动费用
	next({
		fee: fee
	}, {
		data: {
			owner: result.data.publisherHash
		}
	});

	//重要：合并未激活之前的赊账账户，和正式账户的余额。
	mergeForkUser(query.publisherName, result.data.publisherHash);
	callback();

}

function zxCoinTransfer(query, result, txId, userHash, callback) {
	// console.log("query",query)
	// console.log("result",result)
	// console.log("txId",txId)
	// console.log("userHash",userHash)
	/**
	query {
		userCertTo: 'e39fc090e72d796a088fb5831d93efcf1ea4778755b415153fff90c9',
		nameTo: '',
		amount: 10000,
		type: 0,
		userId: 'one_chain_admin',
		fee: 100,
		time: 1491460762193
	}
	result {
		code: 0,
		message: 'Success',
		data: {
			owner: 'c5d0300f616b35f676ad2066ac417828b8f9d699075ef9b450d43f2f',
			ownUserId: 'one_chain_admin',
			oppisite: 'e39fc090e72d796a088fb5831d93efcf1ea4778755b415153fff90c9',
			oppisiteUserId: '',
			Extras: '',
			amount: 10000,
			fee: 100
		}
	}
	txId ef7a7fef-bdc1-4c58-80cc-2f04a7d2151b
	userHash f97e7d742fafd79fb7c6957f58830419421ed86ef61bd215a04db905
	*/
	// if (query.userId != 'one_chain_admin')
	var operator='';
	if (query.Extras) {
		operator = query.Extras;
	};

	var type = (result.data.owner === config.args.userHash) ? 1 : 8;
	insertTranscation({
		txId 		: txId,
		myHash 		: result.data.owner,
		myName 		: result.data.ownUserId,
		relatedHash : result.data.oppisite,
		amount 		: result.data.amount,
		fee 		: result.data.fee,
		type 		: type,
		operator 	: operator,
		time 		: new Date(query.time)
	});
	//扣除转账费用
	next(query, result);
	//扣除原始账户balance
	var sql = "update UserInfo set balance = balance - " + query.amount + "  where userId = ?";

	pool.query(sql, [result.data.owner], function(err) {
		if (err) {
			console.log('扣除原始账户金额成功', err)
		} else {
			console.log('扣除原始账户金额成功')
		}
	});
	//给对方账户充钱
	//先检查此账户是否存在,如果不存在设置激活时间
	var sql = "select * from UserInfo where userId = ? ";
	pool.query(sql, [result.data.oppisite], function(err, data) {
		if (data.length < 1) {
			sql = "insert into UserInfo set ?"
			pool.query(sql, {
				userId: result.data.oppisite,
				balance: query.amount,
				activationTime: new Date(query.time)
			}, function(err) {
				if (err) {
					console.log('给对方账户充钱成功并设置激活时间', err)
				} else {
					console.log('给对方账户充钱成功并设置激活时间')
				}
			})
		} else {
			sql = "update UserInfo set balance = balance + " + query.amount + "  where userId = ?";
			pool.query(sql, [result.data.oppisite], function(err) {
				if (err) {
					console.log('给对方账户充钱成功', err)
				} else {
					console.log('给对方账户充钱成功')
				}
			})

		}

	});
	callback();

}

// 状态 1申请中 2已打款 3已确认 4提现失败（申请被拒） 5客户申诉 6确认到账（申诉驳回）7提现失败（退回众享币） 8后台自动确认

function zxCoinWithdraw(query, result, txId, userHash, callback) {
	console.log('提现申请处理---');

	/*
	query: {
		AccountInfo: '{"BankName":"啦啦啦","AccountName":"如家快捷","AccountId":"12445"}',
		UserId: 'yhhsnnhh',
		Amount: 10000,
		ModifyTime: 1491825993
	}
	result: {
		code: 0,
		message: 'Success',
		data: null
	}

	*/

	// console.log("query",query)
	// console.log("result",result)
	// console.log("txId",txId)
	// console.log("userHash",userHash)

	updateUserInfo(userHash, {
		name: query.UserId
	});

	//扣除提现费用
	nextByName(query.Amount, query.UserId, "提现申请");
	var sql = "select userId from UserInfo where name = ?";
	pool.query(sql, [query.UserId], function(err, data) {
		if (err) {
			console.log('查找用户失败', err)
		} else {
			if (data.length < 1) {
				callback();
				return;
			};
			query=checkTxTime(query);
		   	// console.log('提现-date-----------',query.ModifyTime)
		   	query.AccountInfo = JSON.parse(query.AccountInfo);
			var sql = "insert into Withdraw set ?";

			pool.query(sql, {
		 		id            : txId,
		 		amounts 	  : query.Amount,
		 	 	userId 		  : data[0].userId,
		 	 	name          : query.UserId,
		  		bank          : query.AccountInfo.BankName,
		  		account       : query.AccountInfo.AccountId,
		  		accountName   : query.AccountInfo.AccountName,
		  		state         : 1,
		  		createTime    : query.ModifyTime,	  		
		  		updateTime    : query.ModifyTime

			}, function(err) {
				if (err) {
					console.log('提现申请处理失败', err);
				} else {
					console.log('提现申请处理成功');
					//设置活动创建者冻结账户金额
					var sql = "update UserInfo set reserved = reserved + " + query.Amount + "  where userId = ?";
					pool.query(sql, [query.UserId], function(err) {
						if (err) {
							console.log('提现申请冻结账户金额失败——————————', err);
						} else {
							console.log("提现申请冻结账户金额成功——————————")
						}
			        })
			        
					callback();

				}
			})
		}
	})
}

function zxCoinWithdrawFail(query, result, txId, userHash, callback) {
	console.log('提现失败处理---');
	// console.log("query", query)
	// console.log("result", result)
	// console.log("txId", txId)
	// console.log("userHash", userHash)
	query.ModifyTime === checkTxTime(query);
	
	var operator='';
	if (query.Extras) {
		operator = query.Extras;
	};

	var sql = "update Withdraw set ? where id = ?";
		pool.query(sql, [{
		remark   	 : query.Remark,
		state     	 : 4,
		payAdmin 	: operator,
		updateTime 	 :query.ModifyTime
	}, query.TxId], function(err, data) {
		if (err) {
			console.log('提现失败处理失败', err);
			callback();
		} else {
			console.log("提现失败处理成功----------")
			// balance 增加
			var sql = "select * from Withdraw where id = ?";
			pool.query(sql, [query.TxId], function(err, data1) {
				if (data1.length < 1) {
					console.log('没搜索到这个提现申请', err);
					callback();
				} else {
					data1=data1[0];
					nextByName(-data1.amounts, result.data.UserName, "提现失败");
					callback();
				}
			});
		}
	})
	

}

function zxCoinWithdrawConfirm(query, result, txId, userHash, callback) {
	console.log("提现确认处理---")
	// console.log("query", query)
	// console.log("result", result)
	// console.log("txId", txId)
	// console.log("userHash", userHash)
/*
	query {
		TxId: 'e4983620-e2dd-46ad-af8e-45a302aa1173',
		ModifyTime: 1491903597
	}
	result {
		code: 0,
		message: 'Success',
		data: {
			TxId: '524e4b71-bced-4e41-a001-2bb228c15308',
			UserName: 'star1',
			UserHash: '77e2ab8989ce1736a337c8915fa0bfdbd5682f5913e80c35bea562b9'
		}
	}
	txId 6d226be9-a621-4cfe-bd58-98e9c4732e8e
	userHash bae43aa9c6838f2dc0906f8cae6d4910dacc51c43f8d8f02b36bfb43
*/
	query=checkTxTime(query);
   	query.userId=config.userName;
   	var operator='';
	if (query.Extras) {
		operator = query.Extras;
	};
   	var sql = "update Withdraw set ? where id = ?";
	pool.query(sql, [{
		state  		: (result.data.Extras === config.userName ? 8 : 3),//判断发起者如果不是本人那就是后台自动确认了
		updateTime	: query.ModifyTime
	}, query.TxId], function(err, data) {
		if (err) {
			console.log('用户已确认打钱失败', err);
			callback();
		} else {
			console.log("用户已确认打钱成功----------")

			var sql = "select * from Withdraw where id = ?";
			pool.query(sql, [query.TxId], function(err, data) {
				if (data.length < 1) {
					console.log('没搜索到这个提现申请', err);
					callback();
				} else {

					//TODO 插入交易记录
					insertTranscation({
						txId 		: txId,
						myHash 		: data[0].userId,
						myName 		: data[0].name,
						amount 		: data[0].amounts,
						relatedHash : query.TxId,
						operator 	: operator,
						fee 		: 0,
						type 		: (result.data.UserName === config.userName ? 16 : 11),
						time 		: query.ModifyTime
					});
					nextByName(-data[0].amounts,config.userName,"提现确认");
					callback();
				}
			});
		}
	})
	
}

function zxCoinWithdrawAppeal(query, result, txId, userHash, callback) {

	console.log("提起申诉---")
	// console.log("query",query)
	// console.log("result",result)
	// console.log("txId",txId)
	// console.log("userHash",userHash)
	/*
	query {
		TxId: 'b9f76892-c379-49af-b152-6bd6d59f0b10',
		ModifyTime: 1491899315,
		Remark: '456'
	}
	result {
		code: 0,
		message: 'Success',
		data: 'b9f76892-c379-49af-b152-6bd6d59f0b10'
	}
	txId 3b465725-e72d-4070-92e9-ae5d3d259f10
	userHash 26c09aba8b9990367e28a6760a3b6f8457c5e9ac73995bf29090bf3d
	*/

	var sql = "update Withdraw set ? where id = ?";
	query=checkTxTime(query);
   	
	pool.query(sql, [{
		state: 5,
		appeal: query.Remark,
		updateTime: query.ModifyTime
	}, query.TxId], function(err, data) {
		if (err) {
			console.log('处理申诉请求失败', err);
		} else {
			console.log("处理申诉请求成功----------")
			callback();
		}
	});
}

function zxCoinWithdrawAppealDone(query, result, txId, userHash, callback) {

	console.log("申诉结果处理---")
	// console.log("query",query)
	// console.log("result",result)
	// console.log("txId",txId)
	// console.log("userHash",userHash)

	/*
	query {
		TxId: 'b9f76892-c379-49af-b152-6bd6d59f0b10',
		ModifyTime: 1491899914,
		Result: 1
	}
	result {
		code: 0,
		message: 'Success',
		data: {
			TxId: 'b9f76892-c379-49af-b152-6bd6d59f0b10',
			ModifyTime: 1491899914,
			Result: 1,
			Remark: '',
			UserName: 'star1',
			UserHash: '77e2ab8989ce1736a337c8915fa0bfdbd5682f5913e80c35bea562b9'
		}
	}
	txId 36916284-a071-40d2-90c7-fd1e46c4faf3
	userHash 83b565b424335e188ca7e68edc6fc317577afe9b61ea4bc0e3aee940
*/

	//Result：1 申诉驳回 2 申诉接受
	var operator='';
	if (query.Extras) {
		operator = query.Extras;
	};
	query=checkTxTime(query);
	var sql = "update Withdraw set ? where id = ?";
	pool.query(sql, [{	
		state 			: query.Result == 1 ? 6 : 7,
		remark 			: query.Remark,
		updateTime      : query.ModifyTime,
	    appealAdmin     : operator

	}, query.TxId], function(err, data) {
		if (err) {
			console.log('申诉结果处理 错误', err);
			callback();
		} else {
			console.log("申诉结果处理完成------")

			var sql = "select * from Withdraw where id = ?";
			pool.query(sql, [query.TxId], function(err, data) {
				if (data.length < 1) {
					console.log('没搜索到这个提现申请', err);
					callback();
				} else {

					if (query.Result == 2) {
						// balance 增加
						nextByName(-data[0].amounts,result.data.UserName,"提现申诉接受")
						callback();
						return;
					};
					//TODO 插入交易记录
					insertTranscation({
						txId 		: txId,
						myHash 		: result.data.UserHash,
						myName 		: result.data.UserName,
						amount 		: data[0].amounts,
						relatedHash : query.TxId,
						fee 		: 0,
						type 		: 14,//query.Result + 13,
						operator 	: operator,
						time 		: query.ModifyTime
					});
					nextByName(-data[0].amounts,config.userName,"提现申诉驳回");
					callback();
				}
			});

		}

	})

}

function zxCoinWithdrawRemitSucces(query, result, txId, userHash, callback) {
	console.log("提现已打款处理---")
	// console.log("query",query)
	// console.log("result",result)
	// console.log("txId",txId)
	// console.log("userHash",userHash)
	/*
	query {
		TxId: '3131300a-d22b-4d26-bcad-46c86d1e64a6',
		ModifyTime: 1491896921,
		RemitOrderNumber: 'yy'
	}
	result {
		code: 0,
		message: 'Success',
		data: {
			TxId: '3131300a-d22b-4d26-bcad-46c86d1e64a6',
			ModifyTime: 1491896921,
			RemitOrderNumber: 'yy',
			UserName: 'star1',
			UserHash: '77e2ab8989ce1736a337c8915fa0bfdbd5682f5913e80c35bea562b9'
		}
	}
	txId 40c97186-4acc-4f6b-afe7-0b14e5044504
	userHash 7 aeb8ac7e2dc0244a8e9d59d9c88a0fa9a06a8c8e35695a1717d6d6e
	*/

	var sql = "update Withdraw set ? where id = ?";
	pool.query(sql, [{
		state       : 2,
		orderNum    : query.RemitOrderNumber,
		updateTime  : new Date(),
		payAdmin    : query.Extras
	}, query.TxId], function(err, data) {
		if (err) {
			console.log('提现已打款失败', err);
		} else {
			console.log("提现已打款成功----------")
			callback();
		}
	})

}



function oneLotteryBet(query, result, txId, userHash, callback) {
	//更新用户信息
	/*
	query = {
		lotteryID: 'c8ec0fcc-002a-4350-b751-26ae946b6b7d',
		amount: 10000,
		count: 1,
		userID: 'wangyi',
		CreateTime: 1491387699300
	}
	result = {
		code: 0,
		message: '',
		data: {
			numbers: '10000000',
			name: 'wangyi',
     		owner: 'dff4b2840401c775106cef81a407fa9c1c353f91de54beb64de95ac6'
		}
	}
	txId = a768ea71-ff77-4dc3-8679-f231e7a97328
	userHash c040bc4d30955a581a6fcf1895e29de98d040645b87a07a07782731c
	*/
	if (result.data.name && result.data.owner) {
		updateUserInfo(result.data.owner, {
			name: result.data.name
		});
		//重要：合并未激活之前的赊账账户，和正式账户的余额。
		mergeForkUser(result.data.name, result.data.owner);
	};

	// 根据用户名去获取用户hash,如果没有的话，那就先用当前的userHash
	var sql = "select userId from UserInfo where name = ?";
	pool.query(sql,[query.userID], function(err, hash) {
		var userId = userHash;
		if (hash.length < 1) {
			console.log('没搜索到这个用户',query.userID);
			// 那就先把这个账户的钱赊账一下，放在db中，等激活后，将这个钱合并到余额内，然后再删除这个账户。
			var _sql = "insert into UserInfo set ?";
			pool.query(_sql, {
				userId: "fork_" + uuidV4(),
				name: query.userID,
				balance: -query.amount,
				activationTime: new Date(query.time)
			}, function(err) {
				if (err) {
					console.log('创建临时赊账账户', err)
				} else {
					console.log('创建临时赊账账户')
				}
			})

		} else {
			userId = hash[0].userId;
			if (userId.startsWith('fork_')) {
				//扣除投注费用
				nextByName(query.amount, query.userID, "投注");
			} else {

				updateUserInfo(userId, {
					name: query.userID
				});
				//扣除投注费用
				result.data.owner = userId;
				next({
					fee: query.amount
				}, result);
			}
		}
		console.log(query.userID,' hash=',userId);

		//插入投注表
		var sql1 = "insert into OneLotteryBet set ?";
		pool.query(sql1, [{
			ticketId: txId,
			lotteryId: query.lotteryID,
			attendeeHash: userId,
			attendeeName: query.userID,
			betNumbers: result.data.numbers,
			betCost: query.amount,
			betCount: query.count,
			prizeLevel: 0,
			bonus: 0,
			createTime: new Date(query.CreateTime)
		}], function(err) {
			if (err) {
				console.log('投注成功', err);
			} else {
				console.log("投注成功----------")

			}
		});

		//更新活动信息
		//是不是应该updateTime也更新一下呢，还可以根据是否投满设置活动状态为可开奖（看web是否有必要？状态有未开始0，进行中1，成功23和失败4？）
		var sql2 = "update OneLottery set curBetAmount = curBetAmount + " + query.amount +
			" , curBetCount = curBetCount + " + query.count + " where lotteryID = ?";
		pool.query(sql2, [query.lotteryID], function(err) {
			if (err) {
				console.log('活动投注更新', err);
			} else {
				console.log("活动投注更新---------");

			}
		});
		//插入交易记录
		insertTranscation({
			txId: txId,
			myHash: userId,
			myName: query.userID,
			relatedHash: query.lotteryID,
			amount: query.amount,
			fee: 0,
			type: 2,
			time: new Date(query.CreateTime)
		});
		//设置活动创建者冻结账户金额。将大家投注的钱放到活动创建者的reserved金额内
		// 但是作为投注的时候 publisherHash 没下发, 只好去活动表搜索出来后再赋值了

		var sql3 = "select publisherHash, curBetCount, maxAttendeeCnt from OneLottery where lotteryID = ?";
		pool.query(sql3, [query.lotteryID], function(err, onelot) {
			if (err) {
				console.log('查询活动发布者——————————', err);
			} else {
				console.log("查询活动发布者——————————");
				onelot = onelot[0];

				var sql4 = "update UserInfo set reserved = reserved + " + query.amount + "  where userId = ?";
				pool.query(sql4, [onelot.publisherHash], function(err) {
					if (err) {
						console.log('冻结账户金额——————————', err);
					} else {
						console.log("冻结账户金额——————————")

					}
				})

				if (onelot.curBetCount == onelot.maxAttendeeCnt) {
					var sql5 = "update OneLottery set state = 2 where lotteryID = ?";
					pool.query(sql5, [query.lotteryID], function(err) {
						if (err) {
							console.log('更新活动为可开奖——————————', err);
						} else {
							console.log("更新活动为可开奖——————————")
						}
					})
				};
			}
		})

		callback();
	});
	
};

function checkTxTime(query) {
	if (query.ModifyTime === undefined || query.ModifyTime == 0) {
		query.ModifyTime = new Date();
	} else {
		query.ModifyTime = new Date(query.ModifyTime<2000000000 ? query.ModifyTime*1000 : query.ModifyTime)
	}
	return query;
}
//扣除各种费用(用户未激活，通过用户名)
function nextByName(amount, name, from) {

	var sql = "update UserInfo set balance = balance - " + amount + "  where name = ?";
	pool.query(sql, [name], function(err) {
		if (err) {
			console.log(from, '扣除费用', err)
		} else {
			console.log(from, '扣除费用------------')
		}
	})
}
//扣除各种费用
function next(query, result) {

	var sql = "update UserInfo set balance = balance - " + query.fee + "  where userId = ?";
	pool.query(sql, [result.data.owner], function(err) {
		if (err) {
			console.log('扣除费用', err)
		} else {
			console.log('扣除费用------------')
		}
	})
}
//记录各种交易记录(有关钱的记录)
function insertTranscation(value) {
	var sql = "insert into TransactionDetail set ?";
	pool.query(sql, value, function(err) {
		if (err) {
			console.log('交易入库', err);
		} else {
			console.log('交易入库--------------');
		}
	})

}
//更新用户名根据hash
function updateUserInfo(userHash, set) {
	var sql = "update UserInfo set ? where userId = ?";
	pool.query(sql, [set, userHash], function(err) {
		if (err) {
			console.log('更新用户名信息', err);
		} else {
			console.log("更新用户名信息----------")

		}
	})
}
//更新投注表用户hash
function updateBetUserName(name, set) {
	var sql = "update OneLotteryBet set ? where attendeeName = ?";
	pool.query(sql, [set, name], function(err) {
		if (err) {
			console.log('更新投注表用户信息', err);
		} else {
			console.log("更新投注表用户信息----------")

		}
	})
}
//更新交易表投注类型的用户hash
function updateTranUserName(name, set) {
	var sql = "update TransactionDetail set ? where myName = ?";
	pool.query(sql, [set, name], function(err) {
		if (err) {
			console.log('更新交易表投注类型的用户信息', err);
		} else {
			console.log("更新交易表投注类型的用户信息----------")

		}
	})
}
//重要：合并未激活之前的赊账账户，和正式账户的余额。
// funon mergeForkUser(query.publisherName, result.data.publisherHash);
function mergeForkUser(name, hash) {
	var sql = "select userId, name, balance from UserInfo where name = ?";
	pool.query(sql, [name], function(err, data) {
		if (err) {
			console.log('合并未激活之前的赊账账户，查找', err);
		} else {
			// console.log('合并未激活之前的赊账账户，查找到',data.length);
			if (data.length == 2) {
				var sum_bal = data[0].balance + data[1].balance;
				sql = "update UserInfo SET balance = ? where userId = ?";
				pool.query(sql, [sum_bal, hash], function(err, data) {
					if (err) {
						console.log('合并未激活之前的赊账账户,更新余额', err);
					} else {
						sql = "delete from UserInfo where name = ? and userId != ?";
						pool.query(sql, [name, hash], function(err, data) {
							if (err) {
								console.log('合并未激活之前的赊账账户', err);
							} else {
								console.log('合并未激活之前的赊账账户');
							}
						});
					}
				});
			}

		}
	});
}

function getSha(str) {
	var sha3_224 = require('js-sha3').sha3_224
	return sha3_224(str);
}

module.exports = parse;