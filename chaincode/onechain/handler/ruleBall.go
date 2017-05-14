package handler

/*
type RuleBall struct {
}

func NewBallRule() *RuleBall {
	return &RuleBall{}
}

func (l *RuleBall) AddRule(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	// check args and admin
	if err := check(stub, args); err != nil {
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}

	// insert to db
	ball := db.NewBallRule(stub)
	err := ball.AddRule(args[1])
	if err != nil {
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}

	// return success
	return writeMessage(function, stub, SUCCESS, "", ball.ID)
}

func (l *RuleBall) DeleteRule(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	// check args and admin
	if err := check(stub, args); err != nil {
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}
	// parse json
	ball := db.NewBallRule(stub)
	err := ball.Unmarshal(args[1])
	if err != nil {
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}
	// delete db
	err = ball.DeleteRule()
	if err != nil {
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}

	// return success
	return writeMessage(function, stub, SUCCESS, "", ball.ID)
}

func (l *RuleBall) EditRule(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	// check args and admin
	if err := check(stub, args); err != nil {
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}
	// parse json
	ball := db.NewBallRule(stub)
	err := ball.Unmarshal(args[1])
	if err != nil {
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}
	// edit db
	err = ball.EditRule()
	if err != nil {
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}

	// return success
	return writeMessage(function, stub, SUCCESS, "", ball.ID)
}

func (l *RuleBall) GetRules(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	// check args
	if err := checkArgs(args); err != nil {
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}

	// get db
	ball := db.NewBallRule(stub)
	rules, err := ball.GetRules()
	if err != nil {
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}

	// return success
	return writeMessage(function, stub, SUCCESS, "", rules)
}
*/
