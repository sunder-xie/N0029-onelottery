package handler

import (
	"github.com/hyperledger/fabric/core/chaincode/shim"
	"peersafe.com/onelottery/chaincode/onechain/db"
)

type LotteryHistory struct {
}

func NewLotteryHistory() *LotteryHistory {
	return &LotteryHistory{}
}

func (l *LotteryHistory) GetLotterys(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	// check args
	if err := checkArgs(args); err != nil {
		return writeMessage(function, stub, 1, err.Error(), nil)
	}

	// parse json
	lottery := db.NewLotteryHistory(stub)
	err := lottery.Unmarshal(args[1])
	if err != nil {
		logger.Errorf("[EditLotteryHistory] Unmarshal error=%v\n", err)
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}

	if lottery.TxnID != "" {
		// get db
		lotterys, err := lottery.GetOneByID(lottery.TxnID)
		if err != nil {
			return writeMessage(function, stub, FAILED, err.Error(), nil)
		}

		// return success
		return writeMessage(function, stub, SUCCESS, "", lotterys)
	}

	// get db
	lotterys, err := lottery.GetAll()
	// lotterys, err := lottery.GetOneByID(args[1])
	if err != nil {
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}

	// return success
	return writeMessage(function, stub, SUCCESS, "", lotterys)
}

func (l *LotteryHistory) Delete(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	// check args
	if err := checkArgs(args); err != nil {
		return writeMessage(function, stub, 1, err.Error(), nil)
	}

	logger.Debug("move lottery history to old lottery history table")

	// parse refund reqeust
	request := &refundRequest{}
	if err := parseRefundReuest(args[1], request); err != nil {
		logger.Debugf("[Delete] parseRefundReuest err:%v\n", err)
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}
	// delete lottery history map
	lotteryHistoryMapDelete(request.LotteryID)
	logger.Debugf("request LotteryID=%v,CurrentTime=%v\n", request.LotteryID, request.CurrentTime)
	if request.LotteryID == "" {
		logger.Debugf("[HistoryLotteryDelete] lotteryID is nil\n")
		return writeMessage(function, stub, FAILED, "lotteryID is nil", nil)
	}

	lottery := db.NewLotteryHistory(stub)
	// get db
	history, err := lottery.GetOneByID(request.LotteryID)
	if err != nil {
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}

	err = history.Delete()
	if err != nil {
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}

	// return success
	return writeMessage(function, stub, SUCCESS, "", lottery.TxnID)
}
