var grpc = require('grpc');
var path = require('path');
var protobuf = require('protocol-buffers')
var config = require('../config.json');
var protoPath = path.join(require.resolve('hfc'), '../lib/protos');
var fs = require('fs');
var api = grpc.load(protoPath + "/api.proto").protos;
console.log(config.peer.substr(7))
var client = new api.Openchain(config.peer.substr(7),grpc.credentials.createInsecure());

// client.getBlockCount({},function(err,data){
// 	console.log(err,data)
// })

exports.client = client;
function decodeChaincodeSpec(buf){
	var spe = api.ChaincodeInvocationSpec.decode(buf);
	return spe.chaincodeSpec.ctorMsg.args;
} 

exports.decode = decodeChaincodeSpec;

