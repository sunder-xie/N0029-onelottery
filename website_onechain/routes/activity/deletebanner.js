'use strict'
const wrapper = require('co-mysql');
const pool = wrapper(global.pool);
var config = require('../../config');
const util = require('../../lib/util');
const streamBuffers = require('stream-buffers');
const parse = require('co-busboy');
var fs= require('fs');
var path = require('path');



exports.get = function*() {

}
exports.post = function*() {

    var imgName = this.request.body.filename;
    console.log("imgName "+imgName);
    try {
        var sql = "delete from Banner where imgName = ?";
        yield pool.query(sql, [imgName]);
        fs.unlinkSync(path.join(__dirname, '../../public/banner/'+imgName))
        this.response.body = '删除成功';
    } catch (e) {
        console.log(e);
        this.response.body = '删除失败';
    }
}
