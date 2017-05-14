'use strict'
const wrapper = require('co-mysql');
const pool = wrapper(global.pool);
var config = require('../../config');
var eachPage = config.eachPage;

exports.get = function*() {
    var userHash =this.request.query.userHash;
    var currentPage = 1;
    if (this.request.query.currentPage) {
        currentPage = parseInt(this.request.query.currentPage);
    }
    var arr = []
    var sql = "select * from UserInfo where (name !='one_chain_admin' or name is NULL) ";
    var countsql = "select count(*) count from UserInfo where (name !='one_chain_admin' or name is NULL) ";
    if (userHash) {
        if (userHash.length == 56) {
            sql += "and userId = ?"
            countsql += "and userId = ?"
            arr.push(userHash);
        } else {
            sql += "and name = ?"
            countsql += "and name = ?"
            arr.push(userHash);
        }
    }
    sql += " order by activationTime desc limit " + (currentPage - 1) * eachPage + "," + eachPage;
    var balance = yield pool.query(sql,arr);
    var count = yield pool.query(countsql,arr);
    count = count[0].count;
    //balance = balance[0].balance;
    console.log(eachPage)
    yield this.render('user/list', {
        data: balance,
        pageCount: count,
        currentPage: currentPage,
        eachPage:eachPage
    });

}

