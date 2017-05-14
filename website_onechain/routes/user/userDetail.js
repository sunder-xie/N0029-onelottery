'use strict'
const wrapper = require('co-mysql');
const pool = wrapper(global.pool);
var config = require('../../config');
var eachPage = 4;

exports.get = function*() {
    var userHash =this.request.query.userHash;
    var userName =this.request.query.userName;
    var sql = 'select * from UserInfo where userId = ?';
    var balance = yield pool.query(sql,[userHash]);
    balance = balance[0].balance;
    yield this.render('user/userDetail', {
        userHash: userHash,
        userName:userName,
        balance:balance
    });
 
}

