package onechain

import (
	"github.com/hyperledger/fabric/core/chaincode/shim"

	"peersafe.com/onelottery/chaincode/onechain/handler"
)

func init() {
	shim.SetLoggingLevel(shim.LogError)
}

type Onechain struct {
}

func NewOnechain() *Onechain {
	return &Onechain{}
}

// Init function
func (o *Onechain) OneChainInit(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	return handler.Init(stub, function, args)
}

// Init timer task
func (o *Onechain) OneChainInitTimerTask(stub shim.ChaincodeStubInterface) {
	handler.InitTimerTask(stub)
}

// Init chaincode id and so on
func (o *Onechain) OneChainInitArgs(stub shim.ChaincodeStubInterface) {
	handler.InitArgs(stub)
}

// Init chaincode id and so on
func (o *Onechain) OneChainCheckHistoryLottery() {
	handler.CheckHistoryLottery()
}

// PrizeRule Operation
func (o *Onechain) OneLotteryPrizeRuleAdd(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	return handler.NewPrizeRule().AddRule(stub, function, args)
}
func (o *Onechain) OneLotteryPrizeRuleDelete(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	return handler.NewPrizeRule().DeleteRule(stub, function, args)
}
func (o *Onechain) OneLotteryPrizeRuleEdit(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	return handler.NewPrizeRule().EditRule(stub, function, args)
}
func (o *Onechain) OneLotteryPrizeRuleQuery(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	return handler.NewPrizeRule().GetRules(stub, function, args)
}

// BallRule Operation
/*
func (o *Onechain) OneLotteryBallRuleAdd(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	return handler.NewBallRule().AddRule(stub, function, args)
}
func (o *Onechain) OneLotteryBallRuleDelete(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	return handler.NewBallRule().DeleteRule(stub, function, args)
}
func (o *Onechain) OneLotteryBallRuleEdit(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	return handler.NewBallRule().EditRule(stub, function, args)
}
func (o *Onechain) OneLotteryBallRuleQuery(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	return handler.NewBallRule().GetRules(stub, function, args)
}
*/

// Lottery Operation
func (o *Onechain) OneLotteryAdd(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	return handler.NewLottery().AddLottery(stub, function, args)
}
func (o *Onechain) OneLotteryEdit(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	return handler.NewLottery().EditLottery(stub, function, args)
}
func (o *Onechain) OneLotteryDelete(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	return handler.NewLottery().DeleteLottery(stub, function, args)
}
func (o *Onechain) OneLotteryQuery(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	return handler.NewLottery().GetLotterys(stub, function, args)
}
func (o *Onechain) OneLotteryHistoryQuery(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	return handler.NewLotteryHistory().GetLotterys(stub, function, args)
}
func (o *Onechain) OneLotteryHistoryDelete(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	return handler.NewLotteryHistory().Delete(stub, function, args)
}
func (o *Onechain) OneLotteryOldHistoryQuery(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	return handler.NewOldLotteryHistory().GetLottery(stub, function, args)
}

// Bet Operation
func (o *Onechain) OneLotteryBet(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	return handler.NewTicket().AddTicket(stub, function, args)
}
func (o *Onechain) OneLotteryBetQuery(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	return handler.NewTicket().GetTickets(stub, function, args)
}

// Open reward
func (o *Onechain) OneLotteryBetOver(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	return handler.NewTicket().BetOver(stub, function, args)
}

// Lottery refund
func (o *Onechain) OneLotteryRefund(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	return handler.NewLottery().Refund(stub, function, args)
}

// Lottery status update
func (o *Onechain) OneLotteryStart(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	return handler.NewLottery().LotteryStart(stub, function, args)
}

// Lottery status update
func (o *Onechain) OneLotteryGetTicketNumbers(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	return handler.NewTicket().GetTicketNumbers(stub, function, args)
}

// test
func (o *Onechain) OneLotteryTest(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	return handler.NewTest().Test(stub, function, args)
}
