var pool = require('./mysqlPool.js');
var client = require('./grpc.js').client;
var EventHub = require('hfc').EventHub;
var config = require('../config.json');
var async = require('async');
var parse = require('./parse');


var path = require('path');
var Chain = require('../lib/chain');
var store = path.join(__dirname, '.' + config.store);
var event = new EventHub();
event.setPeerAddr(config.eventPeer);
event.connect();
global.event = event;
var chain = new Chain(config.servicesUrl, config.peer, store, config.chaincodeID, config.userName);
global.chain = chain;
var chaincodeID = config.chaincodeID;
chain.init().then(function() {
    console.log('chain初始化成功');
    start();

})

function start() {
    //获取当前数据库的区块位置
    pool.query('select blockNum  from SyncState where id = 1', function(err, data) {
        var startNum;
        if (!data[0]) {
            startNum = 0;
        } else {
            startNum = !data[0].blockNum ? 0 : data[0].blockNum;

        }
        console.log(startNum)
        //获取当前区块位置
        client.getBlockCount({}, function(err, data) {
            console.log(err, data)
            var currentNet = parseInt(data.count);
            console.log('当前区块高度：', data.count);
            if (startNum < currentNet - 1) {
                prepareTask(startNum, currentNet - 1, function(err, rs) {
                    start();
                });
            } else {
                listen(startNum + 1);
            }
        })
    })
}
//通过grpc获取整个区块信息并存入数据库
function prepareTask(startNum, endNum, callback) {

    console.log(startNum, endNum);
    client.getBlockByNumber(startNum, function(err, data) {
        if (err) {
            console.log(err)
        }
        //解析请求的数据
        parse(data, startNum, function(err, rs) {

            startNum++;
            if (startNum <= endNum) {
                prepareTask(startNum, endNum, callback);
            } else {
                callback()
            }

        })
    });
    //创建任务，每一百个block并发请求一次(不足一百的算一次),然后对处理数据，入库后在请求下一百个block。
    // var size = Math.floor((endNum - startNum) / 100);
    // var task = [];
    // if (size) {
    //     for (var i = 0; i < size; i++) {
    //         var ary = [];
    //         for (var j = 1; j < 101; j++) {
    //             ary.push(startNum + j + i * 100)
    //         };
    //         task.push(ary);
    //     }
    // }
    // //不足一百的
    // var shengyu = (endNum - startNum) % 100;
    // var one = [];
    // for (var i = endNum; i > endNum - shengyu; i--) {
    //     one.push(i);
    // }
    // task.push(one);
    // startTask(task, function(err, data) {
    //     console.log('拉取区块信息完成');
    //     callback();
    // })
}
//并发请求
function startTask(task, callback) {
    console.log(task.length)
    if (task.length > 0) {
        var t = task[0];
        var task = task.slice(1);
        console.log(t.toString())
        async.map(t, function(item, cb) {
            //grpc请求
            client.getBlockByNumber(item, function(err, data) {
                if (err) {
                    console.log(err)
                }
                //解析请求的数据
                parse(data, item, function(err, rs) {

                    cb(null);
                })
            })
        }, function(err, result) {
            startTask(task, callback);
        })
    } else {
        callback();
    }
}
//监听block事件
function listen(blockNum) {
    console.log('开始监听', blockNum)
    var event = new EventHub();
    event.setPeerAddr(config.eventPeer);
    event.connect();
    chain.init().then(function() {
        event.registerBlockEvent(function(event) {
            var bn = blockNum;
            blockNum++;
            parse(event, bn, function(err, rs) {

            })
        });
    })

}
// listen(0);