package handler

import (
	"bytes"
	"encoding/json"
	"errors"
	"os"
	"strconv"
	"strings"
	"sync"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	"github.com/op/go-logging"
)

const (
	ONECHAIN_ADMIN            = "onechain_admin"
	TRANSFER_COST             = "transfer_cost"
	LOTTERY_CREATE_COST       = "lottery_create_cost"
	LOTTERY_MODIFY_COST       = "lottery_modify_cost"
	CHAINCODE_ID              = "chaincode_id"
	HISTORY_LOTTERY_KEEP_TIME = "history_lottery_keep_time"

	SUCCESS            = 0
	FAILED             = 1
	BALANCE_NOT_ENOUGH = 10

	// lottery state
	// 0未开始 1进行中 2可开奖 3开奖中 4已开奖 5能退款 6退款中 7已退款 8失败(无人投注，到期关闭)
	LOTTERY_STATE_NOT_START      = 0
	LOTTERY_STATE_IN_PROCESS     = 1
	LOTTERY_STATE_CAN_OPENREWARD = 2
	LOTTERY_STATE_IN_OPENREWARD  = 3
	LOTTERY_STATE_HAD_OPENREWARD = 4
	LOTTERY_STATE_CAN_REFUND     = 5
	LOTTERY_STATE_IN_REFUND      = 6
	LOTTERY_STATE_HAD_REFUND     = 7
	LOTTERY_STATE_FAILED         = 8

	// zxcoin type
	TRANSFER_TYPE            = 0
	CONSUME_TYPE             = 1
	LOTTERY_CREATE_COST_TYPE = 2
	LOTTERY_MODIFY_COST_TYPE = 3
)

type lotteryTime struct {
	StartTime uint64
	CloseTime uint64
}

type lotteryStart struct {
	closeTime uint64
	StartFlag bool
}

var (
	logger       = logging.MustGetLogger("onechain.handler")
	lotteryMap   map[string]*lotteryTime
	lotteryMutex sync.RWMutex

	lotteryHistoryMap   map[string]uint64
	lotteryHistoryMutex sync.RWMutex

	lotteryMapStart      map[string]*lotteryStart
	lotteryMapStartMutex sync.RWMutex

	lotteryMapRefund   map[string]int
	lotteryRefundMutex sync.RWMutex

	chaincodeID string

	historyLotteryKeepTime uint64
)

func init() {
	// log format
	format := logging.MustStringFormatter("%{shortfile} %{time:15:04:05.000} [%{module}] %{level:.4s} : %{message}")
	backend := logging.NewLogBackend(os.Stderr, "", 0)
	backendFormatter := logging.NewBackendFormatter(backend, format)
	logging.SetBackend(backendFormatter).SetLevel(logging.DEBUG, "onechain.handler")
	// lottery chan init
	lotteryMap = make(map[string]*lotteryTime)
	lotteryHistoryMap = make(map[string]uint64)
	lotteryMapStart = make(map[string]*lotteryStart)
	lotteryMapRefund = make(map[string]int)

}

func lotteryMapWrite(key string, value *lotteryTime) {
	lotteryMutex.Lock()
	lotteryMap[key] = value
	lotteryMutex.Unlock()
}

func lotteryMapDelete(key string) {
	lotteryMutex.Lock()
	delete(lotteryMap, key)
	lotteryMutex.Unlock()
}

func lotteryHistoryMapWrite(key string, value uint64) {
	lotteryMutex.Lock()
	lotteryHistoryMap[key] = value
	lotteryMutex.Unlock()
}

func lotteryHistoryMapDelete(key string) {
	lotteryMutex.Lock()
	delete(lotteryHistoryMap, key)
	lotteryMutex.Unlock()
}

func lotteryMapStartWrite(key string, value *lotteryStart) {
	lotteryMapStartMutex.Lock()
	lotteryMapStart[key] = value
	lotteryMapStartMutex.Unlock()
}

func lotteryMapStartDelete(key string) {
	lotteryMapStartMutex.Lock()
	delete(lotteryMapStart, key)
	lotteryMapStartMutex.Unlock()
}

func lotteryMapRefundWrite(key string, value int) {
	lotteryRefundMutex.Lock()
	lotteryMapRefund[key] = value
	lotteryRefundMutex.Unlock()
}

func lotteryMapRefundDelete(key string) {
	lotteryRefundMutex.Lock()
	delete(lotteryMapRefund, key)
	lotteryRefundMutex.Unlock()
}

//func lotteryMapHistoryWrite(key string, value uint64) {
//	lotteryRefundMutex.Lock()
//	lotteryHistoryMap[key] = value
//	lotteryRefundMutex.Unlock()
//}

//func lotteryMapHistoryDelete(key string) {
//	lotteryRefundMutex.Lock()
//	delete(lotteryHistoryMap, key)
//	lotteryRefundMutex.Unlock()
//}

func check(stub shim.ChaincodeStubInterface, args []string) error {
	if err := checkArgs(args); err != nil {
		return err
	}
	if err := checkAdmin(stub, args[0]); err != nil {
		return err
	}
	return nil
}

// check args
func checkArgs(args []string) error {
	if len(args) != 2 {
		logger.Errorf("Args len not 2!\n")
		return errors.New("Args len not 2")
	}
	return nil
}

// check admin
func checkAdmin(stub shim.ChaincodeStubInterface, adminCert string) error {
	// get admin cert form db
	data, err := stub.GetState(ONECHAIN_ADMIN)
	if err != nil {
		logger.Errorf("Get admin cert err:%v\n", err)
		return err
	}

	if !bytes.Equal(data, []byte(adminCert)) {
		return errors.New("Permission denied.")
	}
	return nil
}

func isAdmin(stub shim.ChaincodeStubInterface, adminCert string) bool {
	if err := checkAdmin(stub, adminCert); err != nil {
		return false
	}
	return true
}

// get transfer cost
func getTransferCost(stub shim.ChaincodeStubInterface) uint64 {
	return getDataFromLedger(stub, TRANSFER_COST)
}

// get lottery cost
func getLotteryCost(stub shim.ChaincodeStubInterface, TYPE string) uint64 {
	return getDataFromLedger(stub, TYPE)
}

// get delete history lottery time
func getDeleteHistoryLotterytime(stub shim.ChaincodeStubInterface, TYPE string) uint64 {
	return getDataFromLedger(stub, TYPE)
}

func getDataFromLedger(stub shim.ChaincodeStubInterface, key string) uint64 {
	data, err := stub.GetState(key)
	if err != nil {
		logger.Errorf("stub.GetState err:%v\n", err)
	}
	result, err := strconv.ParseUint(string(data), 10, 64)
	if err != nil {
		logger.Errorf("strconv.ParseUint err:%v\n", err)
	}
	logger.Debugf("[getDataFromLedger] key=%v,value=%v\n", key, result)
	return result
}

// Uniform return results
type result struct {
	Code    int         `json:"code"`
	Message string      `json:"message"`
	Data    interface{} `json:"data"`
	// RefundData []string    `json:"refundData,omitempty"`
}

func writeMessage(function string, stub shim.ChaincodeStubInterface, code int, message string, data interface{}) ([]byte, error) {
	// find closed lottery
	//var refundData []string
	// logger.Debugf("len lotteryMapRefund = %v\n", len(lotteryMapRefund))
	// if strings.Contains(function, "invoke") {
	// 	for k, _ := range lotteryMapRefund {
	// 		logger.Debugf("lottery id in lotteryMapRefund =%v\n", k)
	// 		_, err := doRefund(stub, k, getTxTime(stub))
	// 		if err != nil {
	// 			logger.Errorf("doRefund err=%v\n", err)
	// 		} else {
	// 			refundData = append(refundData, k)
	// 		}
	// 	}
	// 	logger.Debugf("After range, len lotteryMapRefund = %v\n", len(lotteryMapRefund))
	// }

	// recover function name
	function = parseFunction(function)

	var res []byte
	var err error
	var returnError error
	if message != "" {
		returnError = errors.New(message)
	}
	if function == "oneLotteryRefund" && data != nil {
		res = data.([]byte)
	}

	if res == nil {
		// json return data
		res, err = json.Marshal(&result{
			Code:    code,
			Message: message,
			Data:    data,
			// RefundData: refundData,
		})
		if err != nil {
			logger.Errorf("writeMessage json marshal err:%v\n", err)
			return res, err
		}
	}

	// set event
	//if len(refundData) > 0 {
	//	function += "||Refund"
	//}
	err = stub.SetEvent(function, res)
	if err != nil {
		logger.Errorf("writeMessage SetEvent err:%v\n", err)
	}
	// else {
	// 	for _, v := range refundData {
	// 		// delete lottery map
	// 		lotteryMapDelete(v)
	// 		// delete lottery refund map
	// 		lotteryMapRefundDelete(v)
	// 	}
	// }
	return res, returnError
}

func parseFunction(function string) string {
	if strings.Contains(strings.ToLower(function), "init") {
		return function
	}
	return function[8:]
}

func getTxTime(stub shim.ChaincodeStubInterface) uint64 {
	timestamp, err := stub.GetTxTimestamp()
	if err != nil {
		return 0
	}
	return uint64(timestamp.Seconds)
}
