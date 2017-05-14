var http = require('http');
var app = require('koa')();
var logger = require('koa-logger');
require('./lib/date');
require('./lib/init');


var views = require('koa-views');
var bodyparser = require('koa-bodyparser');
var session = require('koa-session');
//初始化chaincode
var router = require('./routes/index');
app.keys = ['zx-asset'];

// app['Max-Age'] = 10;
app.use(bodyparser());
app.use(session({
	maxAge: 1000 * 60 * 60 
}));
app.use(require('koa-static')(__dirname + '/public'));
app.use(views('views', {
	root: __dirname + '/views',
	default: 'ejs'
}));
app.use(logger());
app.use(router.routes());
process.on('uncaughtException', function(err) {
  console.log('Caught exception: ' + err);
});
http.createServer(app.callback()).listen(8004);
console.log('this server listening 8004')