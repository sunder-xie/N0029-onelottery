'use strict'
var path = require('path');
var fs = require('fs');

var basePath = path.join(require.resolve('hfc'), '../lib/hfc.js');
var data = fs.readFileSync(path.join(__dirname, '../lib/hfc.js'))
fs.writeFileSync(basePath,data);
var hfc = require('hfc');

var config = require('../config');

var store = path.join(__dirname, '.' + config.store);
var chaincodeID = config.chaincodeID;
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

var chain, admin, webAdmin;
var data = JSON.parse(fs.readFileSync('../config.json').toString());
chain = new hfc.newChain('test');
chain.setKeyValStore(hfc.newFileKeyValStore(store));
chain.setMemberServicesUrl(config.servicesUrl);
chain.addPeer(config.peer, "");
chain.enroll('admin', 'Xurw3yU9zI0l', function(err, user) {
  if (err) {
    throw err;
  }
  admin = user;
  chain.setRegistrar(admin);

  // console.log('admin', user);
  var registrationRequest = {
    roles: ['client'],
    enrollmentID: 'one_chain_admin',
    affiliation: "bank_a",
    attributes: null,
    secret: 'abcd',
    registrar: {
      roles: ['client']
    }
  }
  chain.registerAndEnroll(registrationRequest, function(err, user) {
    if (err) {
      throw err;
    }
    webAdmin = user;
    getPubkey(user, function(err, pubKey) {
        console.log(err, pubKey)
        if(registrationRequest.enrollmentID == 'one_chain_admin'){
          data.args.userHash = pubKey;
        }
      })
      // query(user)
      // deploy(user);

  })
})

function deploy(user) {
  var args = config.args;
  args.Time = parseInt(new Date().getTime() / 1000)
  var key = new ECert(new Buffer(user.enrollment.cert, 'hex'), user.enrollment.key, 2);
  var deployRequest = {
    // Function to trigger
    fcn: "init",
    // Arguments to the initializing function
    args: ['zxCoinInit', JSON.stringify(args)],
    userCert: key,
    chaincodePath: "peersafe.com/onelottery"
  };
  deployRequest.args[0] = key.encode().toString('base64');

  // Trigger the deploy transaction
  var deployTx = user.deploy(deployRequest);

  // Print the deploy results
  deployTx.on('complete', function(results) {
    // Deploy request completed successfully
    console.log(results);
    // Set the testChaincodeID for subsequent tests
    data.chaincodeID = results.chaincodeID;

    fs.writeFileSync('../config.json',JSON.stringify(data));
  });
  deployTx.on('error', function(err) {
    // Deploy request failed
    console.log('jdfdfk', err)
  });
}

function query(user) {
  user.getUserCert(store + '/member.webAdmin', function(err, key) {
    var userCert = key.publicKey.toString('hex');
    userCert = JSON.stringify({
      UserCert: 'e06e3ebd1994bb27dfee0371d21a01180dc5e913b60e10096504b625'
    });
    console.log(userCert);

    var queryRequest = {
      // Function to trigger
      fcn: "zxCoinAccountBalance",
      // Existing state variable to retrieve
      args: ['', userCert],
      chaincodeID: chaincodeID
    };
    // console.log(queryRequest)
    var queryTx = user.query(queryRequest);

    // Print the query results
    queryTx.on('complete', function(results) {
      // Query completed successfully
      console.log(results.result.toString())
    });
    queryTx.on('error', function(err) {
      // Query failed
      console.log('err', err);
    });
  })

}


function getPubkey(user, cb) {
  var sha3_224 = require('js-sha3').sha3_224;
  var result = sha3_224(new Buffer(user.enrollment.cert, 'hex').toString('base64'));
  cb(null, result)
}

function getSha(str) {
  // var SHA3 = require('sha3');
  // var d = new SHA3.SHA3Hash(224);
  // d.update(str,'utf-8');
  // var result = d.digest('hex');
  // console.log(result)
  var sha3_224 = require('js-sha3').sha3_224
  console.log(str.toString('base64'))
  console.log('abc', sha3_224(new Buffer(str,'hex').toString('base64')))
}
// getSha('qq');