'use strict'
const path = require('path');
const fs = require('fs');
var basePath = path.join(require.resolve('hfc'), '../lib/hfc.js');
var data = fs.readFileSync(path.join(__dirname, '../lib/hfc.js'))
fs.writeFileSync(basePath,data);
var hfc = require('hfc');
var Promise = require('bluebird');
var Certificate = hfc.Certificate;
var PrivacyLevel = hfc.PrivacyLevel;
var __extends = (this && this.__extends) || function(d, b) {
	for (var p in b)
		if (b.hasOwnProperty(p)) d[p] = b[p];

	function __() {
		this.constructor = d;
	}
	d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};
var ECert = (function(_super) {
	__extends(ECert, _super);

	function ECert(cert, privateKey) {
		_super.call(this, cert, privateKey, PrivacyLevel.Nominal);
		this.cert = cert;
		this.privateKey = new Key(privateKey, null);
		this.publicKey = cert;
	}
	return ECert;
}(Certificate));
var Key = function Key(key) {
	this.priv = key;
	this.pub = null;
}
Key.prototype.getPrivate = function(code) {
	return this.priv;
}

function Chain(service, peer, store, chaincodeID, userName) {
	var chain = new hfc.newChain();
	chain.setMemberServicesUrl(service);
	chain.setKeyValStore(hfc.newFileKeyValStore(store));
	chain.addPeer(peer, "");
	this.chaincodeID = chaincodeID;
	this.chain = chain;
	this.userName = userName;

};

Chain.prototype.init = function() {
	var self = this;
	return new Promise(function(resolve, reject) {
		self.chain.getUser(self.userName, function(err, user) {
			if (!err) {
				self.user = user;
				resolve(null);
			} else {
				reject(err);
			}
		})
	})
}
Chain.prototype.getUser = function(name) {
	return new Promise(function(resolve, reject) {
		this.chain.getUser(name, function(err, user) {
			if (err) {
				reject(err);
			} else {
				resolve(user);
			}
		})
	})
};

Chain.prototype.query = function(fn, args, chaincodeID) {
	var self = this;
	var key = new ECert(new Buffer(self.user.enrollment.cert, 'hex'), self.user.enrollment.key, 2);
    args.shift();
	return new Promise(function(resolve, reject) {
		var queryRequest = {
			fcn: fn,
			args: args,
			chaincodeID: self.chaincodeID,
			userCert: key
		};
		var queryTx = self.user.query(queryRequest);
		queryTx.on('complete', function(results) {
			resolve(results.result.toString());
		});
		queryTx.on('error', function(err) {
			reject(err);
		});
	})
}
Chain.prototype.invoke = function(fn, args, chaincodeId) {
	var self = this;
	var key = new ECert(new Buffer(self.user.enrollment.cert, 'hex'), self.user.enrollment.key, 2);

	args.shift();
	return new Promise(function(resolve, reject) {
		var queryRequest = {
			fcn: fn,
			args: args,
			chaincodeID: self.chaincodeID,
			userCert: key
		};
		var uuid = '';
		var queryTx = self.user.invoke(queryRequest);
		queryTx.on('submitted', function(results) {
			uuid = results.uuid;
		});
		queryTx.on('complete', function(results) {
			results.uuid = uuid;
			resolve(results);
		});
		queryTx.on('error', function(err) {
			console.log(err)
			reject(err);
		});

	})
}

module.exports = Chain;