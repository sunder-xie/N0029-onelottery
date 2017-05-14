/*
Copyright IBM Corp. 2016 All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

		 http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package client_sdk

import (
	"encoding/asn1"
	"errors"
	"fmt"
	"io/ioutil"
	"math/big"
	"strings"
	"time"

	"github.com/op/go-logging"
	"golang.org/x/net/context"

	"github.com/golang/protobuf/proto"
	"github.com/spf13/viper"

	"github.com/hyperledger/fabric/core/chaincode/platforms"
	"github.com/hyperledger/fabric/core/comm"
	"github.com/hyperledger/fabric/core/container"
	"github.com/hyperledger/fabric/core/crypto/primitives"
	"github.com/hyperledger/fabric/core/crypto/utils"
	"github.com/hyperledger/fabric/core/util"

	crypto "github.com/hyperledger/fabric/core/crypto"
	membersrvc "github.com/hyperledger/fabric/membersrvc/protos"
	pb "github.com/hyperledger/fabric/protos"
	"google.golang.org/grpc/credentials"
	"log"
	"path/filepath"
)

// restResult defines the response payload for a general REST interface request.
type restResult struct {
	OK    string `json:",omitempty"`
	Error string `json:",omitempty"`
}

var ClientSdkLogger = logging.MustGetLogger("client_sdk")

const invokeOrQueryTimeout = time.Second * 30

/**
 * Description
 *
 * @param configPath
 * @param fileSystemPath
 * @return error
 */
func initClientSdk(configPath string, fileSystemPath string, configFile string, getWebAppAdmin bool) (err error) {
	viper.SetConfigName(configFile)
	viper.SetConfigType("yaml")
	viper.AddConfigPath(configPath)

	err = viper.ReadInConfig()
	if err != nil {
		ClientSdkLogger.Panicf("Fatal error when reading %s config file: %s", "client_sdk", err.Error())
	}

	viper.Set("peer.fileSystemPath", fileSystemPath)

	logging.SetLevel(logging.DEBUG, "client_sdk")

	// Init the crypto layer
	if err := crypto.Init(); err != nil {
		ClientSdkLogger.Panicf("Failed to initialize the crypto layer: %s", err)
	}

	if getWebAppAdmin {
		//Enroll "WebAppAdmin" who is already registered
		err = enroll(GetRegistrarId(), getRegistrarSecret(), nil)
		if err != nil {
			ClientSdkLogger.Error("Enroll webAppAdmin failed!")
		}
	}

	return
}

/**
 * Description Register a user
 *
 * @param registerUserReq
 * @return error
 */
func register(registerUserReq *membersrvc.RegisterUserReq) (enrollPwd string, err error) {
	registrar := new(membersrvc.Registrar)
	identity := new(membersrvc.Identity)
	identity.Id = GetRegistrarId()
	registrar.Id = identity
	// registrar.Roles = []string{"client"}
	registerUserReq.Registrar = registrar

	rawReq, err := proto.Marshal(registerUserReq)
	if err != nil {
		ClientSdkLogger.Errorf("Failed marshaling request [%s].", err.Error())
		return
	}

	//Sign rawReq
	r, s, err := ecdsaSignWithEnrollmentKey(GetRegistrarId(), rawReq, nil)
	if err != nil {
		ClientSdkLogger.Errorf("Failed creating signature for [% x]: [%s].", rawReq, err.Error())
		return
	}

	R, _ := r.MarshalText()
	S, _ := s.MarshalText()

	//Append the signature
	registerUserReq.Sig = &membersrvc.Signature{Type: membersrvc.CryptoType_ECDSA, R: R, S: S}

	//register call grpc
	sock, ecaA, err := getECAAClient()
	defer sock.Close()
	ctx, _ := context.WithTimeout(context.Background(), invokeOrQueryTimeout)
	pbToken, err := ecaA.RegisterUser(ctx, registerUserReq)

	if err != nil {
		ClientSdkLogger.Errorf("Register error for %s,err info is %s", registerUserReq.GetId().Id, err.Error())
		return
	}

	ClientSdkLogger.Infof("Register result is:%s", pbToken.Tok)
	enrollPwd = string(pbToken.Tok)
	return
}

/**
 * Description enroll a user
 *
 * @param enrollID
 * @param enrollPWD
 * @param pwd
 * @return error
 */
func enroll(enrollID string, enrollPWD string, pwd []byte) (err error) {
	ClientSdkLogger.Infof("Enroll begin!!")

	if enrollID == "" || enrollPWD == "" {
		ClientSdkLogger.Error("enrollId and enrollSecret may not be blank.")
		err = errors.New("enrollId and enrollSecret may not be blank.")
		return
	}

	err = crypto.RegisterClient(enrollID, pwd, enrollID, enrollPWD)
	if err != nil {
		ClientSdkLogger.Errorf("Enroll error for %s,err info is %s", enrollID, err.Error())
	}
	return
}

/**
 * Description Register a user and if success,enroll it
 *
 * @param registerUserReq
 * @param pwd
 * @return error
 */
func registerAndEnroll(registerUserReq *membersrvc.RegisterUserReq, pwd []byte) (err error) {
	enrollPwd, err := register(registerUserReq)
	if err != nil {
		ClientSdkLogger.Errorf("RegisterAndEnroll error in register for %s,err info is %s", registerUserReq.GetId().Id, err.Error())
		return
	}

	err = enroll(registerUserReq.GetId().Id, enrollPwd, pwd)
	if err != nil {
		ClientSdkLogger.Errorf("RegisterAndEnroll error in enroll for %s,err info is %s", registerUserReq.GetId().Id, err.Error())
	}

	return
}

/**
 * Description deploy chaincode
 *
 * @param spec
 * @param pwd
 * @return ChaincodeDeploymentSpec error
 */
func deploy(spec *pb.ChaincodeSpec, pwd []byte) (*pb.ChaincodeDeploymentSpec, error) {
	// get the deployment spec
	chaincodeDeploymentSpec, err := getChaincodeBytes(context.Background(), spec)

	if err != nil {
		ClientSdkLogger.Error(fmt.Sprintf("Error deploying chaincode spec: %v\n\n error: %s", spec, err))
		return nil, err
	}

	// Now create the Transactions message and send to Peer.
	transID := chaincodeDeploymentSpec.ChaincodeSpec.ChaincodeID.Name
	fmt.Println("**************  txid  *****************")
	fmt.Println(transID)

	var tx *pb.Transaction
	var sec crypto.Client

	if isSecurityEnabled() {
		sec, err = crypto.InitClient(spec.SecureContext, pwd)
		if err != nil {
			return nil, err
		}
		defer crypto.CloseClient(sec)

		esec, err := sec.GetEnrollmentCertificateHandler()
		esecImpl, err := esec.GetTransactionHandler()

		if IsSignEnabled() {
			binding, err := esecImpl.GetBinding()
			if err != nil {
				return nil, err
			}

			chaincodeInputRaw, err := proto.Marshal(spec.CtorMsg)
			if err != nil {
				return nil, err
			}

			// Access control. Administrator signs chaincodeInputRaw || binding to confirm his identity
			sigma, err := esec.Sign(append(chaincodeInputRaw, binding...))
			if err != nil {
				return nil, err
			}

			spec.Metadata = sigma
		}

		// remove the security context since we are no longer need it down stream
		spec.SecureContext = ""

		if nil != err {
			return nil, err
		}

		tx, err = esecImpl.NewChaincodeDeployTransaction(chaincodeDeploymentSpec, transID, spec.Attributes...)
		if nil != err {
			return nil, err
		}
	} else {
		tx, err = pb.NewChaincodeDeployTransaction(chaincodeDeploymentSpec, transID)
		if err != nil {
			return nil, fmt.Errorf("Error deploying chaincode: %s ", err)
		}
	}

	resp := sendTransactionsToPeer(getPeerAddress(), tx, true)
	if resp.Status == pb.Response_FAILURE {
		err = fmt.Errorf(string(resp.Msg))
	}

	return chaincodeDeploymentSpec, err
}

/**
 * Description Invoke performs the supplied invocation on the specified chaincode through a transaction
 *
 * @param chaincodeInvocationSpec
 * @param pwd
 * @return pb.Response, error
 */
func invoke(chaincodeInvocationSpec *pb.ChaincodeInvocationSpec, pwd []byte) (*pb.Response, error) {
	return invokeOrQuery(context.Background(), chaincodeInvocationSpec,
		chaincodeInvocationSpec.ChaincodeSpec.Attributes, true, pwd)
}

/**
* Description Query performs the supplied query on the specified chaincode through a transaction
*
* @param chaincodeInvocationSpec
* @return pb.Response, error
 */
func query(chaincodeInvocationSpec *pb.ChaincodeInvocationSpec) (*pb.Response, error) {
	return invokeOrQuery(context.Background(), chaincodeInvocationSpec,
		chaincodeInvocationSpec.ChaincodeSpec.Attributes, false, nil)
}

/**
* Description invokeOrQuery performs the supplied invokeOrQuery on the specified chaincode through a transaction
*
* @param ctx
* @param chaincodeInvocationSpec
* @param attributes
* @param invoke
* @return pb.Response, error
 */
func invokeOrQuery(ctx context.Context, chaincodeInvocationSpec *pb.ChaincodeInvocationSpec,
	attributes []string, invoke bool, pwd []byte) (*pb.Response, error) {
	if chaincodeInvocationSpec.ChaincodeSpec.ChaincodeID.Name == "" {
		return nil, fmt.Errorf("name not given for invoke/query")
	}

	// Now create the Transactions message and send to Peer.
	var customIDgenAlg = strings.ToLower(chaincodeInvocationSpec.IdGenerationAlg)
	var id string
	var generr error
	if invoke {
		if customIDgenAlg != "" {
			ctorbytes, merr := asn1.Marshal(*chaincodeInvocationSpec.ChaincodeSpec.CtorMsg)
			if merr != nil {
				return nil, fmt.Errorf("Error marshalling constructor: %s", merr)
			}
			id, generr = util.GenerateIDWithAlg(customIDgenAlg, ctorbytes)
			if generr != nil {
				return nil, generr
			}
		} else {
			id = util.GenerateUUID()
		}
	} else {
		id = util.GenerateUUID()
	}
	ClientSdkLogger.Infof("Transaction ID: %v", id)
	var transaction *pb.Transaction
	var err error
	var sec crypto.Client
	var esecImpl crypto.TransactionHandler
	if isSecurityEnabled() {
		if invoke {
			sec, err = crypto.InitClient(chaincodeInvocationSpec.ChaincodeSpec.SecureContext, pwd)
			if err != nil {
				ClientSdkLogger.Debugf("InitClient error : %s", err.Error())
				return nil, err
			}
		} else {
			sec, err = crypto.InitClient(GetRegistrarId(), nil)
		}

		defer crypto.CloseClient(sec)

		esec, err := sec.GetEnrollmentCertificateHandler()
		esecImpl, err = esec.GetTransactionHandler()

		if invoke && IsSignEnabled() {
			binding, err := esecImpl.GetBinding()
			if err != nil {
				return nil, err
			}

			chaincodeInputRaw, err := proto.Marshal(chaincodeInvocationSpec.ChaincodeSpec.CtorMsg)
			if err != nil {
				return nil, err
			}

			// Access control. Administrator signs chaincodeInputRaw || binding to confirm his identity
			sigma, err := esec.Sign(append(chaincodeInputRaw, binding...))
			if err != nil {
				return nil, err
			}

			chaincodeInvocationSpec.ChaincodeSpec.Metadata = sigma
		}

		// remove the security context since we are no longer need it down stream
		chaincodeInvocationSpec.ChaincodeSpec.SecureContext = ""
		if nil != err {
			return nil, err
		}
	}

	transaction, err = createExecTx(chaincodeInvocationSpec, attributes, id, invoke, esecImpl)
	if err != nil {
		return nil, err
	}

	resp := sendTransactionsToPeer(getPeerAddress(), transaction, false)
	if resp.Status == pb.Response_FAILURE {
		err = fmt.Errorf(string(resp.Msg))
	} else {
		if !invoke && nil != sec && viper.GetBool("security.privacy") {
			if resp.Msg, err = sec.DecryptQueryResult(transaction, resp.Msg); nil != err {
				ClientSdkLogger.Errorf("Failed decrypting query transaction result %s", string(resp.Msg[:]))
				//resp = &pb.Response{Status: pb.Response_FAILURE, Msg: []byte(err.Error())}
			}
		}
	}
	return resp, err
}

func createExecTx(spec *pb.ChaincodeInvocationSpec, attributes []string, uuid string, invokeTx bool, sec crypto.TransactionHandler) (*pb.Transaction, error) {
	var tx *pb.Transaction
	var err error

	//TODO What should we do with the attributes
	if nil != sec {
		if invokeTx {
			tx, err = sec.NewChaincodeExecute(spec, uuid, attributes...)
		} else {
			tx, err = sec.NewChaincodeQuery(spec, uuid, attributes...)
		}
		if nil != err {
			return nil, err
		}
	} else {
		var t pb.Transaction_Type
		if invokeTx {
			t = pb.Transaction_CHAINCODE_INVOKE
		} else {
			t = pb.Transaction_CHAINCODE_QUERY
		}
		tx, err = pb.NewChaincodeExecute(spec, uuid, t)
		if nil != err {
			return nil, err
		}
	}
	return tx, nil
}

func sendTransactionsToPeer(peerAddress string, transaction *pb.Transaction, isdeploy bool) (response *pb.Response) {
	var creds credentials.TransportCredentials
	tslEnabled := isTLSEnabled()
	if tslEnabled {
		pem, err := filepath.Abs(getClientCert())
		if err != nil {
			log.Fatalf("Failed to get pem file %v", err)
		}
		creds, err = credentials.NewClientTLSFromFile(pem, getServerName())
		if err != nil {
			log.Fatalf("Failed to create TLS credentials %v", err)
		}
	}
	conn, err := comm.NewClientConnectionWithAddress(peerAddress, false, tslEnabled, creds)
	if err != nil {
		return &pb.Response{Status: pb.Response_FAILURE, Msg: []byte(fmt.Sprintf("Error creating client to peer address=%s:  %s", peerAddress, err))}
	}
	defer conn.Close()
	serverClient := pb.NewPeerClient(conn)
	ClientSdkLogger.Debugf("Sending TX to Peer: %s", peerAddress)
	ctx := context.Background()
	if !isdeploy {
		ctx, _ = context.WithTimeout(context.Background(), invokeOrQueryTimeout)
	}
	response, err = serverClient.ProcessTransaction(ctx, transaction)
	if err != nil {
		return &pb.Response{Status: pb.Response_FAILURE, Msg: []byte(fmt.Sprintf("Error calling ProcessTransaction on remote peer at address=%s:  %s", peerAddress, err))}
	}
	return response
}

// get chaincode bytes
func getChaincodeBytes(context context.Context, spec *pb.ChaincodeSpec) (*pb.ChaincodeDeploymentSpec, error) {
	ClientSdkLogger.Debugf("Received build request for chaincode spec: %v", spec)
	mode := getDevMode()
	var codePackageBytes []byte
	if mode != "dev" {
		var err error
		if err = checkSpec(spec); err != nil {
			return nil, err
		}

		codePackageBytes, err = container.GetChaincodePackageBytes(spec)
		if err != nil {
			err = fmt.Errorf("Error getting chaincode package bytes: %s", err)
			ClientSdkLogger.Error(fmt.Sprintf("%s", err))
			return nil, err
		}
	}
	chaincodeDeploymentSpec := &pb.ChaincodeDeploymentSpec{ChaincodeSpec: spec, CodePackage: codePackageBytes}
	return chaincodeDeploymentSpec, nil
}

// CheckSpec to see if chaincode resides within current package capture for language.
func checkSpec(spec *pb.ChaincodeSpec) error {
	// Don't allow nil value
	if spec == nil {
		return errors.New("Expected chaincode specification, nil received")
	}

	platform, err := platforms.Find(spec.Type)
	if err != nil {
		return fmt.Errorf("Failed to determine platform type: %s", err)
	}

	return platform.ValidateSpec(spec)
}

func LoadPrivateKeyRaw(enrollId string, alias string, pwd []byte) ([]byte, error) {
	path := GetPathForAlias(enrollId, alias)
	ClientSdkLogger.Debugf("Loading private key [%s] at [%s]...", alias, path)

	raw, err := ioutil.ReadFile(path)
	if err != nil {
		ClientSdkLogger.Errorf("Failed loading private key [%s]: [%s].", alias, err.Error())

		return nil, err
	}
	return raw, nil
}

func LoadPrivateKey(enrollId string, alias string, pwd []byte) (interface{}, error) {
	raw, err := LoadPrivateKeyRaw(enrollId, alias, pwd)
	if err != nil {
		return nil, err
	}

	privateKey, err := primitives.PEMtoPrivateKey(raw, pwd)
	if err != nil {
		ClientSdkLogger.Errorf("Failed parsing private key [%s]: [%s].", alias, err.Error())

		return nil, err
	}

	return privateKey, nil
}

func ecdsaSignWithEnrollmentKey(enrollId string, msg []byte, pwd []byte) (*big.Int, *big.Int, error) {
	enrollPrivkey, err := LoadPrivateKey(enrollId, GetEnrollmentKeyFilename(), pwd)
	if err != nil {
		return nil, nil, err
	}
	return primitives.ECDSASignDirect(enrollPrivkey, msg)
}

func getBinding(enrollId string) ([]byte, error) {
	nonce, err := createTransactionNonce()
	if err != nil {
		ClientSdkLogger.Errorf("Failed createTransactionNonce [%s]", err)

		return nil, err
	}

	certRaw, err := LoadCertX509CertRaw(enrollId)

	binding := primitives.Hash(append(certRaw, nonce...))

	return utils.Clone(binding), nil
}

func createTransactionNonce() ([]byte, error) {
	nonce, err := primitives.GetRandomNonce()
	if err != nil {
		ClientSdkLogger.Errorf("Failed creating nonce [%s].", err.Error())
		return nil, err
	}

	return nonce, err
}

func sign(msg []byte, enrollId string, pwd []byte) ([]byte, error) {
	enrollPrivkey, _ := LoadPrivateKey(enrollId, GetEnrollmentKeyFilename(), pwd)
	return primitives.ECDSASign(enrollPrivkey, msg)
}

// func getMetaData(chainCodeInput *pb.ChaincodeInput, enrollId string) ([]byte, error) {
// 	_, err := getBinding(enrollId)
// 	if err != nil {
// 		return nil, err
// 	}

// 	chaincodeInputRaw, err := proto.Marshal(chainCodeInput)
// 	if err != nil {
// 		return nil, err
// 	}

// 	// Access control. Administrator signs chaincodeInputRaw || binding to confirm his identity
// 	sigma, err := sign(chaincodeInputRaw, enrollId)
// 	if err != nil {
// 		return nil, err
// 	}

// 	return sigma, nil
// }

func getChaincodeInputByTxId(txId string) (*pb.ChaincodeInput, error) {

	peerAddress := getPeerAddress()
	conn, err := comm.NewClientConnectionWithAddress(peerAddress, false, false, nil)
	if err != nil {
		ClientSdkLogger.Errorf("getChaincodeInputByTxId Error creating client to peer address=%s: %s", peerAddress, err)
		return nil, err
	}
	defer conn.Close()

	transactionID := &pb.TransactionID{Txid: txId}

	openchainClient := pb.NewOpenchainClient(conn)
	input, err := openchainClient.GetChaincodeInputByID(context.Background(), transactionID)
	if err != nil {
		ClientSdkLogger.Errorf("getChaincodeInputByTxId get chaincode input error :%s", err)
		return nil, err
	}

	return input, nil
}

// GetStateByBlockID
func GetStateByBlockID(blockNumber uint64, chaincodeID string, key string) ([]byte, error) {
	peerAddress := getPeerAddress()
	conn, err := comm.NewClientConnectionWithAddress(peerAddress, false, false, nil)
	defer conn.Close()
	if err != nil {
		ClientSdkLogger.Errorf("GetStateByBlockID Error creating client to peer address=%s: %s", peerAddress, err)
		return nil, err
	}

	queryParam := &pb.StateQueryParam{Number: blockNumber, Ccname: chaincodeID, Statekey: key}
	openchainClient := pb.NewOpenchainClient(conn)
	val, err := openchainClient.GetStateByBlockID(context.Background(), queryParam)
	if err != nil {
		ClientSdkLogger.Errorf("GetStateByBlockID get state value error :%s", err)
		return nil, err
	}

	return []byte(val.Statevalue), nil
}
