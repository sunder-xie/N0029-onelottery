'use strict'
const wrapper = require('co-mysql');
const pool = wrapper(global.pool);
var config = require('../../config');
var eachPage = config.eachPage;

exports.get = function*() {

     console.log(this.request.query)
     var sql = '';
     var count = 0;
     var data = {};
     var currentPage = 1;
     if (this.request.query.currentPage) {
          currentPage = parseInt(this.request.query.currentPage);
     }
     var userHash = this.request.query.userHash;
     var date = this.request.query.date.split('一');
     var type = parseInt(this.request.query.type);
     var startDate = new Date(date[0]);
     var endDate = new Date(date[1]);
     endDate.setDate(endDate.getDate() + 1);
     console.log(startDate, endDate, type)
     sql += "from (select '' outOrIn,b.name ,type,amount,fee,time,relatedHash  from TransactionDetail a  join OneLottery b where a.relatedHash = b.lotteryID and a.myHash = ? ";
     sql += "union  select '' outOrIn,relatedHash ,type,amount,fee,time,'' name from TransactionDetail where type in (11,14,16) and myHash = ? ";
     sql += "union  select '' outOrIn,relatedHash ,type,amount,fee,time,'' name from TransactionDetail where type =1 and relatedHash = ? ";
     sql += "union  select '支出' outOrIn,relatedHash ,type,amount,fee,time,d.name from TransactionDetail c join UserInfo d where c.relatedHash = d.userId and myHash = ?  and type =8 ";
     sql += "union  select '存入' outOrIn,myHash relatedHash, type,amount,fee,time,f.name from TransactionDetail e join UserInfo f where e.myHash = f.userId and relatedHash = ?  and type =8) t where time > ? and time < ?";
     if (type != 0) {
          if (type == 9) {
               sql += " and type in (11,14,16) ";
          } else {
               sql += " and type = ? ";
          }
     };
     console.log(sql);
     console.log(sql);
     count = yield pool.query("select count(*) count " + sql, [userHash, userHash, userHash, userHash, userHash, startDate, endDate, type]);
     count = count[0].count;
     sql += " order by time desc limit " + (currentPage - 1) * eachPage + "," + eachPage;
     console.log(count)
     data = yield pool.query("select * " + sql, [userHash,userHash,userHash, userHash, userHash, startDate, endDate, type]);
     console.log('data=',data)
     var pageCount = parseInt(count / eachPage) + (count % eachPage == 0 ? 0 : 1);
     yield this.render('user/userDetaillist', {
          data: data,
          json: {
               '1': "充值",
               '8': "转账",
               '2': "投注",
               '3': "活动退款",
               '4': "活动中奖",
               '5': "活动创建",
               '6': "活动修改",
               '7': "活动分成",
               '11': "提现",//"用户已确认",
               '14': "提现",//"申诉驳回",
               '16': "提现",//"后台自动确认",
          },
          pageCount: count,
          currentPage: currentPage,
          eachPage:eachPage
     });
}