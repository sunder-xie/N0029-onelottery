package handler

import (
	"encoding/json"
	"time"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	"github.com/hyperledger/fabric/core/util"
	"peersafe.com/onelottery/chaincode/onechain/db"
	"peersafe.com/onelottery/chaincode/zxcoin/handler"
	"peersafe.com/onelottery/sdk/client_sdk"
)

func Init(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	logger.Debug("Init oneChain...")

	chaincodeID = stub.GetTxID()
	logger.Debugf("chaincodeID=%v\n", chaincodeID)

	err := stub.PutState(CHAINCODE_ID, []byte(chaincodeID))
	if err != nil {
		logger.Error("Failed to save CHAINCODE_ID\n")
		return writeMessage(function, stub, FAILED, "Failed to save CHAINCODE_ID", nil)
	}

	var request handler.ZxCoinInitRequest
	// Get args from json
	if err = request.DecodeZxCoinInitRequestJson(args[1]); err != nil {
		logger.Debug(err.Error())
		return writeMessage(function, stub, FAILED, "Parse param failed.", nil)
	}

	// create table
	if db.NewPrizeRule(stub).CreateTable() != nil ||
		db.NewBallRule(stub).CreateTable() != nil ||
		db.NewLottery(stub).CreateTable() != nil ||
		db.NewLotteryHistory(stub).CreateTable() != nil ||
		db.NewOldLotteryHistory(stub).CreateTable() != nil {
		logger.Error("Init Failed to create table\n")
		return writeMessage(function, stub, FAILED, "Failed to create table.", nil)
	}

	// save admin public key to ledger
	err = stub.PutState(ONECHAIN_ADMIN, []byte(args[0]))
	if err != nil {
		logger.Error("Failed to save admin pk\n")
		return writeMessage(function, stub, FAILED, "Failed to save admin pk", nil)
	}

	historyLotteryKeepTime = request.HistoryLotteryKeepTime

	//	go doRefundScan()

	logger.Debug("Init oneChain...done")
	return writeMessage(function, stub, SUCCESS, "", nil)
}

func InitArgs(stub shim.ChaincodeStubInterface) {
	if chaincodeID == "" {
		historyLotteryKeepTime = getDeleteHistoryLotterytime(stub, HISTORY_LOTTERY_KEEP_TIME)
		getChaincodeID, _ := stub.GetState(CHAINCODE_ID)
		chaincodeID = string(getChaincodeID)
	}
}

func CheckHistoryLottery() {
	now := uint64(time.Now().Unix())
	for lottery, createTime := range lotteryHistoryMap {
		if now > createTime+historyLotteryKeepTime {
			data, _ := json.Marshal(&refundRequest{lottery, now})
			args := util.ToChaincodeArgs("oneLotteryHistoryDelete", string(data))
			client_sdk.FabricSdkInvoke(chaincodeID, args, "ConsensusVoter", nil)
		}

		logger.Debugf("lottery : %s, time : %d", lottery, createTime)
	}
}

func InitTimerTask(stub shim.ChaincodeStubInterface) {
	if chaincodeID == "" {
		historyLotteryKeepTime = getDeleteHistoryLotterytime(stub, HISTORY_LOTTERY_KEEP_TIME)
		getChaincodeID, _ := stub.GetState(CHAINCODE_ID)
		chaincodeID = string(getChaincodeID)

		// init lottery activities to map
		syncLotteryToMap(stub)
		//		 init history lottery  to map
		syncHistoryLotteryToMap(stub)

		go doRefundScan()
	}
}

func syncLotteryToMap(stub shim.ChaincodeStubInterface) {
	lotterys, err := db.NewLottery(stub).GetNofinishedLotterys()
	if err != nil {
		logger.Errorf("[doRefundScan] get lotterys err: %v\n", err)
		return
	}

	for _, v := range lotterys {
		lotteryMapWrite(v.TxnID, &lotteryTime{v.StartTime, v.CloseTime})
	}
}

func syncHistoryLotteryToMap(stub shim.ChaincodeStubInterface) {
	lotterys, err := db.NewLotteryHistory(stub).GetNoDeleteLotterys()
	if err != nil {
		logger.Errorf("[doRefundScan] get lotterys err: %v\n", err)
		return
	}

	for _, v := range lotterys {
		lotteryHistoryMapWrite(v.TxnID, v.CreateTime)
	}
}

func doRefundScan() {
	ticker := time.NewTicker(30 * time.Second)
	defer ticker.Stop()
	for {
		select {
		case <-ticker.C:
			now := uint64(time.Now().Unix())
			logger.Debugf("time now=%v, len lottery map=%v \n", now, len(lotteryMap))

			for k, v := range lotteryMap {
				logger.Debugf("lottery %v, starttime=%v,closetime =%v\n", k, v.StartTime, v.CloseTime)
				// reach start time
				if now > v.StartTime {
					tmp := &lotteryStart{
						closeTime: v.CloseTime,
						StartFlag: false,
					}
					lotteryMapStartWrite(k, tmp)
				}
			}
			logger.Debugf("lotteryMapStart=%v\n", lotteryMapStart)

			for k, v := range lotteryMapStart {
				logger.Debugf("lottery id %v, lottery=%vn", k, v)
				// reach close time, but has not finish
				if now > v.closeTime {
					lotteryMapRefundWrite(k, 1)
				}
			}
			logger.Debugf("lotteryMapRefund=%v\n", lotteryMapRefund)

			// do update lottery status
			for lotteryID, lottery := range lotteryMapStart {
				if !lottery.StartFlag {
					data, _ := json.Marshal(&refundRequest{lotteryID, now})
					args := util.ToChaincodeArgs("oneLotteryStart", "", string(data))
					client_sdk.FabricSdkInvoke(chaincodeID, args, "ConsensusVoter", nil)
				}
			}

			// do refund
			for lottery := range lotteryMapRefund {
				data, _ := json.Marshal(&refundRequest{lottery, now})
				args := util.ToChaincodeArgs("oneLotteryRefund", "", string(data))
				client_sdk.FabricSdkInvoke(chaincodeID, args, "ConsensusVoter", nil)
			}

			// do history lottery delete
			for lottery, createTime := range lotteryHistoryMap {
				if now > createTime+historyLotteryKeepTime {
					data, _ := json.Marshal(&refundRequest{lottery, now})
					args := util.ToChaincodeArgs("oneLotteryHistoryDelete", "", string(data))
					client_sdk.FabricSdkInvoke(chaincodeID, args, "ConsensusVoter", nil)
				}
			}
		}
	}
}
