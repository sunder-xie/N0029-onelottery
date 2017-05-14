package handler

import (
	"github.com/hyperledger/fabric/core/chaincode/shim"
	"peersafe.com/onelottery/chaincode/onechain/db"
)

type OldLotteryHistory struct {
}

func NewOldLotteryHistory() *OldLotteryHistory {
	return &OldLotteryHistory{}
}

func (l *OldLotteryHistory) GetLottery(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	// check args
	if err := checkArgs(args); err != nil {
		return writeMessage(function, stub, 1, err.Error(), nil)
	}

	// parse json
	lottery := db.NewOldLotteryHistory(stub)
	err := lottery.Unmarshal(args[1])
	if err != nil {
		logger.Errorf("[GetOldLotteryHistory] Unmarshal error=%v\n", err)
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}

	if lottery.TxnID == "" {
		// return false
		return writeMessage(function, stub, FAILED, "[GetOldLotteryHistory] txnID can not be nil", nil)
	}

	// get db
	lotterys, err := lottery.GetOneByID(lottery.TxnID)
	if err != nil {
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}

	// return success
	return writeMessage(function, stub, SUCCESS, "", lotterys)
}
