var mysql = require('mysql');
var pool = mysql.createPool({
	connectionLimit: 10,
	host: 'localhost',
	user: 'root',
	password: '123',
	database: 'peersafe',
	charset: 'utf8'
});

module.exports=pool;