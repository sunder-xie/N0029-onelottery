package handler

import (
	"github.com/hyperledger/fabric/core/chaincode/shim"
	"peersafe.com/onelottery/chaincode/onechain/db"
)

type RulePrize struct {
}

func NewPrizeRule() *RulePrize {
	return &RulePrize{}
}

func (l *RulePrize) AddRule(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	// check args and admin
	if err := check(stub, args); err != nil {
		logger.Errorf("[AddRule] para err %v\n", err)
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}

	logger.Debugf("[AddRule] args=%v\n", args)
	// insert to db
	prize := db.NewPrizeRule(stub)
	// parse json
	err := prize.Unmarshal(args[1])
	if err != nil {
		logger.Errorf("[AddRule] unmarshal error=%v\n", err)
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}
	err = prize.Add()
	if err != nil {
		logger.Errorf("[AddRule] insert db err %v\n", err)
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}

	logger.Debugf("[AddRule] insert db ok\n")

	// return success
	return writeMessage(function, stub, SUCCESS, "", prize)
}

func (l *RulePrize) DeleteRule(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	// check args and admin
	if err := check(stub, args); err != nil {
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}
	// parse json
	prize := db.NewPrizeRule(stub)
	err := prize.Unmarshal(args[1])
	if err != nil {
		logger.Errorf("RulePrize DeleteRule Unmarshal error=%v\n", err)
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}
	// delete db
	err = prize.Delete()
	if err != nil {
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}

	// return success
	return writeMessage(function, stub, SUCCESS, "", prize.RuleID)
}

func (l *RulePrize) EditRule(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	// check args and admin
	if err := check(stub, args); err != nil {
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}
	// parse json
	prize := db.NewPrizeRule(stub)
	err := prize.Unmarshal(args[1])
	if err != nil {
		logger.Errorf("RulePrize EditRule Unmarshal error=%v\n", err)
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}
	// edit db
	err = prize.Update()
	if err != nil {
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}

	// return success
	return writeMessage(function, stub, SUCCESS, "", prize)
}

func (l *RulePrize) GetRules(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	// check args
	if err := checkArgs(args); err != nil {
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}

	// get db
	prize := db.NewPrizeRule(stub)
	rules, err := prize.GetAll()
	if err != nil {
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}

	// return success
	return writeMessage(function, stub, SUCCESS, "", rules)
}
