'use strict'
const wrapper = require('co-mysql');
const pool = wrapper(global.pool);

exports.get = function*() {
    var sql = 'select * from Banner order by imgOrder';
    var imgmsg = [];
    try {
        var rules = yield pool.query(sql);
        for(var i in rules){
            imgmsg.push({"imgName":rules[i].imgName,"url":rules[i].imgAddress})
        }

        this.response.body = imgmsg;
    } catch (e) {
        console.log(e);
    }
}
