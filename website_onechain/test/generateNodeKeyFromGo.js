var fs = require('fs');

var certeficat = {};
var sourcePath = './crypto/client/one_chain_admin/ks/raw/';

certeficat.name = fs.readFileSync(sourcePath + 'enrollment.id').toString();


//enrollment
certeficat.enrollment = {};
var keySource = fs.readFileSync(sourcePath + 'enrollment.key').toString().split('\n');
var key = getDataFromPem(keySource);
console.log(key)
console.log(new Buffer(key, 'base64').toString())
certeficat.enrollment.key = new Buffer(key, 'base64').toString('hex');



var certSource = fs.readFileSync(sourcePath + 'enrollment.cert').toString().split('\n');
var cert = getDataFromPem(certSource);
console.log(cert)
certeficat.enrollment.cert = new Buffer(cert, 'base64').toString('hex');


//chainKey
var chainKeySource = fs.readFileSync(sourcePath + 'chain.key').toString().split('\n');
var chainKey = getDataFromPem(chainKeySource)
certeficat.chainKey = new Buffer(chainKey, 'base64').toString('hex')

certeficat.queryStateKey = {
	type: 'buffer'
}

console.log(certeficat);
fs.writeFileSync('../keyValStore/member.' + certeficat.name, JSON.stringify(certeficat));

function getDataFromPem(source) {
	var result = '';
	for (var i = 0; i < source.length; i++) {
		if (source[i][0] !== '-') {
			result += source[i]
		}
	}
	return result;
}