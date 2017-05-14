exports.get = function*() {
	try {
		console.log('kdslfjdkfdslfj', this.request.query)
		var args = {
			lotteryID: this.request.query.id,
			currentTime: new Date().getTime()
		}
		args = JSON.stringify(args);
		var result = yield chain.invoke('oneLotteryRefund', ['', args]);
		console.log(result);

		this.response.body="退款成功";
	}catch(e){
		this.response.body="退款失败";
		console.log('err:',e);
	}
}