'use strict'
const wrapper = require('co-mysql');
const pool = wrapper(global.pool);
var config = require('../../config');


//财富榜
exports.get = function*() {
    var sql = "select * from UserInfo where name != 'one_chain_admin' and name != 'null' order by balance desc limit 0,20 ";
    var data = yield pool.query(sql);
    yield this.render('asset/rankinglist',{
        data: data
    })
}