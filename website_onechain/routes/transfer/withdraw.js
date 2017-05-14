var chain = global.chain;
var config = require('../../config');

exports.get = function*() {
	yield this.render('transfer/withdraw');
}