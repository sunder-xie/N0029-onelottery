'use strict'
const wrapper = require('co-mysql');
const pool = wrapper(global.pool);
var config = require('../../config');
var eachPage = config.eachPage;
exports.get = function*() {
    console.log(this.request.query);
    var lotteryID = this.request.query.lotteryID;
    var sql = '';
    var count = 0;
    var data = {};
    var currentPage = 1;
    if (this.request.query.currentPage) {
        currentPage = parseInt(this.request.query.currentPage);
    }

    var now = new Date();
    sql = " from OneLotteryBet where lotteryId = ? "
    count = yield pool.query("select count(*) count " + sql,[lotteryID]);
    count = count[0].count;
    sql += "order by createTime desc limit " + (currentPage-1)*eachPage + "," + eachPage;
    data = yield pool.query( "select * " +sql,[lotteryID]);
    for(var i in data){
        data[i].createTime = data[i].createTime.Format("yyyy-MM-dd hh:mm")
    }

    var pageCount = parseInt(count / eachPage) + (count % eachPage == 0 ? 0 : 1);
    yield this.render('activity/list_z', {
        data: data,
        pageCount: count,
        currentPage: currentPage,
        eachPage:eachPage
    });
}
function compareDate(end, start) {
    var intervalTime = end - start; //两个日期相差的毫秒数 一天86400000毫秒
    var Inter_Days = (intervalTime / 86400000); //加1，是让同一天的两个日期返回一天

    return Inter_Days;
}
