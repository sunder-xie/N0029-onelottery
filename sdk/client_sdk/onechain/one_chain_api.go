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

package onechain

import (
	"crypto/ecdsa"
	"crypto/x509"
	"encoding/json"
	"fmt"
	"sync"
	"time"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	"github.com/hyperledger/fabric/core/crypto/primitives"
	"peersafe.com/onelottery/sdk/client_sdk"

	pb "github.com/hyperledger/fabric/protos"
	"golang.org/x/crypto/sha3"
	"io/ioutil"
)

type OneChainCallBack interface {
	// zxcoin callback
	OneChainTransferCallback(retMsg string, txId string)
	OneChainZxCoinWithdraw(retMsg string, txId string)
	OneChainZxCoinWithdrawRecall(retMsg string, txId string)
	OneChainZxCoinWithdrawFail(retMsg string, txId string)
	OneChainZxCoinWithdrawRemitSucces(retMsg string, txId string)
	OneChainZxCoinWithdrawConfirm(retMsg string, txId string)
	OneChainZxCoinWithdrawAppeal(retMsg string, txId string)
	OneChainZxCoinWithdrawAppealDone(retMsg string, txId string)

	// onechain callback
	OneChainPrizeRuleAddCallback(retMsg string, txId string)
	OneChainPrizeRuleModifyCallback(retMsg string, txId string)
	OneChainPrizeRuleDeleteCallback(retMsg string, txId string)
	// OneChainBallRuleAddCallback(retMsg string, txId string)
	// OneChainBallRuleModifyCallback(retMsg string, txId string)
	// OneChainBallRuleDeleteCallback(retMsg string, txId string)
	OneChainOneLotteryAddCallback(retMsg string, txId string)
	OneChainOneLotteryModifyCallback(retMsg string, txId string)
	OneChainOneLotteryDeleteCallback(retMsg string, txId string)
	OneChainOneLotteryBetCallback(retMsg string, txId string)
	OneChainOneLotteryOpenRewardCallback(retMsg string, txId string)
	OneChainOneLotteryRefundCallback(retMsg string, txId string)

	OneChainDeaemonStartNotify(result int)
	OneChainDeaemonShutDownNotify()
	OneChainTransferNotify(retMsg string, txId string)
	OneChainPrizeRuleAddNotify(retMsg string, txId string)
	OneChainPrizeRuleModifyNotify(retMsg string, txId string)
	OneChainPrizeRuleDeleteNotify(retMsg string, txId string)
	OneChainOneLotteryAddNotify(retMsg string, txId string)
	OneChainOneLotteryModifyNotify(retMsg string, txId string)
	OneChainOneLotteryDeleteNotify(retMsg string, txId string)
	OneChainOneLotteryBetNotify(retMsg string, txId string)
	OneChainOneLotteryOpenRewardNotify(retMsg string, txId string)
	OneChainOneLotteryRefundNotify(retMsg string, txId string)

	OneChainZxCoinWithdrawApplyNotify(retMsg string, txId string)
	OneChainZxCoinWithdrawFailNotify(retMsg string, txId string)
	OneChainZxCoinWithdrawRemitSuccesNotify(retMsg string, txId string)
	OneChainZxCoinWithdrawConfirmNotify(retMsg string, txId string)
	OneChainZxCoinWithdrawAppealDoneNotify(retMsg string, txId string)
}

type invokeTXType int

var normalFee NormalFee

const (
	RuleType_Prize string = "PrizeRule" // prize rule
	RuleType_Ball  string = "BallRule"  //ball rule

	// zxcoin
	invokeTx_Type_Transfer_Account invokeTXType = 1
	invokeTx_Type_SetUserName      invokeTXType = 2

	// onechain
	invokeTx_Type_Prize_Rule_Add          invokeTXType = 3
	invokeTx_Type_Prize_Rule_Modify       invokeTXType = 4
	invokeTx_Type_Prize_Rule_Delete       invokeTXType = 5
	invokeTx_Type_Ball_Rule_Add           invokeTXType = 6
	invokeTx_Type_Ball_Rule_Modify        invokeTXType = 7
	invokeTx_Type_Ball_Rule_Delete        invokeTXType = 8
	invokeTx_Type_One_Lottery_Add         invokeTXType = 9
	invokeTx_Type_One_Lottery_Modify      invokeTXType = 10
	invokeTx_Type_One_Lottery_Delete      invokeTXType = 11
	invokeTx_Type_One_Lottery_Bet         invokeTXType = 12
	invokeTx_Type_One_Lottery_Open_Reward invokeTXType = 13
	invokeTx_Type_One_Lottery_Refund      invokeTXType = 14

	//zxcoin withdraw
	invokeTx_Type_ZxCoinWithdraw            invokeTXType = 15
	invokeTx_Type_ZxCoinWithdrawRecall      invokeTXType = 16
	invokeTx_Type_ZxCoinWithdrawFail        invokeTXType = 18
	invokeTx_Type_ZxCoinWithdrawRemitSucces invokeTXType = 19
	invokeTx_Type_ZxCoinWithdrawConfirm     invokeTXType = 20
	invokeTx_Type_ZxCoinWithdrawAppeal      invokeTXType = 21
	invokeTx_Type_ZxCoinWithdrawAppealDone  invokeTXType = 22

	// zxcoin interface
	deployTx_Func_ZXCoin_Init         string = "zxCoinInit"
	invokeTx_Func_Transfer_Account    string = "zxCoinTransfer"
	invokeTx_Func_Query_Balance       string = "zxCoinAccountBalance"
	invokeTx_Func_ZXCoin_Get_UserInfo string = "zxCoinGetUserInfo"
	invokeTx_Func_ZXCoin_Set_UserName string = "zxCoinSetUserName"

	// onechain interface
	invokeTx_Func_Prize_Rule_Add               string = "oneLotteryPrizeRuleAdd"
	invokeTx_Func_Prize_Rule_Modify            string = "oneLotteryPrizeRuleEdit"
	invokeTx_Func_Prize_Rule_Delete            string = "oneLotteryPrizeRuleDelete"
	invokeTx_Func_Prize_Rule_Query             string = "oneLotteryPrizeRuleQuery"
	invokeTx_Func_Ball_Rule_Add                string = "oneLotteryBallRuleAdd"
	invokeTx_Func_Ball_Rule_Modify             string = "oneLotteryBallRuleEdit"
	invokeTx_Func_Ball_Rule_Delete             string = "oneLotteryBallRuleDelete"
	invokeTx_Func_Ball_Rule_Query              string = "oneLotteryBallRuleQuery"
	invokeTx_Func_One_Lottery_Add              string = "oneLotteryAdd"
	invokeTx_Func_One_Lottery_Modify           string = "oneLotteryEdit"
	invokeTx_Func_One_Lottery_Delete           string = "oneLotteryDelete"
	invokeTx_Func_One_Lottery_Query            string = "oneLotteryQuery"
	invokeTx_Func_One_Lottery_History_Query    string = "oneLotteryHistoryQuery"
	invokeTx_Func_One_Lottery_OldHistory_Query string = "oneLotteryOldHistoryQuery"
	invokeTx_Func_One_Lottery_Bet              string = "oneLotteryBet"
	invokeTx_Func_One_Lottery_Bet_Query        string = "oneLotteryBetQuery"
	invokeTx_Func_One_Lottery_Bet_Over         string = "oneLotteryBetOver"
	invokeTx_Func_One_Lottery_Refund           string = "oneLotteryRefund"
	invokeTx_Func_One_Lottery_GetTicketNumbers string = "oneLotteryGetTicketNumbers"
	invokeTx_Func_Withdraw                     string = "zxCoinWithdraw"
	invokeTx_Func_Withdraw_Fail                string = "zxCoinWithdrawFail"
	invokeTx_Func_Withdraw_Remit_Succes        string = "zxCoinWithdrawRemitSucces"
	invokeTx_Func_Withdraw_Remit_Confirm       string = "zxCoinWithdrawConfirm"
	invokeTx_Func_Withdraw_Appeal              string = "zxCoinWithdrawAppeal"
	invokeTx_Func_Withdraw_Appeal_Done         string = "zxCoinWithdrawAppealDone"
)

var (
	globalCallBack OneChainCallBack
	invokeTXMap    map[string]invokeTXType = make(map[string]invokeTXType)
	chainCodeId    string
	txMapMutex     sync.Mutex

	//current user id
	CurrentUserId string = ""
	batch         int64
	IsClient      bool = false
)

//为了临时使用命令行测试，正式发布去掉
func SetChainCodeId(chainCodeId string) {
	chainCodeId = chainCodeId
}

func init() {
	normalFee = NormalFee{
		ModefyLotteryFee: 100,
		CreateLotteryFee: 10000,
		TransferFee:      100,
	}
}

/**
 * Description
 *
 * @param configPath      The client_sdk.yaml dir
 * @param fileSystemPath  The location where file will put in.
 * @return ret
 */
func OneChainInit(configPath, fileSystemPath, configFile string, getWebAppAdmin bool) (ret int) {
	if configPath == "" || fileSystemPath == "" {
		client_sdk.ClientSdkLogger.Error("OneChainInit,configPath or fileSystmePath may empty!")
		return client_sdk.PARA_ERR
	}

	ret = client_sdk.FabricSdkInit(configPath, fileSystemPath, configFile, getWebAppAdmin)

	chainCodeId = client_sdk.GetChainCodeIdName()
	client_sdk.ClientSdkLogger.Infof("OneChainInit,chaincode id name:%s", chainCodeId)
	return ret
}

func OneChainDaemon(callBack OneChainCallBack) (ret int) {
	if callBack != nil {
		globalCallBack = callBack
	} else {
		client_sdk.ClientSdkLogger.Error("OneChainDaemon fail,the callback is nil!")
		return client_sdk.INTERNAL_ERR
	}

	eventAddress := client_sdk.GetListenEventAddress()

	eventCall := func(eventType client_sdk.EventType, event interface{}) {
		switch eventType {
		case client_sdk.Event_Type_Block:
			client_sdk.ClientSdkLogger.Info("Recieve Event_Type_Block")
		case client_sdk.Event_Type_Rejection:
			client_sdk.ClientSdkLogger.Info("Recieve Event_Type_Rejection")
			processEventRejection(event)
		case client_sdk.Event_Type_ChainCode:
			client_sdk.ClientSdkLogger.Info("Recieve Event_Type_ChainCode")
			processEventChainCode(event)
		case client_sdk.Event_Type_Daemon_Start_Suc:
			client_sdk.ClientSdkLogger.Info("Daemon start success")
			globalCallBack.OneChainDeaemonStartNotify(0)
		case client_sdk.Event_Type_Daemon_Start_Fail:
			client_sdk.ClientSdkLogger.Info("Daemon start fail")
			globalCallBack.OneChainDeaemonStartNotify(-1)
		case client_sdk.Event_Type_Daemon_Shut_down:
			client_sdk.ClientSdkLogger.Info("Daemon shut down")
			globalCallBack.OneChainDeaemonShutDownNotify()
		}
	}

	ret = client_sdk.FabricSdkStartDaemon(eventAddress, true, chainCodeId, eventCall)

	return ret
}

func OneChainDaemonStop() {
	client_sdk.FabricStopDaemon()
}

func processEventChainCode(event interface{}) {
	chainCodeEvent, ok := event.(pb.ChaincodeEvent)
	if ok {
		txMapMutex.Lock()
		if transacType, ok := invokeTXMap[chainCodeEvent.TxID]; ok {
			client_sdk.ClientSdkLogger.Info("The transaction has find!")
			delete(invokeTXMap, chainCodeEvent.TxID)
			txMapMutex.Unlock()

			switch transacType {
			// zxcoin callback
			case invokeTx_Type_Transfer_Account:
				globalCallBack.OneChainTransferCallback(string(chainCodeEvent.Payload), chainCodeEvent.TxID)
			case invokeTx_Type_ZxCoinWithdraw:
				globalCallBack.OneChainZxCoinWithdraw(string(chainCodeEvent.Payload), chainCodeEvent.TxID)
			case invokeTx_Type_ZxCoinWithdrawRecall:
				if !IsClient {
					globalCallBack.OneChainZxCoinWithdrawRecall(string(chainCodeEvent.Payload), chainCodeEvent.TxID)
				}
			case invokeTx_Type_ZxCoinWithdrawFail:
				globalCallBack.OneChainZxCoinWithdrawFail(string(chainCodeEvent.Payload), chainCodeEvent.TxID)
			case invokeTx_Type_ZxCoinWithdrawRemitSucces:
				globalCallBack.OneChainZxCoinWithdrawRemitSucces(string(chainCodeEvent.Payload), chainCodeEvent.TxID)
			case invokeTx_Type_ZxCoinWithdrawConfirm:
				globalCallBack.OneChainZxCoinWithdrawConfirm(string(chainCodeEvent.Payload), chainCodeEvent.TxID)
			case invokeTx_Type_ZxCoinWithdrawAppeal:
				globalCallBack.OneChainZxCoinWithdrawAppeal(string(chainCodeEvent.Payload), chainCodeEvent.TxID)
			case invokeTx_Type_ZxCoinWithdrawAppealDone:
				globalCallBack.OneChainZxCoinWithdrawAppealDone(string(chainCodeEvent.Payload), chainCodeEvent.TxID)

				// onechain callback
			case invokeTx_Type_Prize_Rule_Add:
				globalCallBack.OneChainPrizeRuleAddCallback(string(chainCodeEvent.Payload), chainCodeEvent.TxID)
			case invokeTx_Type_Prize_Rule_Modify:
				globalCallBack.OneChainPrizeRuleModifyCallback(string(chainCodeEvent.Payload), chainCodeEvent.TxID)
			case invokeTx_Type_Prize_Rule_Delete:
				globalCallBack.OneChainPrizeRuleDeleteCallback(string(chainCodeEvent.Payload), chainCodeEvent.TxID)
			// case invokeTx_Type_Ball_Rule_Add:
			//globalCallBack.OneChainBallRuleAddCallback(string(chainCodeEvent.Payload), chainCodeEvent.TxID)
			// case invokeTx_Type_Ball_Rule_Modify:
			//globalCallBack.OneChainBallRuleModifyCallback(string(chainCodeEvent.Payload), chainCodeEvent.TxID)
			// case invokeTx_Type_Ball_Rule_Delete:
			//globalCallBack.OneChainBallRuleDeleteCallback(string(chainCodeEvent.Payload), chainCodeEvent.TxID)
			case invokeTx_Type_One_Lottery_Add:
				globalCallBack.OneChainOneLotteryAddCallback(string(chainCodeEvent.Payload), chainCodeEvent.TxID)
			case invokeTx_Type_One_Lottery_Modify:
				globalCallBack.OneChainOneLotteryModifyCallback(string(chainCodeEvent.Payload), chainCodeEvent.TxID)
			case invokeTx_Type_One_Lottery_Delete:
				globalCallBack.OneChainOneLotteryDeleteCallback(string(chainCodeEvent.Payload), chainCodeEvent.TxID)
			case invokeTx_Type_One_Lottery_Bet:
				globalCallBack.OneChainOneLotteryBetCallback(string(chainCodeEvent.Payload), chainCodeEvent.TxID)
			case invokeTx_Type_One_Lottery_Open_Reward:
				globalCallBack.OneChainOneLotteryOpenRewardCallback(string(chainCodeEvent.Payload), chainCodeEvent.TxID)
			case invokeTx_Type_One_Lottery_Refund:
				globalCallBack.OneChainOneLotteryRefundCallback(string(chainCodeEvent.Payload), chainCodeEvent.TxID)
			}
			// fmt.Println(transacType, len(invokeTXMap))
		} else {
			txMapMutex.Unlock()
			client_sdk.ClientSdkLogger.Info("The transaction not find,is notification!")

			switch {
			case chainCodeEvent.EventName == invokeTx_Func_Transfer_Account:
				response := &ZxCoinTransferResponse{}
				err := json.Unmarshal(chainCodeEvent.Payload, response)
				if err != nil {
					client_sdk.ClientSdkLogger.Infof("processEventChainCode,parse transfer response error:%s", err.Error())
				} else {
					if response.Code == 0 {
						_, curUserHash := OneChainGetPubkeyHash(CurrentUserId)
						if curUserHash == response.Data.Oppisite || curUserHash == response.Data.Owner {
							client_sdk.ClientSdkLogger.Info("processEventChainCode,recieve other transfer to me or me transfer to other!")
							globalCallBack.OneChainTransferNotify(string(chainCodeEvent.Payload), chainCodeEvent.TxID)
						}
					}
				}

			case chainCodeEvent.EventName == invokeTx_Func_Prize_Rule_Add:
				globalCallBack.OneChainPrizeRuleAddNotify(string(chainCodeEvent.Payload), chainCodeEvent.TxID)
			case chainCodeEvent.EventName == invokeTx_Func_Prize_Rule_Modify:
				globalCallBack.OneChainPrizeRuleModifyNotify(string(chainCodeEvent.Payload), chainCodeEvent.TxID)
			case chainCodeEvent.EventName == invokeTx_Func_Prize_Rule_Delete:
				globalCallBack.OneChainPrizeRuleDeleteNotify(string(chainCodeEvent.Payload), chainCodeEvent.TxID)
			case chainCodeEvent.EventName == invokeTx_Func_One_Lottery_Add:
				globalCallBack.OneChainOneLotteryAddNotify(string(chainCodeEvent.Payload), chainCodeEvent.TxID)
			case chainCodeEvent.EventName == invokeTx_Func_One_Lottery_Modify:
				globalCallBack.OneChainOneLotteryModifyNotify(string(chainCodeEvent.Payload), chainCodeEvent.TxID)
			case chainCodeEvent.EventName == invokeTx_Func_One_Lottery_Delete:
				globalCallBack.OneChainOneLotteryDeleteNotify(string(chainCodeEvent.Payload), chainCodeEvent.TxID)
			case chainCodeEvent.EventName == invokeTx_Func_One_Lottery_Bet:
				globalCallBack.OneChainOneLotteryBetNotify(string(chainCodeEvent.Payload), chainCodeEvent.TxID)
			case chainCodeEvent.EventName == invokeTx_Func_One_Lottery_Bet_Over:
				globalCallBack.OneChainOneLotteryOpenRewardNotify(string(chainCodeEvent.Payload), chainCodeEvent.TxID)
			case chainCodeEvent.EventName == invokeTx_Func_One_Lottery_Refund:
				globalCallBack.OneChainOneLotteryRefundNotify(string(chainCodeEvent.Payload), chainCodeEvent.TxID)
			case chainCodeEvent.EventName == invokeTx_Func_Withdraw:
				if isCurrentWithdrawInfo(chainCodeEvent.Payload) {
					globalCallBack.OneChainZxCoinWithdrawApplyNotify(string(chainCodeEvent.Payload), chainCodeEvent.TxID)
				}
			case chainCodeEvent.EventName == invokeTx_Func_Withdraw_Fail:
				if isCurrentWithdrawInfo(chainCodeEvent.Payload) {
					globalCallBack.OneChainZxCoinWithdrawFailNotify(string(chainCodeEvent.Payload), chainCodeEvent.TxID)
				}
			case chainCodeEvent.EventName == invokeTx_Func_Withdraw_Remit_Succes:
				if isCurrentWithdrawInfo(chainCodeEvent.Payload) {
					globalCallBack.OneChainZxCoinWithdrawRemitSuccesNotify(string(chainCodeEvent.Payload), chainCodeEvent.TxID)
				}
			case chainCodeEvent.EventName == invokeTx_Func_Withdraw_Remit_Confirm:
				if isCurrentWithdrawInfo(chainCodeEvent.Payload) {
					globalCallBack.OneChainZxCoinWithdrawConfirmNotify(string(chainCodeEvent.Payload), chainCodeEvent.TxID)
				}
			case chainCodeEvent.EventName == invokeTx_Func_Withdraw_Appeal_Done:
				if isCurrentWithdrawInfo(chainCodeEvent.Payload) {
					globalCallBack.OneChainZxCoinWithdrawAppealDoneNotify(string(chainCodeEvent.Payload), chainCodeEvent.TxID)
				}
			}
		}
	} else {
		client_sdk.ClientSdkLogger.Info("The transaction not convert to chaincode event!")
	}
}

func isCurrentWithdrawInfo(payload []byte) bool {
	userInfo := WithdrawUserInfo{}
	response := ZxCoinResponse{Data: &userInfo}
	err := json.Unmarshal(payload, &response)
	if err != nil {
		client_sdk.ClientSdkLogger.Infof("processEventChainCode,parse withdraw response error:%s", err.Error())
		return false
	} else if CurrentUserId == userInfo.UserName {
		client_sdk.ClientSdkLogger.Info("processEventChainCode,recieve process withdraw notify for me!")
		return true
	}
	return false
}

func processEventRejection(event interface{}) {
	rejection, ok := event.(pb.Rejection)
	if ok {
		txMapMutex.Lock()
		if transacType, ok := invokeTXMap[rejection.Tx.Txid]; ok {
			delete(invokeTXMap, rejection.Tx.Txid)
			txMapMutex.Unlock()
			switch transacType {
			case invokeTx_Type_Transfer_Account:
				globalCallBack.OneChainTransferCallback(rejection.ErrorMsg, rejection.Tx.Txid)
			case invokeTx_Type_ZxCoinWithdraw:
				globalCallBack.OneChainZxCoinWithdraw(rejection.ErrorMsg, rejection.Tx.Txid)
			case invokeTx_Type_ZxCoinWithdrawRecall:
				if !IsClient {
					globalCallBack.OneChainZxCoinWithdrawRecall(rejection.ErrorMsg, rejection.Tx.Txid)
				}
			case invokeTx_Type_ZxCoinWithdrawFail:
				globalCallBack.OneChainZxCoinWithdrawFail(rejection.ErrorMsg, rejection.Tx.Txid)
			case invokeTx_Type_ZxCoinWithdrawRemitSucces:
				globalCallBack.OneChainZxCoinWithdrawRemitSucces(rejection.ErrorMsg, rejection.Tx.Txid)
			case invokeTx_Type_ZxCoinWithdrawConfirm:
				globalCallBack.OneChainZxCoinWithdrawConfirm(rejection.ErrorMsg, rejection.Tx.Txid)
			case invokeTx_Type_ZxCoinWithdrawAppeal:
				globalCallBack.OneChainZxCoinWithdrawAppeal(rejection.ErrorMsg, rejection.Tx.Txid)
			case invokeTx_Type_ZxCoinWithdrawAppealDone:
				globalCallBack.OneChainZxCoinWithdrawAppealDone(rejection.ErrorMsg, rejection.Tx.Txid)

			case invokeTx_Type_Prize_Rule_Add:
				globalCallBack.OneChainPrizeRuleAddCallback(rejection.ErrorMsg, rejection.Tx.Txid)
			case invokeTx_Type_Prize_Rule_Modify:
				globalCallBack.OneChainPrizeRuleModifyCallback(rejection.ErrorMsg, rejection.Tx.Txid)
			case invokeTx_Type_Prize_Rule_Delete:
				globalCallBack.OneChainPrizeRuleDeleteCallback(rejection.ErrorMsg, rejection.Tx.Txid)
			case invokeTx_Type_Ball_Rule_Add:
				//globalCallBack.OneChainBallRuleAddCallback(rejection.ErrorMsg, rejection.Tx.Txid)
			case invokeTx_Type_Ball_Rule_Modify:
				//globalCallBack.OneChainBallRuleModifyCallback(rejection.ErrorMsg, rejection.Tx.Txid)
			case invokeTx_Type_Ball_Rule_Delete:
				//globalCallBack.OneChainBallRuleDeleteCallback(rejection.ErrorMsg, rejection.Tx.Txid)
			case invokeTx_Type_One_Lottery_Add:
				globalCallBack.OneChainOneLotteryAddCallback(rejection.ErrorMsg, rejection.Tx.Txid)
			case invokeTx_Type_One_Lottery_Modify:
				globalCallBack.OneChainOneLotteryModifyCallback(rejection.ErrorMsg, rejection.Tx.Txid)
			case invokeTx_Type_One_Lottery_Delete:
				globalCallBack.OneChainOneLotteryDeleteCallback(rejection.ErrorMsg, rejection.Tx.Txid)
			case invokeTx_Type_One_Lottery_Bet:
				globalCallBack.OneChainOneLotteryBetCallback(rejection.ErrorMsg, rejection.Tx.Txid)
			case invokeTx_Type_One_Lottery_Open_Reward:
				globalCallBack.OneChainOneLotteryOpenRewardCallback(rejection.ErrorMsg, rejection.Tx.Txid)
			}
		} else {
			txMapMutex.Unlock()
			client_sdk.ClientSdkLogger.Info("The transaction not find!")
		}
	}
}

/**
 * Description Register a user
 *
 * @param userId The account of a user to register.
 * @param affiliation The affiliation of a user to register.
 * @param role Role of the user to register.
 * @return ret,enrollPwd
 */
func OneChainRegister(role int, userId string, affiliation string) (ret int, enrollPwd string) {
	if userId == "" || affiliation == "" {
		client_sdk.ClientSdkLogger.Error("OneChainRegister,userId or affiliation may empty!")
		return client_sdk.PARA_ERR, ""
	}
	return client_sdk.FabricSdkRegister(role, userId, affiliation)
}

/**
* Description enroll a user
*
* @param enrollID
* @param enrollPWD
* @param pwd
* @return ret
 */
func OneChainEnroll(enrollID string, enrollPWD string, pwd []byte) (ret int) {
	if enrollID == "" || enrollPWD == "" {
		client_sdk.ClientSdkLogger.Error("OneChainEnroll,enrollID or enrollPWD may empty!")
		return client_sdk.PARA_ERR
	}
	ret = client_sdk.FabricSdkEnroll(enrollID, enrollPWD, pwd)
	if ret >= client_sdk.SUCCESS {
		CurrentUserId = enrollID
	}
	return ret
}

/**
 * Description Register and then enroll a user
 *
 * @param userId The account of a user to register.
 * @param affiliation The affiliation of a user to register.
 * @param role Role of the user to register.
 * @param pwd
 * @return ret
 */
func OneChainRegisterAndEnroll(role int, userId string, affiliation string, pwd []byte) (ret int) {
	if userId == "" || affiliation == "" {
		client_sdk.ClientSdkLogger.Error("OneChainRegisterAndEnroll,userId or affiliation may empty!")
		return client_sdk.PARA_ERR
	}
	ret = client_sdk.FabricSdkRegisterAndEnroll(role, userId, affiliation, pwd)
	if ret >= client_sdk.SUCCESS {
		CurrentUserId = userId
	}
	return ret
}

func OneChainGetPubkeyHash(userId string) (ret int, pubHash string) {
	if userId == "" {
		client_sdk.ClientSdkLogger.Error("OneChainGetPubkeyHash,userId may empty!")
		return client_sdk.PARA_ERR, ""
	}

	if client_sdk.IsSignEnabled() {
		userCert, err := client_sdk.LoadCertX509AndDer(userId)
		if err != nil {
			client_sdk.ClientSdkLogger.Errorf("OneChainZXCoinInit,error:%s", err.Error())
			return client_sdk.INTERNAL_ERR, ""
		}

		hash := sha3.New224()

		hash.Write([]byte(userCert))
		hash_context := hash.Sum(nil)
		return client_sdk.SUCCESS, fmt.Sprintf("%x", hash_context)
	}

	return client_sdk.SUCCESS, userId
}

func OneChainGetCertInfo(userId string, pwd []byte) (int, string) {
	if userId == "" {
		client_sdk.ClientSdkLogger.Error("OneChainGetPubkeyHash,userId may empty!")
		return client_sdk.PARA_ERR, ""
	}

	if client_sdk.IsSignEnabled() {
		enrollPrivKey, err := loadPrivateKey(userId, "enrollment.key", pwd)
		if err != nil {
			client_sdk.ClientSdkLogger.Errorf("Failed loading enrollment private key [%s].", err.Error())
			return client_sdk.PARA_ERR, ""
		}
		key := enrollPrivKey.(*ecdsa.PrivateKey).D.Bytes()

		cert, _, err := loadCertX509AndDer(userId, "enrollment.cert")
		if err != nil {
			client_sdk.ClientSdkLogger.Errorf("Failed parsing enrollment certificate [%s].", err.Error())
			return client_sdk.PARA_ERR, ""
		}

		return client_sdk.SUCCESS, fmt.Sprintf(">>>key:%x\n>>>cert:%x", key, cert.Raw)
	}

	return client_sdk.SUCCESS, userId
}

func loadPrivateKey(userId, alias string, pwd []byte) (interface{}, error) {
	path := client_sdk.GetPathForAlias(userId, alias)
	client_sdk.ClientSdkLogger.Debugf("Loading private key [%s] at [%s]...", alias, path)

	raw, err := ioutil.ReadFile(path)
	if err != nil {
		client_sdk.ClientSdkLogger.Errorf("Failed loading private key [%s]: [%s].", alias, err.Error())
		return nil, err
	}

	privateKey, err := primitives.PEMtoPrivateKey(raw, pwd)
	if err != nil {
		client_sdk.ClientSdkLogger.Errorf("Failed parsing private key [%s]: [%s].", alias, err.Error())
		return nil, err
	}

	return privateKey, nil
}

func loadCertX509AndDer(userId, alias string) (*x509.Certificate, []byte, error) {
	path := client_sdk.GetPathForAlias(userId, alias)
	client_sdk.ClientSdkLogger.Debugf("Loading certificate [%s] at [%s]...", alias, path)

	pem, err := ioutil.ReadFile(path)
	if err != nil {
		client_sdk.ClientSdkLogger.Errorf("Failed loading certificate [%s]: [%s].", alias, err.Error())
		return nil, nil, err
	}

	cert, der, err := primitives.PEMtoCertificateAndDER(pem)
	if err != nil {
		client_sdk.ClientSdkLogger.Errorf("Failed parsing certificate [%s]: [%s].", alias, err.Error())
		return nil, nil, err
	}

	return cert, der, nil
}

func GetChainCodeID() string {
	return client_sdk.GetChainCodeIdName()
}

/**
* Description GetTxInfoByTxId
*
* @param userId The account of a user
* @param txId
* @return ret,payload
 */

func GetTxInfoByTxId(txId string) (ret int, payload string) {
	return client_sdk.FabricSdkGetChaincodeInputByTxId(txId)
}

func GetStateByBlockID(blockNumber uint64, key string, tableFalg uint32) (ret int, payload []byte) {
	chaincodeID := client_sdk.GetDBChainCodeIdName()
	ret, row := client_sdk.FabricSdkGetStateByBlockID(blockNumber, chaincodeID, key, tableFalg)
	if ret != client_sdk.SUCCESS {
		return ret, nil
	}

	var retValue []byte
	var err error

	switch tableFalg {
	case 0:
		result := &ZxCoinQueryBalanceResponse{
			Owner:           row.Columns[0].GetString_(),
			UserId:          row.Columns[1].GetString_(),
			Balance:         row.Columns[2].GetUint64(),
			Reserved:        row.Columns[3].GetUint64(),
			TxnIDs:          row.Columns[4].GetString_(),
			BlockHeight:     row.Columns[5].GetUint64(),
			PrevBlockHeight: row.Columns[6].GetUint64(),
		}

		if retValue, err = json.Marshal(result); err != nil {
			client_sdk.ClientSdkLogger.Error("Error marshalling row, type :%d, err : %s", tableFalg, err)
			return client_sdk.INTERNAL_ERR, nil
		}
	case 1:
		result := rowToHistory(row)
		if retValue, err = json.Marshal(result); err != nil {
			client_sdk.ClientSdkLogger.Error("Error marshalling row, type :%d, err : %s", tableFalg, err)
			return client_sdk.INTERNAL_ERR, nil
		}
	default:
		client_sdk.ClientSdkLogger.Error("Error table flag not found :%d", tableFalg)
		return client_sdk.PARA_ERR, nil
	}

	return ret, retValue
}

/**
* Description ZXCoinInit
*
* @param userId The account of a user
* @param initAmount
* @param time
* @param chainCodePath
* @param pwd
* @return ret,txId
 */
func OneChainZXCoinInit(userId string, initAmount uint64, time uint64,
	transferCost uint64, deployLotteryCost uint64,
	modifyLotteryCost uint64, historyLotteryKeepTime uint64, chainCodePath string, pwd []byte) (ret int, txId string) {

	if userId == "" || initAmount == 0 || time == 0 || chainCodePath == "" {
		client_sdk.ClientSdkLogger.Error("OneChainZXCoinInit,invalid param!")
		return client_sdk.PARA_ERR, ""
	}

	chainCodeId = GetChainCodeID()

	zxcoinInitRequest := &ZxCoinInitRequest{
		InitAmount:             initAmount,
		Time:                   time,
		AdminUserId:            userId,
		TransferCost:           transferCost,
		DeployLotteryCost:      deployLotteryCost,
		ModifyLotteryCost:      modifyLotteryCost,
		HistoryLotteryKeepTime: historyLotteryKeepTime,
	}

	zxcoinInitContent, err := json.Marshal(zxcoinInitRequest)
	if err != nil {
		client_sdk.ClientSdkLogger.Errorf("OneChainZXCoinInit,error:%s", err.Error())
		return client_sdk.INTERNAL_ERR, ""
	}

	args := [][]byte{[]byte(deployTx_Func_ZXCoin_Init), zxcoinInitContent, []byte(client_sdk.GetDBChainCodeIdName())}

	ret, txId = client_sdk.FabricSdkDeploy(chainCodePath, args, userId, pwd)

	return ret, txId
}

/**
* Description ZxCoin transfer account.
*
* @param userId The account of the user transfer from
* @param nameTo The account of the user transfer to
* @param addressTo The address(public key hash) to transfer account.
* @param amount The amount of zxcoin to transfer account.
* @param transferType // 0:normal transfer
* @param remark
* @param time The time of transfer account.
* @param pwd
* @return ret
 */
func OneChainTransferAccount(userId, extras string, nameTo string, addressTo string,
	amount uint64, transferType uint32, remark string, time uint64, pwd []byte) (ret int, txId string) {

	if userId == "" || addressTo == "" || amount == 0 ||
		transferType != 0 || time == 0 {
		client_sdk.ClientSdkLogger.Error("OneChainTransferAccount,invalid param!")
		return client_sdk.PARA_ERR, ""
	}

	transferRequest := &ZxCoinTransferRequest{
		UserId:     userId,
		NameTo:     nameTo,
		UserCertTo: addressTo,
		Extras:     extras,
		Amount:     amount,
		Fee:        normalFee.TransferFee,
		Type:       transferType,
		Remark:     remark,
		Time:       time,
	}

	transferContent, err := json.Marshal(transferRequest)
	if err != nil {
		client_sdk.ClientSdkLogger.Errorf("OneChainTransferAccount,error:%s", err.Error())
		return client_sdk.INTERNAL_ERR, ""
	}

	args := [][]byte{[]byte(invokeTx_Func_Transfer_Account), transferContent}

	ret, txId = client_sdk.FabricSdkInvoke(chainCodeId, args, userId, pwd)

	if ret == client_sdk.SUCCESS {
		txMapMutex.Lock()
		invokeTXMap[txId] = invokeTx_Type_Transfer_Account
		txMapMutex.Unlock()
	}

	return ret, txId
}

func OneChainFrezon(userId string, amount uint64, transferType uint32, remark string, time uint64, pwd []byte) (ret int, txId string) {
	transferRequest := &ZxCoinFrezonRequest{
		Amount: int64(amount),
		Type:   uint64(transferType),
		Remark: remark,
		Time:   time,
		UserId: userId,
	}

	transferContent, err := json.Marshal(transferRequest)
	if err != nil {
		client_sdk.ClientSdkLogger.Errorf("OneChainFrezon,error:%s", err.Error())
		return client_sdk.INTERNAL_ERR, ""
	}

	args := [][]byte{[]byte("zxCoinFrezon"), transferContent}

	ret, txId = client_sdk.FabricSdkInvoke(chainCodeId, args, userId, pwd)

	if ret == client_sdk.SUCCESS {
		txMapMutex.Lock()
		invokeTXMap[txId] = invokeTx_Type_Transfer_Account
		txMapMutex.Unlock()
	}

	return ret, txId
}

/**
 * Description Query the zxcoin balance.
 *
 * @param userId The account of the user to query balance.
 * @return retMsg The return json msg of query
 */
func OneChainBalanceQuery(userId string) (retMsg string) {
	if userId == "" {
		client_sdk.ClientSdkLogger.Error("OneChainBalanceQuery,invalid param!")
		return ""
	}

	userCert := userId
	var ret int = client_sdk.SUCCESS
	if client_sdk.IsSignEnabled() {
		ret, userCert = OneChainGetPubkeyHash(userId)
		if ret < client_sdk.SUCCESS {
			client_sdk.ClientSdkLogger.Error("OneChainBalanceQuery,get pubkeyhash error")
			return ""
		}
	}

	queryBanlanceReq := &ZxCoinQueryBalanceRequest{
		UserCert: userCert,
	}

	queryBalanceContent, err := json.Marshal(queryBanlanceReq)
	if err != nil {
		client_sdk.ClientSdkLogger.Errorf("OneChainBalanceQuery,error:%s", err.Error())
		return ""
	}

	args := [][]byte{[]byte(invokeTx_Func_Query_Balance), queryBalanceContent}

	_, val := client_sdk.FabricSdkQuery(chainCodeId, args, userId)

	return val
}

/**
* Description Get user hash by user name.
*
* @param userId   me
* @param userName The name to query.
* @param userAddress The hash to query.
* @return ret and val
 */
func OneChainZxCoinGetUserInfo(userId string, userName string, userAddress string) (ret int, val string) {
	if userId == "" {
		client_sdk.ClientSdkLogger.Error("OneChainZxCoinGetUserInfo,invalid param!")
		return client_sdk.PARA_ERR, ""
	}

	getUserReq := &ZxCoinGetUserInfoRequest{
		Owner:  userAddress,
		UserId: userName,
	}

	content, err := json.Marshal(getUserReq)
	if err != nil {
		client_sdk.ClientSdkLogger.Errorf("OneChainZxCoinGetUserInfo,error:%s", err.Error())
		return client_sdk.INTERNAL_ERR, ""
	}

	args := [][]byte{[]byte(invokeTx_Func_ZXCoin_Get_UserInfo), content}

	return client_sdk.FabricSdkQuery(chainCodeId, args, userId)
}

func ZxCoinWithdraw(bankName, accountName, accountId, userId string, amount uint64, pwd []byte) (int, string) {
	if bankName == "" || accountName == "" || accountId == "" || userId == "" || amount <= 0 {
		client_sdk.ClientSdkLogger.Error("zxCoinWithdraw,invalid param!")
		return client_sdk.PARA_ERR, ""
	}

	accountInfo, err := json.Marshal(AccountInfo{bankName, accountName, accountId})
	if err != nil {
		client_sdk.ClientSdkLogger.Errorf("zxCoinWithdraw,error:%s", err.Error())
		return client_sdk.INTERNAL_ERR, ""
	}

	req := &WithdrawApply{}
	req.AccountInfo = string(accountInfo)
	req.UserId = userId
	req.Amount = amount
	req.ModifyTime = uint64(time.Now().UnixNano() / 1e6)
	cont, err := json.Marshal(req)
	if err != nil {
		client_sdk.ClientSdkLogger.Errorf("zxCoinWithdraw,error:%s", err.Error())
		return client_sdk.INTERNAL_ERR, ""
	}

	args := [][]byte{[]byte(invokeTx_Func_Withdraw), cont}

	ret, txId := client_sdk.FabricSdkInvoke(chainCodeId, args, userId, pwd)
	if ret == client_sdk.SUCCESS {
		txMapMutex.Lock()
		invokeTXMap[txId] = invokeTx_Type_ZxCoinWithdraw
		txMapMutex.Unlock()
	}

	return ret, txId
}

func ZxCoinWithdrawRecall(txid, userId string, pwd []byte) (int, string) {
	if IsClient {
		return client_sdk.PARA_ERR, ""
	}
	if txid == "" || userId == "" {
		client_sdk.ClientSdkLogger.Error("zxCoinWithdrawRecall,invalid param!")
		return client_sdk.PARA_ERR, ""
	}

	req := &WithdrawTxInfo{}
	req.TxId = txid
	req.ModifyTime = uint64(time.Now().UnixNano() / 1e6)
	cont, err := json.Marshal(req)
	if err != nil {
		client_sdk.ClientSdkLogger.Errorf("zxCoinWithdrawRecall,error:%s", err.Error())
		return client_sdk.INTERNAL_ERR, ""
	}

	args := [][]byte{[]byte("zxCoinWithdrawRecall"), cont}
	ret, txId := client_sdk.FabricSdkInvoke(chainCodeId, args, userId, pwd)
	if ret == client_sdk.SUCCESS {
		txMapMutex.Lock()
		invokeTXMap[txId] = invokeTx_Type_ZxCoinWithdrawRecall
		txMapMutex.Unlock()
	}
	return ret, txId
}

func ZxCoinWithdrawFail(txid, remark, extras, userId string, pwd []byte) (int, string) {
	if txid == "" || userId == "" {
		client_sdk.ClientSdkLogger.Error("OneChainZxCoinWithdrawFail,invalid param!")
		return client_sdk.PARA_ERR, ""
	}
	req := &WithdrawRemark{}
	req.TxId = txid
	req.ModifyTime = uint64(time.Now().UnixNano() / 1e6)
	req.Extras = extras
	req.Remark = remark
	cont, err := json.Marshal(req)
	if err != nil {
		client_sdk.ClientSdkLogger.Errorf("OneChainZxCoinWithdrawFail,error:%s", err.Error())
		return client_sdk.INTERNAL_ERR, ""
	}
	args := [][]byte{[]byte(invokeTx_Func_Withdraw_Fail), cont}
	ret, txId := client_sdk.FabricSdkInvoke(chainCodeId, args, userId, pwd)
	if ret == client_sdk.SUCCESS {
		txMapMutex.Lock()
		invokeTXMap[txId] = invokeTx_Type_ZxCoinWithdrawFail
		txMapMutex.Unlock()
	}
	return ret, txId
}

func ZxCoinWithdrawRemitSucces(txid, remitOrderNumber, extras, userId string, pwd []byte) (int, string) {
	if txid == "" || remitOrderNumber == "" || userId == "" {
		client_sdk.ClientSdkLogger.Error("OneChainZxCoinWithdrawRemitSucces,invalid param!")
		return client_sdk.PARA_ERR, ""
	}
	req := &WithdrawSuccess{}
	req.TxId = txid
	req.ModifyTime = uint64(time.Now().UnixNano() / 1e6)
	req.RemitOrderNumber = remitOrderNumber
	req.Extras = extras
	cont, err := json.Marshal(req)
	if err != nil {
		client_sdk.ClientSdkLogger.Errorf("OneChainZxCoinWithdrawRemitSucces,error:%s", err.Error())
		return client_sdk.INTERNAL_ERR, ""
	}
	args := [][]byte{[]byte(invokeTx_Func_Withdraw_Remit_Succes), cont}
	ret, txId := client_sdk.FabricSdkInvoke(chainCodeId, args, userId, pwd)
	if ret == client_sdk.SUCCESS {
		txMapMutex.Lock()
		invokeTXMap[txId] = invokeTx_Type_ZxCoinWithdrawRemitSucces
		txMapMutex.Unlock()
	}
	return ret, txId
}

func ZxCoinWithdrawConfirm(txid, userId, extras string, pwd []byte) (int, string) {
	if txid == "" || userId == "" {
		client_sdk.ClientSdkLogger.Error("OneChainZxCoinWithdrawConfirm,invalid param!")
		return client_sdk.PARA_ERR, ""
	}

	req := &WithdrawTxInfo{}
	req.TxId = txid
	req.ModifyTime = uint64(time.Now().UnixNano() / 1e6)
	req.Extras = extras
	cont, err := json.Marshal(req)
	if err != nil {
		client_sdk.ClientSdkLogger.Errorf("ZxCoinWithdrawConfirm,error:%s", err.Error())
		return client_sdk.INTERNAL_ERR, ""
	}

	args := [][]byte{[]byte(invokeTx_Func_Withdraw_Remit_Confirm), cont}
	ret, txId := client_sdk.FabricSdkInvoke(chainCodeId, args, userId, pwd)
	if ret == client_sdk.SUCCESS {
		txMapMutex.Lock()
		invokeTXMap[txId] = invokeTx_Type_ZxCoinWithdrawConfirm
		txMapMutex.Unlock()
	}
	return ret, txId
}

func ZxCoinWithdrawAppeal(txid, remark, userId string, pwd []byte) (int, string) {
	if txid == "" || userId == "" {
		client_sdk.ClientSdkLogger.Error("OneChainZxCoinWithdrawAppeal,invalid param!")
		return client_sdk.PARA_ERR, ""
	}
	req := &WithdrawRemark{}
	req.TxId = txid
	req.ModifyTime = uint64(time.Now().UnixNano() / 1e6)
	req.Remark = remark
	cont, err := json.Marshal(req)
	if err != nil {
		client_sdk.ClientSdkLogger.Errorf("OneChainZxCoinWithdrawAppeal,error:%s", err.Error())
		return client_sdk.INTERNAL_ERR, ""
	}
	args := [][]byte{[]byte(invokeTx_Func_Withdraw_Appeal), cont}
	ret, txId := client_sdk.FabricSdkInvoke(chainCodeId, args, userId, pwd)
	if ret == client_sdk.SUCCESS {
		txMapMutex.Lock()
		invokeTXMap[txId] = invokeTx_Type_ZxCoinWithdrawAppeal
		txMapMutex.Unlock()
	}
	return ret, txId
}

func ZxCoinWithdrawAppealDone(txid, remark string, result uint32, extras, userId string, pwd []byte) (int, string) {
	if txid == "" || userId == "" {
		client_sdk.ClientSdkLogger.Error("OneChainZxCoinWithdrawAppealDone,invalid param!")
		return client_sdk.PARA_ERR, ""
	}
	req := &WithdrawAppealDone{}
	req.TxId = txid
	req.ModifyTime = uint64(time.Now().UnixNano() / 1e6)
	req.Remark = remark
	req.Extras = extras
	req.Result = result
	cont, err := json.Marshal(req)
	if err != nil {
		client_sdk.ClientSdkLogger.Errorf("OneChainZxCoinWithdrawAppealDone,error:%s", err.Error())
		return client_sdk.INTERNAL_ERR, ""
	}
	args := [][]byte{[]byte(invokeTx_Func_Withdraw_Appeal_Done), cont}
	ret, txId := client_sdk.FabricSdkInvoke(chainCodeId, args, userId, pwd)
	if ret == client_sdk.SUCCESS {
		txMapMutex.Lock()
		invokeTXMap[txId] = invokeTx_Type_ZxCoinWithdrawAppealDone
		txMapMutex.Unlock()
	}
	return ret, txId
}

func ZxCoinWithdrawQuery(userName, userId string) string {
	if userId == "" {
		client_sdk.ClientSdkLogger.Error("OneChainZxCoinWithdrawQuery,invalid param!")
		return ""
	}
	args := [][]byte{[]byte("zxCoinWithdrawQuery"), []byte(userName)}
	_, val := client_sdk.FabricSdkQuery(chainCodeId, args, userId)
	return val
}

func ZxCoinWithdrawInfoQuery(TxId, userId string) string {
	if userId == "" || TxId == "" {
		client_sdk.ClientSdkLogger.Error("OneChainZxCoinWithdrawInfoQuery,invalid param!")
		return ""
	}
	args := [][]byte{[]byte("zxCoinWithdrawInfoQuery"), []byte(TxId)}
	_, val := client_sdk.FabricSdkQuery(chainCodeId, args, userId)
	return val
}

func ZxCoinSetConfig(pastTime uint64, userId string, pwd []byte) (int, string) {
	if pastTime <= 0 || userId == "" {
		client_sdk.ClientSdkLogger.Error("ZxCoinSetConfig,invalid param!")
		return client_sdk.PARA_ERR, ""
	}

	req := &ConfigPair{}
	req.Key = "AutoConfirmTime"
	req.Val = pastTime
	cont, err := json.Marshal(req)
	if err != nil {
		client_sdk.ClientSdkLogger.Errorf("ZxCoinSetConfig,error:%s", err.Error())
		return client_sdk.INTERNAL_ERR, ""
	}

	args := [][]byte{[]byte("zxCoinSetConfig"), cont}
	return client_sdk.FabricSdkInvoke(chainCodeId, args, userId, pwd)
}

func OneChainGetTicketNumbers(userId string, lotteryId string) (retMsg string) {
	if userId == "" || lotteryId == "" {
		client_sdk.ClientSdkLogger.Error("OneChainGetTicketNumbers,invalid param!")
		return ""
	}

	request := &OnelotteryGetTicketNumbersRequest{
		LotteryId: lotteryId,
	}

	requestContent, err := json.Marshal(request)
	if err != nil {
		client_sdk.ClientSdkLogger.Errorf("OneChainGetTicketNumbers,error:%s", err.Error())
		return ""
	}

	args := [][]byte{[]byte(invokeTx_Func_One_Lottery_GetTicketNumbers), requestContent}

	_, val := client_sdk.FabricSdkQuery(chainCodeId, args, userId)

	return val
}

/**
* Description OneLottery PrizeRule Add.
*
* @param adminId The account of the user to add prize rule.
* @param percentage The percentage of win prize in prize pool
* @param pwd
* @return ret:retcode txId:transction id
 */
func OneChainPrizeRuleAdd(adminId string, percentage uint64, hide bool, pwd []byte) (ret int, txId string) {
	if adminId == "" || percentage == 0 || percentage > 100 {
		client_sdk.ClientSdkLogger.Error("OneChainPrizeRuleAdd,invalid param!")
		return client_sdk.PARA_ERR, ""
	}

	rulePrizeAddReq := &RulePrizeAddRequest{
		Percentage: percentage,
		Hide:       hide,
		CreateTime: uint64(time.Now().UnixNano() / 1e6),
	}

	ruleAddContent, err := json.Marshal(rulePrizeAddReq)
	if err != nil {
		client_sdk.ClientSdkLogger.Errorf("OneChainPrizeRuleAdd,error:%s", err.Error())
		return client_sdk.INTERNAL_ERR, ""
	}

	args := [][]byte{[]byte(invokeTx_Func_Prize_Rule_Add), ruleAddContent}

	ret, txId = client_sdk.FabricSdkInvoke(chainCodeId, args, adminId, pwd)

	if ret == client_sdk.SUCCESS {
		txMapMutex.Lock()
		invokeTXMap[txId] = invokeTx_Type_Prize_Rule_Add
		txMapMutex.Unlock()
	}

	return ret, txId
}

/**
* Description OneLottery PrizeRule Modify.
*
* @param adminId The account of the user to modify prize rule.
* @param ruleId The rule id.
* @param name The rule name.
* @param percentage The percentage of win prize in prize pool
* @param updateTime
* @param pwd
* @return ret:retcode txId:transction id
 */
func OneChainPrizeRuleModify(adminId string, ruleId string,
	percentage uint64, hide bool, pwd []byte) (ret int, txId string) {
	if adminId == "" || ruleId == "" || percentage == 0 || percentage > 100 {
		client_sdk.ClientSdkLogger.Error("OneChainPrizeRuleModify,invalid param!")
		return client_sdk.PARA_ERR, ""
	}

	rulePrizeModifyReq := &RulePrizeModifyRequest{
		ID:         ruleId,
		Percentage: percentage,
		Hide:       hide,
	}

	ruleModifyContent, err := json.Marshal(rulePrizeModifyReq)
	if err != nil {
		client_sdk.ClientSdkLogger.Errorf("OneChainPrizeRuleModify,error:%s", err.Error())
		return client_sdk.INTERNAL_ERR, ""
	}

	args := [][]byte{[]byte(invokeTx_Func_Prize_Rule_Modify), ruleModifyContent}

	ret, txId = client_sdk.FabricSdkInvoke(chainCodeId, args, adminId, pwd)

	if ret == client_sdk.SUCCESS {
		txMapMutex.Lock()
		invokeTXMap[txId] = invokeTx_Type_Prize_Rule_Modify
		txMapMutex.Unlock()
	}

	return ret, txId
}

/**
* Description OneLottery PrizeRule Delete.
*
* @param adminId The account of the user to modify prize rule.
* @param ruleId The rule id.
* @param updateTime
* @return ret:retcode txId:transction id
 */
func OneChainPrizeRuleDelete(adminId string, ruleId string, pwd []byte) (ret int, txId string) {
	if adminId == "" || ruleId == "" {
		client_sdk.ClientSdkLogger.Error("OneChainPrizeRuleDelete,invalid param!")
		return client_sdk.PARA_ERR, ""
	}

	rulePrizeDeleteReq := &RulePrizeDeleteRequest{
		ID: ruleId,
	}

	ruleDeleteContent, err := json.Marshal(rulePrizeDeleteReq)
	if err != nil {
		client_sdk.ClientSdkLogger.Errorf("OneChainPrizeRuleDelete,error:%s", err.Error())
		return client_sdk.INTERNAL_ERR, ""
	}

	args := [][]byte{[]byte(invokeTx_Func_Prize_Rule_Delete), ruleDeleteContent}

	ret, txId = client_sdk.FabricSdkInvoke(chainCodeId, args, adminId, pwd)

	if ret == client_sdk.SUCCESS {
		txMapMutex.Lock()
		invokeTXMap[txId] = invokeTx_Type_Prize_Rule_Delete
		txMapMutex.Unlock()
	}

	return ret, txId
}

/**
* Description OneLottery PrizeRule query.
*
* @return retMsg The return json msg of query
 */
func OneChainPrizeRuleQuery(userId string) (retMsg string) {
	if userId == "" {
		client_sdk.ClientSdkLogger.Error("OneChainPrizeRuleQuery,invalid param!")
		return ""
	}

	args := [][]byte{[]byte(invokeTx_Func_Prize_Rule_Query), []byte("")}

	_, val := client_sdk.FabricSdkQuery(chainCodeId, args, userId)

	return val
}

/**
* Description OneLottery BallRule Add.
*
* @param adminId The account of the user to add ball rule.
* @param name The rule name.
* @param percentage1 The percentage of win first prize in prize pool
* @param percentage2 The percentage of win second prize in prize pool
* @param percentage3 The percentage of win third prize in prize pool
* @param pwd
* @return ret:retcode txId:transction id
 */
func OneChainBallRuleAdd(adminId string, name string, percentage1 uint32,
	percentage2 uint32, percentage3 uint32, pwd []byte) (ret int, txId string) {

	if adminId == "" || name == "" || (percentage1+percentage2+percentage3) == 0 ||
		(percentage1+percentage2+percentage3) > 100 ||
		percentage1 > 100 || percentage2 > 100 || percentage3 > 100 {
		client_sdk.ClientSdkLogger.Error("OneChainBallRuleAdd,invalid param!")
		return client_sdk.PARA_ERR, ""
	}

	ruleBallAddReq := &ruleBallAddReq{
		Name:        name,
		Percentage1: percentage1,
		Percentage2: percentage2,
		Percentage3: percentage3,
	}

	ruleAddContent, err := json.Marshal(ruleBallAddReq)
	if err != nil {
		client_sdk.ClientSdkLogger.Errorf("OneChainBallRuleAdd,error:%s", err.Error())
		return client_sdk.INTERNAL_ERR, ""
	}

	args := [][]byte{[]byte(invokeTx_Func_Ball_Rule_Add), ruleAddContent}

	ret, txId = client_sdk.FabricSdkInvoke(chainCodeId, args, adminId, pwd)

	if ret == client_sdk.SUCCESS {
		txMapMutex.Lock()
		invokeTXMap[txId] = invokeTx_Type_Ball_Rule_Add
		txMapMutex.Unlock()
	}

	return ret, txId
}

/**
* Description OneLottery BallRule Modify.
*
* @param adminId The account of the user to add ball rule.
* @param ruleId The rule id.
* @param name The rule name.
* @param percentage1 The percentage of win first prize in prize pool
* @param percentage2 The percentage of win second prize in prize pool
* @param percentage3 The percentage of win third prize in prize pool
* @param pwd
* @return ret:retcode txId:transction id
 */
func OneChainBallRuleModify(adminId string, ruleId string, name string, percentage1 uint32,
	percentage2 uint32, percentage3 uint32, pwd []byte) (ret int, txId string) {

	if adminId == "" || ruleId == "" || name == "" ||
		(percentage1+percentage2+percentage3) == 0 ||
		(percentage1+percentage2+percentage3) > 100 ||
		percentage1 > 100 || percentage2 > 100 || percentage3 > 100 {
		client_sdk.ClientSdkLogger.Error("OneChainBallRuleModify,invalid param!")
		return client_sdk.PARA_ERR, ""
	}

	ruleBallModifyReq := &ruleBallModifyReq{
		ID:          ruleId,
		Name:        name,
		Percentage1: percentage1,
		Percentage2: percentage2,
		Percentage3: percentage3,
	}

	ruleModifyContent, err := json.Marshal(ruleBallModifyReq)
	if err != nil {
		client_sdk.ClientSdkLogger.Errorf("OneChainBallRuleModify,error:%s", err.Error())
		return client_sdk.INTERNAL_ERR, ""
	}

	args := [][]byte{[]byte(invokeTx_Func_Ball_Rule_Modify), ruleModifyContent}

	ret, txId = client_sdk.FabricSdkInvoke(chainCodeId, args, adminId, pwd)

	if ret == client_sdk.SUCCESS {
		txMapMutex.Lock()
		invokeTXMap[txId] = invokeTx_Type_Ball_Rule_Modify
		txMapMutex.Unlock()
	}

	return ret, txId
}

/**
* Description OneLottery BallRule Delete.
*
* @param adminId The account of the user to add ball rule.
* @param ruleId The rule id.
* @param pwd
* @return ret:retcode txId:transction id
 */
func OneChainBallRuleDelete(adminId string, ruleId string, pwd []byte) (ret int, txId string) {

	if adminId == "" || ruleId == "" {
		client_sdk.ClientSdkLogger.Error("OneChainBallRuleDelete,invalid param!")
		return client_sdk.PARA_ERR, ""
	}

	ruleBallDeleteReq := &ruleBallDeleteReq{
		ID: ruleId,
	}

	ruleDeleteContent, err := json.Marshal(ruleBallDeleteReq)
	if err != nil {
		client_sdk.ClientSdkLogger.Errorf("OneChainBallRuleDelete,error:%s", err.Error())
		return client_sdk.INTERNAL_ERR, ""
	}

	args := [][]byte{[]byte(invokeTx_Func_Ball_Rule_Delete), ruleDeleteContent}

	ret, txId = client_sdk.FabricSdkInvoke(chainCodeId, args, adminId, pwd)

	if ret == client_sdk.SUCCESS {
		txMapMutex.Lock()
		invokeTXMap[txId] = invokeTx_Type_Ball_Rule_Delete
		txMapMutex.Unlock()
	}

	return ret, txId
}

/**
* Description OneLottery BallRule query.
*
* @return retMsg The return json msg of query
 */
func OneChainBallRuleQuery(userId string) (retMsg string) {
	if userId == "" {
		client_sdk.ClientSdkLogger.Error("OneChainBallRuleQuery,invalid param!")
		return ""
	}

	args := [][]byte{[]byte(invokeTx_Func_Ball_Rule_Query), []byte("")}

	_, val := client_sdk.FabricSdkQuery(chainCodeId, args, userId)

	return val
}

/**
* Description OneLottery activity add
*
* @param userId The account of the user to add one lottery.
* @param name The activity name.
* @param ruleType The rule type. "PrizeRule":prize rule "BallRule":ball rule
* @param ruleId The rule id.
* @param pictureIndex.
* @param createTime
* @param startTime
* @param closeTime
* @param minAttendeeCnt
* @param maxAttendeeCnt  0:no limit
* @param cost
* @param description
* @param pwd
* @return ret:retcode txId:transction id
 */
func OneChainOneLotteryAdd(userId string, name string, ruleType string,
	ruleId string, pictureIndex uint32, createTime uint64, startTime uint64, closeTime uint64,
	minAttendeeCnt uint32, maxAttendeeCnt uint32,
	cost uint32, total uint64, description string, pwd []byte) (ret int, txId string) {

	if userId == "" || name == "" || (ruleType != RuleType_Prize && ruleType != RuleType_Ball) ||
		ruleId == "" || createTime == 0 || startTime == 0 || closeTime == 0 ||
		minAttendeeCnt == 0 || cost == 0 || minAttendeeCnt > maxAttendeeCnt ||
		startTime > closeTime ||
		uint64(cost)*uint64(maxAttendeeCnt) != total {
		client_sdk.ClientSdkLogger.Error("OneChainOneLotteryAdd,invalid param!")
		return client_sdk.PARA_ERR, ""
	}

	oneLotteryAddReq := &OnelotteryAddRequest{
		Name:           name,
		Fee:            normalFee.CreateLotteryFee,
		PublisherName:  userId,
		RuleType:       ruleType,
		RuleID:         ruleId,
		PictureIndex:   pictureIndex,
		CreateTime:     createTime,
		StartTime:      startTime,
		CloseTime:      closeTime,
		MinAttendeeCnt: minAttendeeCnt,
		MaxAttendeeCnt: maxAttendeeCnt,
		Cost:           cost,
		Total:          total,
		Description:    description,
	}

	oneLotteryAddContent, err := json.Marshal(oneLotteryAddReq)
	if err != nil {
		client_sdk.ClientSdkLogger.Errorf("OneChainOneLotteryAdd,error:%s", err.Error())
		return client_sdk.INTERNAL_ERR, ""
	}

	client_sdk.ClientSdkLogger.Infof("OneChainOneLotteryAdd,json:%s", string(oneLotteryAddContent))

	args := [][]byte{[]byte(invokeTx_Func_One_Lottery_Add), oneLotteryAddContent}

	ret, txId = client_sdk.FabricSdkInvoke(chainCodeId, args, userId, pwd)

	if ret == client_sdk.SUCCESS {
		txMapMutex.Lock()
		invokeTXMap[txId] = invokeTx_Type_One_Lottery_Add
		txMapMutex.Unlock()
	}

	return ret, txId
}

/**
* Description OneLottery activity modify
*
* @param userId The account of the user to modify one lottery.
* @param lotteryId The lottery activity id.
* @param name The activity name.
* @param ruleType The rule type. "1":prize rule "2":ball rule
* @param ruleId The rule id.
* @param updateTime User can modify the activity before it starts.
* @param startTime
* @param closeTime
* @param minAttendeeCnt
* @param maxAttendeeCnt  0:no limit
* @param cost
* @param description
* @param pwd
* @return ret:retcode txId:transction id
 */
func OneChainOneLotteryModify(userId string, lotteryId string, name string, ruleType string,
	ruleId string, pictureIndex uint32, updateTime uint64, startTime uint64, closeTime uint64,
	minAttendeeCnt uint32, maxAttendeeCnt uint32,
	cost uint32, total uint64, description string, pwd []byte) (ret int, txId string) {
	fmt.Println(cost * maxAttendeeCnt)

	if userId == "" || lotteryId == "" || name == "" ||
		(ruleType != RuleType_Prize && ruleType != RuleType_Ball) ||
		ruleId == "" || updateTime == 0 || startTime == 0 || closeTime == 0 ||
		minAttendeeCnt == 0 || cost == 0 || minAttendeeCnt > maxAttendeeCnt ||
		updateTime > closeTime || startTime > closeTime ||
		uint64(cost)*uint64(maxAttendeeCnt) != total {
		client_sdk.ClientSdkLogger.Error("OneChainOneLotteryModify,invalid param!")
		return client_sdk.PARA_ERR, ""
	}

	oneLotteryModifyReq := &OnelotteryModifyRequest{
		ID:             lotteryId,
		Name:           name,
		Fee:            normalFee.ModefyLotteryFee,
		PublisherName:  userId,
		RuleType:       ruleType,
		RuleID:         ruleId,
		PictureIndex:   pictureIndex,
		UpdateTime:     updateTime,
		StartTime:      startTime,
		CloseTime:      closeTime,
		MinAttendeeCnt: minAttendeeCnt,
		MaxAttendeeCnt: maxAttendeeCnt,
		Cost:           cost,
		Total:          total,
		Description:    description,
	}

	oneLotteryModifyContent, err := json.Marshal(oneLotteryModifyReq)
	if err != nil {
		client_sdk.ClientSdkLogger.Errorf("OneChainOneLotteryModify,error:%s", err.Error())
		return client_sdk.INTERNAL_ERR, ""
	}

	args := [][]byte{[]byte(invokeTx_Func_One_Lottery_Modify), oneLotteryModifyContent}

	ret, txId = client_sdk.FabricSdkInvoke(chainCodeId, args, userId, pwd)

	if ret == client_sdk.SUCCESS {
		txMapMutex.Lock()
		invokeTXMap[txId] = invokeTx_Type_One_Lottery_Modify
		txMapMutex.Unlock()
	}

	return ret, txId
}

/**
* Description OneLottery activity delete
*
* @param userId The account of the user to delete one lottery.
* @param lotteryId The lottery activity id.
* @param pwd
* @return ret:retcode txId:transction id
 */
func OneChainOneLotteryDelete(userId string, lotteryId string, pwd []byte) (ret int, txId string) {
	if userId == "" || lotteryId == "" {
		client_sdk.ClientSdkLogger.Error("OneChainOneLotteryDelete,invalid param!")
		return client_sdk.PARA_ERR, ""
	}

	oneLotteryDeleteReq := &OnelotteryDeleteRequest{
		ID:         lotteryId,
		UpdateTime: uint64(time.Now().UnixNano() / 1e6),
	}

	oneLotteryDeleteContent, err := json.Marshal(oneLotteryDeleteReq)
	if err != nil {
		client_sdk.ClientSdkLogger.Errorf("OneChainOneLotteryDelete,error:%s", err.Error())
		return client_sdk.INTERNAL_ERR, ""
	}

	args := [][]byte{[]byte(invokeTx_Func_One_Lottery_Delete), oneLotteryDeleteContent}

	ret, txId = client_sdk.FabricSdkInvoke(chainCodeId, args, userId, pwd)

	if ret == client_sdk.SUCCESS {
		txMapMutex.Lock()
		invokeTXMap[txId] = invokeTx_Type_One_Lottery_Delete
		txMapMutex.Unlock()
	}

	return ret, txId
}

/**
* Description OneLottery activity query.
*
* @return retMsg The return json msg of query
 */
func OneChainOneLotteryQuery(userId string, lotteryId string) (retMsg string) {
	if userId == "" {
		client_sdk.ClientSdkLogger.Error("OneChainOneLotteryQuery,invalid param!")
		return ""
	}

	request := &OnelotteryQueryRequest{
		ID: lotteryId,
	}

	content, err := json.Marshal(request)
	if err != nil {
		client_sdk.ClientSdkLogger.Errorf("OneChainOneLotteryQuery,error:%s", err.Error())
		return ""
	}

	args := [][]byte{[]byte(invokeTx_Func_One_Lottery_Query), content}

	_, val := client_sdk.FabricSdkQuery(chainCodeId, args, userId)

	return val
}

/**
* Description OneLottery history activity query.
*
* @return retMsg The return json msg of query
 */
func OneChainOneLotteryHistoryQuery(userId string, lotteryId string) (retMsg string) {
	if userId == "" {
		client_sdk.ClientSdkLogger.Error("OneChainOneLotteryHistoryQuery,invalid param!")
		return ""
	}

	request := &OneLotteryHistoryQueryRequest{
		ID: lotteryId,
	}

	content, err := json.Marshal(request)
	if err != nil {
		client_sdk.ClientSdkLogger.Errorf("OneChainOneLotteryHistoryQuery,error:%s", err.Error())
		return ""
	}

	args := [][]byte{[]byte(invokeTx_Func_One_Lottery_History_Query), content}

	_, val := client_sdk.FabricSdkQuery(chainCodeId, args, userId)

	return val
}

/**
* Description OneLottery history activity query.
*
* @return retMsg The return json msg of query
 */
func OneChainOneLotteryOldHistoryQuery(userId string, lotteryId string) (retMsg string) {
	if userId == "" {
		client_sdk.ClientSdkLogger.Error("OneChainOneLotteryHistoryQuery,invalid param!")
		return ""
	}

	request := &OneLotteryHistoryQueryRequest{
		ID: lotteryId,
	}

	content, err := json.Marshal(request)
	if err != nil {
		client_sdk.ClientSdkLogger.Errorf("OneChainOneLotteryHistoryQuery,error:%s", err.Error())
		return ""
	}

	args := [][]byte{[]byte(invokeTx_Func_One_Lottery_OldHistory_Query), content}

	_, val := client_sdk.FabricSdkQuery(chainCodeId, args, userId)

	return val
}

/**
* Description OneLottery bet.
*
* @param userId
* @param amount
* @param lotteryId
* @param count
* @param chianCodeId
* @param createTime
* @return ret:retcode txId:transction id
 */
func OneChainOneLotteryBet(userId string, amount uint64, lotteryId string,
	count uint32, createTime uint64, pwd []byte) (ret int, txId string) {
	if userId == "" || amount == 0 || lotteryId == "" || count == 0 || chainCodeId == "" || createTime == 0 {
		client_sdk.ClientSdkLogger.Error("OneChainOneLotteryBet,invalid param!")
		return client_sdk.PARA_ERR, ""
	}

	oneLotteryBetReq := &OnelotteryBetRequest{
		LotteryID:  lotteryId,
		Amount:     amount,
		Count:      count,
		CreateTime: createTime,
		UserId:     userId,
	}

	oneLotteryBetContent, err := json.Marshal(oneLotteryBetReq)
	if err != nil {
		client_sdk.ClientSdkLogger.Errorf("OneChainOneLotteryBet,error:%s", err.Error())
		return client_sdk.INTERNAL_ERR, ""
	}

	args := [][]byte{[]byte(invokeTx_Func_One_Lottery_Bet), oneLotteryBetContent}

	ret, txId = client_sdk.FabricSdkInvoke(chainCodeId, args, userId, pwd)

	if ret == client_sdk.SUCCESS {
		txMapMutex.Lock()
		invokeTXMap[txId] = invokeTx_Type_One_Lottery_Bet
		txMapMutex.Unlock()
	}

	return ret, txId
}

/**
* Description OneLottery bet query.
*
* @param userId
* @param toQueryUser
* @param lotteryId
* @return retMsg The return json msg of query
 */
func OneChainOneLotteryBetQuery(userId string, toQueryUser string, lotteryId string) (retMsg string) {
	if userId == "" || (lotteryId == "" && toQueryUser == "") {
		client_sdk.ClientSdkLogger.Error("OneChainOneLotteryBetQuery,invalid param!")
		return ""
	}

	toQueryUserHash := toQueryUser
	if toQueryUser != "" {
		_, toQueryUserInfo := OneChainZxCoinGetUserInfo(userId, toQueryUser, "")
		client_sdk.ClientSdkLogger.Errorf("OneChainZxCoinGetUserInfo,user:%s", toQueryUserInfo)
		response := &ZxCoinQueryUserInfoResponse{}
		err := json.Unmarshal([]byte(toQueryUserInfo), response)
		if err != nil {
			client_sdk.ClientSdkLogger.Error("OneChainOneLotteryBetQuery,error get toQueryUser info")
			return ""
		}

		toQueryUserHash = response.Data.Owner
	}

	oneLotteryBetQuery := &onelotteryBetQueryRequest{
		Attendee:  toQueryUserHash,
		LotteryId: lotteryId,
	}

	oneLotteryBetQueryContent, err := json.Marshal(oneLotteryBetQuery)
	if err != nil {
		client_sdk.ClientSdkLogger.Errorf("OneChainOneLotteryBetQuery,error:%s", err.Error())
		return ""
	}

	args := [][]byte{[]byte(invokeTx_Func_One_Lottery_Bet_Query), oneLotteryBetQueryContent}

	_, val := client_sdk.FabricSdkQuery(chainCodeId, args, userId)

	return val
}

/**
* Description OneLottery open reward.
*
* @param userId
* @param lotteryId
* @param curTime
* @param pwd
* @return ret:retcode txId:transction id
 */
func OneChainOpenReward(userId string, lotteryId string, curTime uint64, pwd []byte) (ret int, txId string) {
	if userId == "" || lotteryId == "" || curTime == 0 {
		client_sdk.ClientSdkLogger.Error("OneChainOpenReward,invalid param!")
		return client_sdk.PARA_ERR, ""
	}
	openRewardReq := &OnelotteryBetOverRequest{
		LotteryID:   lotteryId,
		CurrentTime: curTime,
	}

	content, err := json.Marshal(openRewardReq)
	if err != nil {
		client_sdk.ClientSdkLogger.Errorf("OneChainOpenReward,error:%s", err.Error())
		return client_sdk.INTERNAL_ERR, ""
	}

	args := [][]byte{[]byte(invokeTx_Func_One_Lottery_Bet_Over), content}

	ret, txId = client_sdk.FabricSdkInvoke(chainCodeId, args, userId, pwd)

	if ret == client_sdk.SUCCESS {
		txMapMutex.Lock()
		invokeTXMap[txId] = invokeTx_Type_One_Lottery_Open_Reward
		txMapMutex.Unlock()
	}

	return ret, txId
}

/**
* Description OneChainOneLotteryRefund
*
* @param userId
* @param lotteryId
* @param refundTime
* @param pwd
* @return ret:retcode txId:transction id
 */
func OneChainOneLotteryRefund(userId string, lotteryId string, refundTime uint64, pwd []byte) (ret int, txId string) {
	if userId == "" || lotteryId == "" || refundTime == 0 {
		client_sdk.ClientSdkLogger.Error("OneChainOneLotteryRefund,invalid param!")
		return client_sdk.PARA_ERR, ""
	}

	refundReq := &OneLotteryRefundRequest{
		LotteryID:   lotteryId,
		CurrentTime: refundTime,
	}

	content, err := json.Marshal(refundReq)
	if err != nil {
		client_sdk.ClientSdkLogger.Errorf("OneChainOneLotteryRefund,error:%s", err.Error())
		return client_sdk.INTERNAL_ERR, ""
	}

	args := [][]byte{[]byte(invokeTx_Func_One_Lottery_Refund), content}

	ret, txId = client_sdk.FabricSdkInvoke(chainCodeId, args, userId, pwd)

	if ret == client_sdk.SUCCESS {
		txMapMutex.Lock()
		invokeTXMap[txId] = invokeTx_Type_One_Lottery_Refund
		txMapMutex.Unlock()
	}

	return ret, txId
}

func OneChainUserLogin(userId string, pwd string) (retCode int) {
	_, err := client_sdk.LoadPrivateKey(userId, client_sdk.GetEnrollmentKeyFilename(), []byte(pwd))
	if err != nil {
		client_sdk.ClientSdkLogger.Errorf("OneChainUserLogin,error:%s", err.Error())
		return client_sdk.PARA_ERR
	}
	CurrentUserId = userId
	return client_sdk.SUCCESS
}

func OneChainCheckUser(userId string, pwd string) (retCode int) {
	_, err := client_sdk.LoadPrivateKey(userId, client_sdk.GetEnrollmentKeyFilename(), []byte(pwd))
	if err != nil {
		client_sdk.ClientSdkLogger.Errorf("OneChainUserLogin,error:%s", err.Error())
		return client_sdk.PARA_ERR
	}
	return client_sdk.SUCCESS
}

func OneChainTest(userId string, putCount, getCount int) (ret int, txId string) {
	transferRequest := &struct {
		PutCount int `json:"putCount"`
		GetCount int `json:"getCount"`
	}{
		PutCount: putCount,
		GetCount: getCount,
	}

	transferContent, err := json.Marshal(transferRequest)
	if err != nil {
		client_sdk.ClientSdkLogger.Errorf("OneChainTransferAccount,error:%s", err.Error())
		return client_sdk.INTERNAL_ERR, ""
	}

	args := [][]byte{[]byte("oneLotteryTest"), transferContent}

	ret, txId = client_sdk.FabricSdkInvoke(chainCodeId, args, userId, []byte("admin"))

	if ret == client_sdk.SUCCESS {
		txMapMutex.Lock()
		invokeTXMap[txId] = invokeTx_Type_Transfer_Account
		txMapMutex.Unlock()
	}

	return ret, txId
}

func rowToHistory(row shim.Row) *LotteryHistory {
	return &LotteryHistory{
		TxnID:           row.Columns[0].GetString_(),
		NewTxnID:        row.Columns[1].GetString_(),
		Version:         row.Columns[2].GetUint32(),
		LastCloseTime:   row.Columns[3].GetUint64(),
		Numbers:         row.Columns[4].GetString_(),
		Balance:         row.Columns[5].GetUint64(),
		PrizeTxnID:      row.Columns[6].GetString_(),
		CountTotal:      row.Columns[7].GetUint32(),
		PictureIndex:    row.Columns[8].GetUint32(),
		Status:          row.Columns[9].GetUint32(),
		UpdateTime:      row.Columns[10].GetUint64(),
		BlockHeight:     row.Columns[11].GetUint64(),
		PrevBlockHeight: row.Columns[12].GetUint64(),
		TxnIDs:          row.Columns[13].GetString_(),
		CreateTime:      row.Columns[14].GetUint64(),
	}
}
