var router = require('koa-router')();
router.get('/*', function*() {
  // try {
  var path = this.request.path;
  if(path==='/getBanner'){
    yield require('./getBanner').get;
    return;
  }
  console.log(path)
  if (!this.session.user) {
    yield this.render('login');
    return;
  }
  if (path === '/' || path === '/index') {
    yield require('./asset').get;
  } else {
    if (path !== undefined) {
      console.log(path)
      yield require('.' + path).get;
    }
  }

  // } catch (e) {
  //   console.log(e);
  // }

})
router.post('/*', function*() {

  // try {
  var clientIp = this.request.header['x-forwarded-for'];
  var path = this.request.path;
  console.log(path, clientIp)
  if (path === '/webRecharge' && (clientIp === '123.57.146.46' || clientIp === '182.92.114.175')) {
    yield require('./webRecharge').post;
    return;
  }

  if (!this.session.user && path != '/login' && path != '/webRecharge') {
    yield this.render('login');
    return;
  }
  console.log(this.request.method, path);
  yield require('.' + path).post;

  // } catch (e) {
  //   console.log(e);
  // }

})

module.exports = router;