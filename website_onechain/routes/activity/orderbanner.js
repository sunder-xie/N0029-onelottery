'use strict'
const wrapper = require('co-mysql');
const pool = wrapper(global.pool);
var config = require('../../config');




exports.get = function*() {

}
exports.post = function*() {
    var data = this.request.body;

    var sql = "update Banner set imgOrder=? where imgName=?";
    console.log(data)
    try {
        yield pool.query(sql, [parseInt(data.corder), data.filename])
        yield pool.query(sql, [parseInt(data.order), data.cfilename])
        this.response.body = '修改成功';
    } catch (e) {
        console.log(e);
        this.response.body = '修改失败';
    }
}
