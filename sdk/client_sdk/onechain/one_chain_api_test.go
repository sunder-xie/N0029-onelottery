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
	"peersafe.com/onelottery/sdk/client_sdk"
	"testing"
	"time"
)

func TestOneChainApi_Init(t *testing.T) {
	ret := OneChainInit("../", "../")
	if ret != client_sdk.SUCCESS {
		t.Log("TestOneChainApi_Init error")
		t.Fail()
	}
}

// func TestOneChainApi_RegisterAndEnroll(t *testing.T) {
// 	ret := OneChainRegisterAndEnroll("one_chain_test1", "institutions")
// 	if ret < client_sdk.SUCCESS {
// 		t.Log("TestOneChainApi_RegisterAndEnroll error")
// 		t.Fail()
// 	}
// }

// func TestOneChainApi_RegisterAndEnroll1(t *testing.T) {
// 	ret := OneChainRegisterAndEnroll("one_chain_test2", "institutions")
// 	if ret < client_sdk.SUCCESS {
// 		t.Log("TestOneChainApi_RegisterAndEnroll1 error")
// 		t.Fail()
// 	}
// }

// func TestOneChainApi_RegisterAndEnroll2(t *testing.T) {
// 	ret := OneChainRegisterAndEnroll("one_chain_admin", "institutions")
// 	if ret < client_sdk.SUCCESS {
// 		t.Log("TestOneChainApi_RegisterAndEnroll1 error")
// 		t.Fail()
// 	}
// }

// func TestOneChainApi_Enroll(t *testing.T) {
// 	ret := OneChainEnroll("one_chain_test2", "NVxhTTDrvudj")
// 	if ret < client_sdk.SUCCESS {
// 		t.Log("TestOneChainApi_Enroll error")
// 		t.Fail()
// 	}
// }

// func TestOneChainApi_ZXCoinInit(t *testing.T) {
// 	chaincodePath := "peersafe.com/onelottery"
// 	time := time.Now().Unix()
// 	ret, txId := OneChainZXCoinInit("one_chain_admin", 1000000000, uint64(time), chaincodePath)
// 	if ret < client_sdk.SUCCESS {
// 		t.Log("TestOneChainApi_ZXCoinInit error")
// 		t.Fail()
// 	}
// 	client_sdk.ClientSdkLogger.Infof("TestOneChainApi_ZXCoinInit,txid:%s", txId)
// }

// func TestOneChainApi_TransferAccount(t *testing.T) {
// 	time := time.Now().Unix()
// 	ret, txId := OneChainTransferAccount("one_chain_admin", "one_chain_test1", "one_chain_test1", 100000, uint64(time), 0)
// 	if ret < client_sdk.SUCCESS {
// 		t.Log("TestOneChainApi_TransferAccount error")
// 		t.Fail()
// 	}
// 	client_sdk.ClientSdkLogger.Infof("TestOneChainApi_TransferAccount,txid:%s", txId)
// }

// func TestOneChainApi_TransferAccount1(t *testing.T) {
// 	time := time.Now().Unix()
// 	ret, txId := OneChainTransferAccount("one_chain_admin", "one_chain_test2", "one_chain_test2", 200000, uint64(time), 0)
// 	if ret < client_sdk.SUCCESS {
// 		t.Log("TestOneChainApi_TransferAccount error")
// 		t.Fail()
// 	}
// 	client_sdk.ClientSdkLogger.Infof("TestOneChainApi_TransferAccount,txid:%s", txId)
// }

// func TestOneChainApi_BalanceQuery(t *testing.T) {
// 	retMsg := OneChainBalanceQuery("one_chain_test1")
// 	if retMsg == "" {
// 		t.Log("TestOneChainApi_BalanceQuery error")
// 		t.Fail()
// 	}
// 	client_sdk.ClientSdkLogger.Infof("TestOneChainApi_BalanceQuery,retMsg:%s", retMsg)
// }

// func TestOneChainApi_BalanceQuery1(t *testing.T) {
// 	retMsg := OneChainBalanceQuery("one_chain_test2")
// 	if retMsg == "" {
// 		t.Log("TestOneChainApi_BalanceQuery error")
// 		t.Fail()
// 	}
// 	client_sdk.ClientSdkLogger.Infof("TestOneChainApi_BalanceQuery1,retMsg:%s", retMsg)
// }

// func TestOneChainApi_BalanceQuery2(t *testing.T) {
// 	retMsg := OneChainBalanceQuery("one_chain_admin")
// 	if retMsg == "" {
// 		t.Log("TestOneChainApi_BalanceQuery error")
// 		t.Fail()
// 	}
// 	client_sdk.ClientSdkLogger.Infof("TestOneChainApi_BalanceQuery2,retMsg:%s", retMsg)
// }

// func TestOneChainApi_ZXCoinDetailQuery(t *testing.T) {
// 	startTime := time.Now().Unix() - 600
// 	endTime := time.Now().Unix() - 10
// 	retMsg := OneChainZXCoinDetailQuery("one_chain_test2", uint64(startTime), uint64(endTime))
// 	if retMsg == "" {
// 		t.Log("TestOneChainApi_ZXCoinDetailQuery error")
// 		t.Fail()
// 	}
// 	client_sdk.ClientSdkLogger.Infof("TestOneChainApi_ZXCoinDetailQuery,retMsg:%s", retMsg)
// }

// func TestOneChainApi_PrizeRuleAdd(t *testing.T) {
// 	ret, txId := OneChainPrizeRuleAdd("one_chain_admin", "prize", 80)
// 	if ret < client_sdk.SUCCESS {
// 		t.Log("TestOneChainApi_PrizeRuleAdd error")
// 		t.Fail()
// 	}
// 	client_sdk.ClientSdkLogger.Infof("TestOneChainApi_PrizeRuleAdd,txid:%s", txId)
// }

// func TestOneChainApi_PrizeRuleModify(t *testing.T) {
// 	ret, txId := OneChainPrizeRuleModify("one_chain_admin", "d2cd9a54-a7ed-4f04-a3a5-8f39a17d3aa3", "prize", 85)
// 	if ret < client_sdk.SUCCESS {
// 		t.Log("TestOneChainApi_PrizeRuleModify error")
// 		t.Fail()
// 	}
// 	client_sdk.ClientSdkLogger.Infof("TestOneChainApi_PrizeRuleModify,txid:%s", txId)
// }

// func TestOneChainApi_PrizeRuleDelete(t *testing.T) {
// 	ret, txId := OneChainPrizeRuleDelete("one_chain_test2", "d2cd9a54-a7ed-4f04-a3a5-8f39a17d3aa3")
// 	if ret < client_sdk.SUCCESS {
// 		t.Log("TestOneChainApi_PrizeRuleDelete error")
// 		t.Fail()
// 	}
// 	client_sdk.ClientSdkLogger.Infof("TestOneChainApi_PrizeRuleDelete,txid:%s", txId)
// }

// func TestOneChainApi_PrizeRuleQuery(t *testing.T) {
// 	retMsg := OneChainPrizeRuleQuery("one_chain_admin")
// 	if retMsg == "" {
// 		t.Log("TestOneChainApi_PrizeRuleQuery error")
// 		t.Fail()
// 	}
// 	client_sdk.ClientSdkLogger.Infof("TestOneChainApi_PrizeRuleQuery,retMsg:%s", retMsg)
// }

// func TestOneChainApi_BallRuleAdd(t *testing.T) {
// 	ret, txId := OneChainBallRuleAdd("one_chain_test2", "ball", 50, 20, 10)
// 	if ret < client_sdk.SUCCESS {
// 		t.Log("TestOneChainApi_BallRuleAdd error")
// 		t.Fail()
// 	}
// 	client_sdk.ClientSdkLogger.Infof("TestOneChainApi_BallRuleAdd,txid:%s", txId)
// }

// func TestOneChainApi_BallRuleModify(t *testing.T) {
// 	ret, txId := OneChainBallRuleModify("one_chain_test2", "ruleId", "ball", 60, 20, 20)
// 	if ret < client_sdk.SUCCESS {
// 		t.Log("TestOneChainApi_BallRuleModify error")
// 		t.Fail()
// 	}
// 	client_sdk.ClientSdkLogger.Infof("TestOneChainApi_BallRuleModify,txid:%s", txId)
// }

// func TestOneChainApi_BallRuleDelete(t *testing.T) {
// 	ret, txId := OneChainBallRuleDelete("one_chain_test2", "ruleId")
// 	if ret < client_sdk.SUCCESS {
// 		t.Log("TestOneChainApi_BallRuleDelete error")
// 		t.Fail()
// 	}
// 	client_sdk.ClientSdkLogger.Infof("TestOneChainApi_BallRuleDelete,txid:%s", txId)
// }

// func TestOneChainApi_BallRuleQuery(t *testing.T) {
// 	retMsg := OneChainBallRuleQuery("one_chain_test2")
// 	if retMsg == "" {
// 		t.Log("TestOneChainApi_BallRuleQuery error")
// 		t.Fail()
// 	}
// 	client_sdk.ClientSdkLogger.Infof("TestOneChainApi_BallRuleQuery,retMsg:%s", retMsg)
// }

func TestOneChainApi_OneLotteryAdd(t *testing.T) {
	createTime := time.Now().Unix()
	startTime := createTime + 300
	closeTime := startTime + 100000000
	ret, txId := OneChainOneLotteryAdd("one_chain_test2", "oneLottery", RuleType_Prize, "d2cd9a54-a7ed-4f04-a3a5-8f39a17d3aa3",
		uint64(createTime), uint64(startTime), uint64(closeTime), 2, 3, 10000, 30000, "onelottery activity")
	if ret < client_sdk.SUCCESS {
		t.Log("TestOneChainApi_OneLotteryAdd error")
		t.Fail()
	}
	client_sdk.ClientSdkLogger.Infof("TestOneChainApi_OneLotteryAdd,txid:%s", txId)
}

// func TestOneChainApi_OneLotteryModify(t *testing.T) {
// 	updateTime := time.Now().Unix()
// 	startTime := updateTime + 600
// 	closeTime := startTime + 10000000
// 	ret, txId := OneChainOneLotteryModify("one_chain_test2", "099378db-bb9d-465a-8026-87ba30fde79b", "oneLottery", RuleType_Prize,
// 		"698f4f5e-3127-4827-9ca6-b92680620de0", uint64(updateTime), uint64(startTime), uint64(closeTime), 2, 3,
// 		20000, 60000, "onelottery activity new")
// 	if ret < client_sdk.SUCCESS {
// 		t.Log("TestOneChainApi_OneLotteryModify error")
// 		t.Fail()
// 	}
// 	client_sdk.ClientSdkLogger.Infof("TestOneChainApi_OneLotteryModify,txid:%s", txId)
// }

// func TestOneChainApi_OneLotteryDelete(t *testing.T) {
// 	ret, txId := OneChainOneLotteryDelete("one_chain_test2", "099378db-bb9d-465a-8026-87ba30fde79b")
// 	if ret < client_sdk.SUCCESS {
// 		t.Log("TestOneChainApi_OneLotteryDelete error")
// 		t.Fail()
// 	}
// 	client_sdk.ClientSdkLogger.Infof("TestOneChainApi_OneLotteryDelete,txid:%s", txId)
// }

// func TestOneChainApi_OneLotteryQuery(t *testing.T) {
// 	retMsg := OneChainOneLotteryQuery("one_chain_test2")
// 	if retMsg == "" {
// 		t.Log("TestOneChainApi_OneLotteryQuery error")
// 		t.Fail()
// 	}
// 	client_sdk.ClientSdkLogger.Infof("TestOneChainApi_OneLotteryQuery,retMsg:%s", retMsg)
// }

// func TestOneChainApi_OneLotteryBet(t *testing.T) {
// 	createTime := time.Now().Unix()
// 	ret, txId := OneChainOneLotteryBet("one_chain_test2", 10000, "099378db-bb9d-465a-8026-87ba30fde79b", 1,
// 		"2b4840331684eb263b6cb4476d3801383149971072fbdfb30e657f49186d919e5721773e28d94e1b53817194fa7a4a1a267f4801d3e83f44a621fbe5590166f6",
// 		uint64(createTime))
// 	if ret < client_sdk.SUCCESS {
// 		t.Log("TestOneChainApi_OneLotteryBet error")
// 		t.Fail()
// 	}
// 	client_sdk.ClientSdkLogger.Infof("TestOneChainApi_OneLotteryBet,txid:%s", txId)
// }

// func TestOneChainApi_OneLotteryBet1(t *testing.T) {
// 	createTime := time.Now().Unix()
// 	ret, txId := OneChainOneLotteryBet("one_chain_test1", 20000, "099378db-bb9d-465a-8026-87ba30fde79b", 2,
// 		"2b4840331684eb263b6cb4476d3801383149971072fbdfb30e657f49186d919e5721773e28d94e1b53817194fa7a4a1a267f4801d3e83f44a621fbe5590166f6",
// 		uint64(createTime))
// 	if ret < client_sdk.SUCCESS {
// 		t.Log("TestOneChainApi_OneLotteryBet error")
// 		t.Fail()
// 	}
// 	client_sdk.ClientSdkLogger.Infof("TestOneChainApi_OneLotteryBet,txid:%s", txId)
// }

// func TestOneChainApi_OneLotteryBetQuery(t *testing.T) {
// 	retMsg := OneChainOneLotteryBetQuery("one_chain_test1")
// 	if retMsg == "" {
// 		t.Log("TestOneChainApi_OneLotteryBetQuery error")
// 		t.Fail()
// 	}
// 	client_sdk.ClientSdkLogger.Infof("TestOneChainApi_OneLotteryBetQuery,retMsg:%s", retMsg)
// }
