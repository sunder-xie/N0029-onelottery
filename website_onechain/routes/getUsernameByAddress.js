var chain = global.chain;

exports.get = function*(){
	var args = {};
	args.owner = this.request.query.owner;
	args = JSON.stringify(args);
	console.log(args);
	try {
		var result = yield chain.query('zxCoinGetUserInfo', ['', args]);
		result =JSON.parse(result);
		console.log(result,result.data.userId)
		if (result.code == 0) {
			this.response.body = result.data.userId;
		} else {
			this.response.body = ''
		}
	} catch (e) {
		console.log(e);
		this.response.body = ''
	}
}