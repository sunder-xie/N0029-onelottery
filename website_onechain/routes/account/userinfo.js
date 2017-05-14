exports.get = function*(){
	this.response.body = this.session.user;
};
