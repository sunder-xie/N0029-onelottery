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
	"fmt"
	"io/ioutil"
	"os"
	"strings"
	"sync"

	"github.com/spf13/viper"

	"github.com/golang/protobuf/proto"
	"github.com/hyperledger/fabric/core/chaincode/shim"
	membersrvc "github.com/hyperledger/fabric/membersrvc/protos"
	pb "github.com/hyperledger/fabric/protos"
)

const (
	SUCCESS      = 0
	PARA_ERR     = -1
	INTERNAL_ERR = -2

	DEPLOY_SECURE_NOT_LOGIN  = -3
	LOGIN_USER_ALREADY_EXIST = -3

	REGISTR_ALREADY_EXIST          = -4 //The user has already registered.
	REGISTR_ALREADY_EXIST_IN_CHAIN = -5 //The user has already registered.
	ENROLL_ALREADY_EXIST           = -6 //The user has already enrolled.

	REGISTR_ROLE_NOT_DEFINED = -7

	NETWORK_ERR = -8 //Invoke or query err of not connect peer or timeout.

	ZXCoinTable  = "ZxCoin"
	LotteryTable = "Lottery"
)

const (
	RoleClient = iota
	RoleDeveloper
	RoleVoter
)

var (
	registerUserMap map[string]string = make(map[string]string)
	regUserMapMutex sync.Mutex
)

/*************************** chaincode input ***************************/
type Args struct {
	Function string `json:"function"`
	Payload  string `json:"payload"`
}

/**
 * Description
 *
 * @param configPath      The client_sdk.yaml dir
 * @param fileSystemPath  The location where file will put in.
 * @return ret
 */
func FabricSdkInit(configPath, fileSystemPath, configFile string, getWebAppAdmin bool) (ret int) {
	if configPath == "" || fileSystemPath == "" || configFile == "" {
		ClientSdkLogger.Error("configPath or fileSystemPath can not be empty!")
		return PARA_ERR
	}

	err := initClientSdk(configPath, fileSystemPath, configFile, getWebAppAdmin)
	if err != nil {
		ClientSdkLogger.Errorf("FabricSdkInit failed:%s", err.Error())
		return INTERNAL_ERR
	}

	return SUCCESS
}

/**
* Description start a damemon to recieve the event
*
* @param eventAddress
* @param isListenToRejections
* @param chainCodeId
* @param callback
* @return ret
 */
func FabricSdkStartDaemon(eventAddress string, isListenToRejections bool, chainCodeId string, callback EventCallBack) (ret int) {
	return startDaemon(eventAddress, isListenToRejections, chainCodeId, callback)
}

/**
* Description stop the damemon to recieve the event
*
* @return
 */
func FabricStopDaemon() {
	stopDaemon()
}

/**
 * Description Register a user
 *
 * @param userId The account of a user to register.
 * @param affiliation The affiliation of a user to register.
 * @param role The role of the user to register.
 * @return ret,enrollPwd
 */
func FabricSdkRegister(role int, userId string, affiliation string) (ret int, enrollPwd string) {
	if userId == "" || affiliation == "" {
		ClientSdkLogger.Error("userId or affiliation can not be empty!")
		return PARA_ERR, ""
	}

	// If the user is already register, return
	if _, err := os.Stat(GetPathForAlias(userId, getRegisterIdFileName())); err == nil {
		ClientSdkLogger.Infof("FabricSdkRegister register,the user already exist:%s", userId)
		return REGISTR_ALREADY_EXIST, ""
	}

	registerUserReq := new(membersrvc.RegisterUserReq)
	identity := new(membersrvc.Identity)
	identity.Id = userId
	registerUserReq.Id = identity
	registerUserReq.Attributes = nil
	registerUserReq.Affiliation = affiliation
	registerUserReq.Registrar = nil
	registerUserReq.Sig = nil

	switch role {
	case RoleClient:
		registerUserReq.Role = membersrvc.Role_CLIENT
	case RoleDeveloper:
		registerUserReq.Role = membersrvc.Role_DEVELOPER
	case RoleVoter:
		registerUserReq.Role = membersrvc.Role_VOTER
	default:
		ClientSdkLogger.Errorf("FabricSdkRegister register role do not exsit! role: %d", role)
		return REGISTR_ROLE_NOT_DEFINED, ""
	}

	enrollPwd, err := register(registerUserReq)
	if err != nil {
		ClientSdkLogger.Infof("FabricSdkRegister register error:%s", err.Error())
		if strings.Contains(err.Error(), "User is already registered") {
			return REGISTR_ALREADY_EXIST_IN_CHAIN, ""
		}
		return INTERNAL_ERR, ""
	}

	if _, err := os.Stat(getRawsPath(userId)); err != nil {
		if os.IsNotExist(err) {
			// Directory does not exist, create it
			if err := os.MkdirAll(getRawsPath(userId), 0755); err != nil {
				ClientSdkLogger.Panicf("FabricSdkRegister,Fatal error when creating %s directory: %s\n", getRawsPath(userId), err)
				return INTERNAL_ERR, ""
			}
		} else {
			// Unexpected error
			ClientSdkLogger.Panicf("FabricSdkRegister,Fatal error on os.Stat of %s directory: %s\n", getRawsPath(userId), err)
			return INTERNAL_ERR, ""
		}
	}

	ClientSdkLogger.Infof("Storing register token for user '%s'.\n", userId)
	err = ioutil.WriteFile(GetPathForAlias(userId, getRegisterIdFileName()), []byte(userId), 0755)

	if err != nil {
		ClientSdkLogger.Panicf("Fatal error when storing client register token: %s\n", err)
		return INTERNAL_ERR, ""
	}

	return SUCCESS, enrollPwd
}

/**
 * Description enroll a user
 *
 * @param enrollID
 * @param enrollPWD
 * @param pwd
 * @return ret
 */
func FabricSdkEnroll(enrollID string, enrollPWD string, pwd []byte) (ret int) {
	if enrollID == "" || enrollPWD == "" {
		ClientSdkLogger.Error("enrollID or enrollPWD can not be empty!")
		return PARA_ERR
	}

	// If the user is already enroll, return
	if _, err := os.Stat(GetPathForAlias(enrollID, getEnrollIdFileName())); err == nil {
		ClientSdkLogger.Infof("FabricSdkEnroll enroll,the user already exist:%s", enrollID)
		return ENROLL_ALREADY_EXIST
	}

	err := enroll(enrollID, enrollPWD, pwd)
	if err != nil {
		ClientSdkLogger.Infof("FabricSdkEnroll enroll error:%s", err.Error())
		return INTERNAL_ERR
	}

	return SUCCESS
}

/**
* Description Register and then enroll a user
*
* @param userId The account of a user to register.
* @param affiliation The affiliation of a user to register.
* @param pwd
* @param role The role of the user to register.
* @return ret
 */
func FabricSdkRegisterAndEnroll(role int, userId string, affiliation string, pwd []byte) (ret int) {
	if userId == "" || affiliation == "" {
		ClientSdkLogger.Error("FabricSdkRegisterAndEnroll,userId or affiliation can not be empty!")
		return PARA_ERR
	}

	regUserMapMutex.Lock()
	if userEnrollPwd, ok := registerUserMap[userId]; ok {
		ClientSdkLogger.Info("FabricSdkRegisterAndEnroll find before register user!")
		ret = FabricSdkEnroll(userId, userEnrollPwd, pwd)
		if ret < SUCCESS {
			os.RemoveAll(GetUserDir(userId))
			ClientSdkLogger.Error("FabricSdkRegisterAndEnroll error when call FabricSdkEnroll.")
			regUserMapMutex.Unlock()
			return ENROLL_ALREADY_EXIST
		}

		delete(registerUserMap, userId)
		regUserMapMutex.Unlock()
		return ret
	}
	regUserMapMutex.Unlock()

	// If the user is already enroll,return
	if _, err := os.Stat(GetPathForAlias(userId, getEnrollIdFileName())); err == nil {
		ClientSdkLogger.Infof("FabricSdkRegisterAndEnroll,the user already exist:%s", userId)
		return ENROLL_ALREADY_EXIST
	}

	ret, enrollPwd := FabricSdkRegister(role, userId, affiliation)
	if ret < SUCCESS {
		os.RemoveAll(GetUserDir(userId))
		ClientSdkLogger.Error("FabricSdkRegisterAndEnroll error when call FabricSdkRegister.")
		return ret
	}

	regUserMapMutex.Lock()
	registerUserMap[userId] = enrollPwd
	regUserMapMutex.Unlock()

	ret = FabricSdkEnroll(userId, enrollPwd, pwd)
	if ret < SUCCESS {
		os.RemoveAll(GetUserDir(userId))
		ClientSdkLogger.Error("FabricSdkRegisterAndEnroll error when call FabricSdkEnroll.")
		return ret
	}

	regUserMapMutex.Lock()
	delete(registerUserMap, userId)
	regUserMapMutex.Unlock()

	return ret
}

/**
* Description deploy chaincode
*
* @param chaincodePath: If in dev mode,chaincodepath is the name of chaincode,else is the path of the chaincode
* @param args:The args to deploy chaincode
* @param secureContext:If security is enabled,the secureContext can not be empty.
* @param pwd
* @return ret,chainCodeId
 */
func FabricSdkDeploy(chaincodePath string, args [][]byte, secureContext string, pwd []byte) (ret int, chainCodeId string) {
	if chaincodePath == "" {
		ClientSdkLogger.Error("FabricSdkDeploy,chaincode path can not be empty!")
		return PARA_ERR, ""
	}
	if args == nil || len(args) == 0 {
		ClientSdkLogger.Error("FabricSdkDeploy,args can not be nil or zero length!")
		return PARA_ERR, ""
	}

	if isSecurityEnabled() {
		if secureContext == "" {
			ClientSdkLogger.Error("FabricSdkDeploy,securedContext can not be empty when security is enabled!")
			return PARA_ERR, ""
		}

		//check the user has already login
		if _, err := os.Stat(GetPathForAlias(secureContext, getEnrollIdFileName())); err != nil {
			ClientSdkLogger.Error("FabricSdkDeploy,the user not log in when security is enabled!")
			return DEPLOY_SECURE_NOT_LOGIN, ""
		}
	}

	// chainCodeInput := make([][]byte, len(args))
	// for i, arg := range args {
	// 	chainCodeInput[i] = []byte(arg)
	// }

	chaincodeInput := &pb.ChaincodeInput{Args: args}
	spec := &pb.ChaincodeSpec{Type: pb.ChaincodeSpec_GOLANG, CtorMsg: chaincodeInput}

	if isSecurityEnabled() {
		spec.SecureContext = secureContext

		// If privacy is enabled, mark chaincode as confidential
		if viper.GetBool("security.privacy") {
			spec.ConfidentialityLevel = pb.ConfidentialityLevel_CONFIDENTIAL
		}
	}
	if viper.GetString("chaincode.mode") == "dev" {
		spec.ChaincodeID = &pb.ChaincodeID{Name: chaincodePath}
	} else {
		spec.ChaincodeID = &pb.ChaincodeID{Path: chaincodePath}
	}

	chainCodeDeploymentSpec, err := deploy(spec, pwd)
	if err != nil {
		ClientSdkLogger.Errorf("FabricSdkDeploy,failed:%s", err.Error())
		return INTERNAL_ERR, ""
	}

	return SUCCESS, chainCodeDeploymentSpec.ChaincodeSpec.ChaincodeID.Name
}

/**
* Description invoke chaincode
*
* @param chaincodeIdName
* @param args:The args to invoke chaincode
* @param secureContext:If security is enabled,the secureContext can not be empty.
* @param pwd
* @return ret
 */
func FabricSdkInvoke(chaincodeIdName string, args [][]byte, secureContext string, pwd []byte) (ret int, txId string) {
	return fabricInvokeOrQuery(chaincodeIdName, args, secureContext, true, pwd)
}

/**
* Description query chaincode
*
* @param chaincodeIdName
* @param args:The args to query chaincode
* @param secureContext:If security is enabled,the secureContext can not be empty.
* @return ret
 */
func FabricSdkQuery(chaincodeIdName string, args [][]byte, secureContext string) (ret int, val string) {
	return fabricInvokeOrQuery(chaincodeIdName, args, secureContext, false, nil)
}

func fabricInvokeOrQuery(chaincodeIdName string, args [][]byte, secureContext string, isInvoke bool, pwd []byte) (ret int, msg string) {
	if chaincodeIdName == "" {
		ClientSdkLogger.Error("FabricSdkInvoke,chaincodeIdName can not be empty!")
		return PARA_ERR, ""
	}
	if args == nil || len(args) == 0 {
		ClientSdkLogger.Error("fabricInvokeOrQuery,args can not be nil or zero length!")
		return PARA_ERR, ""
	}

	if isSecurityEnabled() {
		if secureContext == "" {
			ClientSdkLogger.Error("fabricInvokeOrQuery,securedContext can not be empty when security is enabled!")
			return PARA_ERR, ""
		}

		//check the user has already login
		if _, err := os.Stat(GetPathForAlias(secureContext, getEnrollIdFileName())); err != nil {
			ClientSdkLogger.Error("fabricInvokeOrQuery,the user not log in when security is enabled!")
			return DEPLOY_SECURE_NOT_LOGIN, ""
		}
	}

	// chainCodeInput := make([][]byte, len(args))
	// for i, arg := range args {
	// 	chainCodeInput[i] = []byte(arg)
	// }

	chaincodeInput := &pb.ChaincodeInput{Args: args}
	spec := &pb.ChaincodeSpec{Type: pb.ChaincodeSpec_GOLANG, ChaincodeID: &pb.ChaincodeID{Name: chaincodeIdName},
		CtorMsg: chaincodeInput}

	if isSecurityEnabled() {
		spec.SecureContext = secureContext

		// If privacy is enabled, mark chaincode as confidential
		if viper.GetBool("security.privacy") {
			spec.ConfidentialityLevel = pb.ConfidentialityLevel_CONFIDENTIAL
		}
	}

	invokequeryPayload := &pb.ChaincodeInvocationSpec{ChaincodeSpec: spec}

	var err error
	var response *pb.Response
	if isInvoke {
		response, err = invoke(invokequeryPayload, pwd)
	} else {
		response, err = query(invokequeryPayload)
	}

	if err != nil {
		ClientSdkLogger.Errorf("fabricInvokeOrQuery,failed:%s", err.Error())

		if strings.Contains(err.Error(), "Error calling ProcessTransaction on remote peer at address") {
			return NETWORK_ERR, ""
		}
		return INTERNAL_ERR, ""
	}

	msg = string(response.Msg)

	return SUCCESS, msg
}

// Get ChaincodeInput by transactionID via gRPC server
func FabricSdkGetChaincodeInputByTxId(txId string) (int, string) {
	if txId == "" {
		ClientSdkLogger.Error("FabricSdkGetCCInputByTxId, txId cannot be empty!")
		return PARA_ERR, ""
	}

	input, err := getChaincodeInputByTxId(txId)
	if err != nil {
		ClientSdkLogger.Errorf("FabricSdkGetChaincodeInputByTxId Error:%s", err)
		return INTERNAL_ERR, ""
	}

	if len(input.Args) < 3 {
		ClientSdkLogger.Errorf("FabricSdkGetChaincodeInputByTxId get not enough args:%s", input.Args)
		return INTERNAL_ERR, ""
	}

	//第二个参数如果有值，则原始为用户的证书，传递到上层为用户的hash
	userHash := ""
	userCert := string(input.Args[1])
	if userCert != "" {
		userHash = getPubkeyHashByCert(userCert)
	}

	retMsg := string(input.Args[0]) + "&X&" + userHash + "&X&" + string(input.Args[2])

	return SUCCESS, retMsg
}

/**
 * Description
 *
 * @param blockNumber  block num
 * @param chaincodeID  chaincodeID
 * @param key          primary key
 * @param tableFalg    0:zxcon user info table, 1:lottery table
 * @return ret
 */
// GetStateByBlockID
func FabricSdkGetStateByBlockID(blockNumber uint64, chaincodeID string, key string, tableFalg uint32) (int, shim.Row) {
	var row shim.Row
	realKey := ""
	switch tableFalg {
	case 0:
		realKey = fmt.Sprintf("%d%s%d%s", len(ZXCoinTable), ZXCoinTable, len(key), key)
	case 1:
		realKey = fmt.Sprintf("%d%s%d%s", len(LotteryTable), LotteryTable, len(key), key)
	default:
		ClientSdkLogger.Error("Error table flag not found :%d", tableFalg)
		return PARA_ERR, row
	}

	retVal, err := GetStateByBlockID(blockNumber, chaincodeID, realKey)
	if err != nil || retVal == nil {
		return INTERNAL_ERR, row
	}

	err = proto.Unmarshal(retVal, &row)
	if err != nil {
		ClientSdkLogger.Error("Error unmarshalling row: %s", err)
		return INTERNAL_ERR, row
	}

	return SUCCESS, row
}
