'use strict'
const wrapper = require('co-mysql');
const pool = wrapper(global.pool);
var config = require('../../config');
var eachPage = config.eachPage;

exports.get = function*() {
    var count = 0;
    var currentPage = 1;
    if (this.request.query.currentPage) {
        currentPage = parseInt(this.request.query.currentPage);
    }
    var date = this.request.query.date.split('一');
    console.log(date)
    var startDate = new Date(date[0]);
    var endDate = new Date(date[1]);
    endDate.setDate(endDate.getDate()+1);
    console.log(startDate,endDate)


    var sql = "select count(*) count from UserInfo where name !='one_chain_admin' and activationTime >? and activationTime < ?";
    
    var count = yield pool.query(sql,[startDate, endDate]);
    console.log(count)
    count = count[0].count;
    sql = "select * from UserInfo where name !='one_chain_admin' and name != 'null' and activationTime >? and activationTime < ?";
    sql += " order by activationTime desc limit " + (currentPage-1)*eachPage + "," + eachPage;
    var data  = yield pool.query(sql,[startDate, endDate]);
    var pageCount = parseInt(count / eachPage) + (count % eachPage == 0 ? 0 : 1);
    console.log(data.length)
    yield this.render('user/list', {
        data: data,
        pageCount: count,
        currentPage: currentPage,
        eachPage:eachPage
    });
}
