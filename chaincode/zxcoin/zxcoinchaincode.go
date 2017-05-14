package zxcoin

import (
	"os"

	"peersafe.com/onelottery/chaincode/zxcoin/handler"
	"github.com/hyperledger/fabric/core/chaincode/shim"
	"github.com/op/go-logging"
)

type ZxCoin struct {
}

func NewZxCoin() *ZxCoin {
	return &ZxCoin{}
}

var myLogger = logging.MustGetLogger("zxcoin")
var LOG_LEVEL = false

func init() {
	if LOG_LEVEL {
		format := logging.MustStringFormatter("%{time:15:04:05.000} [%{module}] %{level:.4s} : %{message}")
		backend := logging.NewLogBackend(os.Stderr, "", 0)
		backendFormatter := logging.NewBackendFormatter(backend, format)
		logging.SetBackend(backendFormatter).SetLevel(logging.DEBUG, "zxcoin")
	}
}

//issue ZxCoin
func (t *ZxCoin) ZxCoinInit(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	myLogger.Debug("Init ZxCoin Chaincode...")
	myLogger.Debug(args)

	return handler.Init(stub, function, args)
}

//ZxCoin trancfer accounts(fromBalance to toBalance)
func (t *ZxCoin) ZxCoinTransfer(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	myLogger.Debugf("Invoke [%s]", function)
	myLogger.Debug(args)

	return handler.ZxCoinTransfer(stub, function, args)
}

//ZxCoin Frezon
func (t *ZxCoin) ZxCoinFrezon(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	myLogger.Debugf("Invoke [%s]", function)
	myLogger.Debug(args)

	return handler.ZxCoinFrezon(stub, function, args)
}

//ZxCoin query the balance
func (t *ZxCoin) ZxCoinAccountBalance(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	myLogger.Debugf("Query [%s]", function)
	myLogger.Debug(args)

	return handler.ZxCoinAccountBalance(stub, function, args)
}

// ZxCoin Lottery Refund
func (t *ZxCoin) ZxCoinLotteryRefund(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	myLogger.Debugf("Query [%s]", function)
	myLogger.Debug(args)

	return handler.ZxCoinLotteryRefund(stub, function, args)
}

// ZxCoin get user hash by name or get user name by hash
func (t *ZxCoin) ZxCoinGetUserInfo(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	myLogger.Debugf("Query [%s]", function)
	myLogger.Debug(args)

	return handler.ZxCoinGetUserInfo(stub, function, args)
}

func (t *ZxCoin) ZxCoinWithdraw(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	myLogger.Debugf("Invoke [%s]", function)
	myLogger.Debug(args)

	return handler.ZxCoinWithdraw(stub, function, args)
}

func (t *ZxCoin) ZxCoinWithdrawRecall(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	myLogger.Debugf("Invoke [%s]", function)
	myLogger.Debug(args)

	return handler.ZxCoinWithdrawRecall(stub, function, args)
}

func (t *ZxCoin) ZxCoinWithdrawFail(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	myLogger.Debugf("Invoke [%s]", function)
	myLogger.Debug(args)

	return handler.ZxCoinWithdrawFail(stub, function, args)
}

func (t *ZxCoin) ZxCoinWithdrawRemitSuccess(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	myLogger.Debugf("Invoke [%s]", function)
	myLogger.Debug(args)

	return handler.ZxCoinWithdrawRemitSuccess(stub, function, args)
}

func (t *ZxCoin) ZxCoinWithdrawConfirm(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	myLogger.Debugf("Invoke [%s]", function)
	myLogger.Debug(args)

	return handler.ZxCoinWithdrawConfirm(stub, function, args)
}

func (t *ZxCoin) ZxCoinWithdrawAppeal(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	myLogger.Debugf("Invoke [%s]", function)
	myLogger.Debug(args)

	return handler.ZxCoinWithdrawAppeal(stub, function, args)
}

func (t *ZxCoin) ZxCoinWithdrawAppealDone(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	myLogger.Debugf("Invoke [%s]", function)
	myLogger.Debug(args)

	return handler.ZxCoinWithdrawAppealDone(stub, function, args)
}

func (t *ZxCoin) ZxCoinWithdrawQuery(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	myLogger.Debugf("Query [%s]", function)
	myLogger.Debug(args)

	return handler.ZxCoinWithdrawQuery(stub, function, args)
}

func (t *ZxCoin) ZxCoinWithdrawInfoQuery(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	myLogger.Debugf("Query [%s]", function)
	myLogger.Debug(args)

	return handler.ZxCoinWithdrawInfoQuery(stub, function, args)
}

func (t *ZxCoin) ZxCoinSetConfig(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	myLogger.Debugf("Query [%s]", function)
	myLogger.Debug(args)

	return handler.ZxCoinSetConfig(stub, function, args)
}