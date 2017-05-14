'use strict'
var EventHub = require('hfc').EventHub;
var Chain = require('../lib/chain');
var path = require('path');
var config = require('../config');
var mysql = require('mysql');
var pool = mysql.createPool({
  connectionLimit: 10,
  host: 'localhost',
  user: 'root',
  password: '123',
  database: 'peersafe',
  charset: 'utf8'
});
require('./schedule/everyDay');
require('./schedule/refund');
var store = path.join(__dirname, '.' + config.store);
var chain = new Chain(config.servicesUrl, config.peer, store, config.chaincodeID, config.userName);

function init() {
  console.log('init****')
  chain.init().then(function() {
    console.log('chain初始化完成')
    // var args = '{"userCertTo":"19b9039b58cfa6a7fef5635bcbf76613cdf372dfeb5913bb213e4236","nameTo":"","amount":10000,"type":0,"userId":"one_chain_admin","fee":100,"time":1488440242172}'
    // // 初始化完成后测试了下转账接口
    // chain.invoke('zxCoinTransfer', ['', args], function() {
    //   console.log('测试充值成功！')
    // });
  });
  var sql = "select * from UserInfo where userId = ? ";

  // 初始插入官方账户，初始金额为10亿ZXB
  pool.query(sql, [config.args.userHash], function(err, result) {
    if (result.length < 1) {
      sql = "insert into UserInfo set ?";
      pool.query(sql, {
        userId: config.args.userHash,
        name: config.userName,
        balance: 1000000000 * 10000
      }, function(err) {
        if (!err) {
          console.log('one_chai_admin初始金额设置完成')
        }
      })
    }
  });

  // 将chain, pool, event设为全局变量
  global.chain = chain;
  global.pool = pool;
  var event = new EventHub();
  event.setPeerAddr(config.eventPeer);
  event.connect();
  global.event = event;
}
init();