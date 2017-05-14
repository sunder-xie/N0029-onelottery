package handler

import (
	"encoding/json"
	"errors"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	"peersafe.com/onelottery/chaincode/onechain/db"
	"peersafe.com/onelottery/chaincode/zxcoin"
)

type Lottery struct {
}

func NewLottery() *Lottery {
	return &Lottery{}
}

func (l *Lottery) AddLottery(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	// check args
	if err := checkArgs(args); err != nil {
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}

	// parse json
	lottery := db.NewLottery(stub)
	err := lottery.Unmarshal(args[1])
	if err != nil {
		logger.Errorf("[AddLottery] unmarshal error=%v\n", err)
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}
	logger.Debugf("get lottery data=%v\n", *lottery)

	if lottery.CloseTime <= lottery.CreateTime {
		logger.Errorf("[AddLottery] close time can not less than create time\n")
		return writeMessage(function, stub, FAILED, " close  time can not less than create time", nil)
	}
	if lottery.RuleID == "" || lottery.RuleType == "" {
		logger.Debugf("[AddLottery] ruleid or rule type is nil\n")
		return writeMessage(function, stub, FAILED, "ruleid or rule type is nil", nil)
	}

	// check rule
	rule, err := checkRule(stub, lottery.RuleID, lottery.RuleType)
	if err != nil {
		logger.Debugf("[AddLottery] check rule error %v\n", err)
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}

	// zxcoin get balance
	userBalance, err := getZxCoinBalance(stub, args[0])
	if err != nil {
		logger.Errorf("[AddLottery] getZxCoinBalance error : %v\n", err)
		return writeMessage(function, stub, FAILED, "get balance err:"+err.Error(), nil)
	}

	// get lottery cost
	lotteryCost := getLotteryCost(stub, LOTTERY_CREATE_COST)
	if userBalance < lotteryCost {
		logger.Error("[AddLottery] user balance not enough\n")
		return writeMessage(function, stub, FAILED, "user balance not enough", nil)
	}

	if lottery.Fee < lotteryCost {
		logger.Error("[AddLottery] deploy lottery fee not enough\n")
		return writeMessage(function, stub, FAILED, "lottery fee not enough", nil)
	}

	// zxcoin deducion cost
	if err := doZxCoinCost(stub, lottery.Fee, lottery.CreateTime, args[0], "创建活动扣费", lottery.PublisherName, uint32(LOTTERY_CREATE_COST_TYPE)); err != nil {
		logger.Errorf("[AddLottery] doZxCoinCost error: %v\n", err)
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}

	// insert to db
	lottery.PublisherHash = args[0]
	lottery.Status = LOTTERY_STATE_NOT_START
	err = lottery.Add()
	if err != nil {
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}
	// update rule
	currentRule, _ := rule.(*db.RulePrize)
	if !currentRule.IsUsed {
		if err := updateRule(lottery.RuleType, rule); err != nil {
			return writeMessage(function, stub, FAILED, err.Error(), nil)
		}
	}

	// add to lottery map
	//		lotteryMapWrite(lottery.TxnID, &lotteryTime{lottery.StartTime, lottery.CloseTime})

	// return success
	return writeMessage(function, stub, SUCCESS, "", &db.SimpleLottery{
		TxnID:         lottery.TxnID,
		NewTxnID:      lottery.NewTxnID,
		Name:          lottery.Name,
		Version:       lottery.Version,
		PublisherName: lottery.PublisherName,
		PublisherHash: lottery.PublisherHash,
	})
}

type lotteryResult struct {
	LotteryID string `json:"lotteryID"`
	Owner     string `json:"owner"`
}

func checkRule(stub shim.ChaincodeStubInterface, ruleId string, ruleType string) (interface{}, error) {
	switch ruleType {
	case "PrizeRule":
		rule, err := db.NewPrizeRule(stub).GetOneByID(ruleId)
		if err != nil {
			logger.Debugf("get rule error %v\n", err)
			return nil, err
		}
		if rule == nil {
			return nil, errors.New("Rule not exist.")
		}
		return rule, nil
	case "BallRule":
		rule, err := db.NewBallRule(stub).GetOneByID(ruleId)
		if err != nil {
			logger.Debugf("get rule error %v\n", err)
			return nil, err
		}
		if rule == nil {
			return nil, errors.New("Rule not exist.")
		}
		return rule, nil
	default:
		return nil, errors.New("Rule type not support.")
	}
}

func updateRule(ruleType string, object interface{}) error {
	switch ruleType {
	case "PrizeRule":
		prize := object.(*db.RulePrize)
		if !prize.IsUsed {
			prize.IsUsed = true
			err := prize.Update()
			if err != nil {
				logger.Debugf("update Rule error %v\n", err)
				return err
			}
		}
	case "BallRule":
		ball := object.(*db.RuleBall)
		if !ball.IsUsed {
			ball.IsUsed = true
			err := ball.Update()
			if err != nil {
				logger.Debugf("update Rule error %v\n", err)
				return err
			}
		}
	default:
		return nil
	}
	return nil
}

func doZxCoinCost(stub shim.ChaincodeStubInterface, lotteryCost, createTime uint64, cert, remark, userID string, types uint32) error {
	// compose request
	costRequest := &struct {
		Amount uint64 `json:"Amount"`
		Time   uint64 `json:"Time"`
		Fee    uint64 `json:"fee"`
		Type   uint32 `json:"Type"`
		Remark string `json:"remark"`
		UserID string `json:"userID"`
	}{
		Amount: 0,
		Time:   createTime,
		Type:   types,
		Fee:    lotteryCost,
		Remark: remark,
		UserID: userID,
	}
	data, err := json.Marshal(costRequest)
	if err != nil {
		logger.Debugf("doZxCoinCost json marsharl err %v\n", err)
		return err
	}
	// do zxcoin cost
	costResponse, err := zxcoin.NewZxCoin().ZxCoinTransfer(stub, "", []string{cert, string(data)})
	if err != nil {
		logger.Debugf("ZxCoinCosts err %v\n", err)
		return err
	}
	// parse response
	response := &struct {
		Code        int    `json:"Code"`
		CodeMessage string `json:"CodeMessage"`
	}{}
	err = json.Unmarshal(costResponse, response)
	if err != nil {
		logger.Debug("ZxCoinCosts json Unmarshal err %v\n", err)
		return err
	}
	// result check
	if response.Code != SUCCESS {
		logger.Debug("ZxCoinCosts code err %v\n", response.CodeMessage)
		return errors.New(response.CodeMessage)
	}
	return nil
}

func getZxCoinBalance(stub shim.ChaincodeStubInterface, cert string) (uint64, error) {
	// compose request
	balanceRequest := &struct {
		UserCert string `json:"UserCert"`
	}{
		UserCert: cert,
	}
	data, err := json.Marshal(balanceRequest)
	if err != nil {
		logger.Debug("getZxCoinBalance json marsharl err %v\n", err)
		return 0, err
	}
	// get reponse
	balanceResponse, err := zxcoin.NewZxCoin().ZxCoinAccountBalance(stub, "", []string{cert, string(data)})
	if err != nil {
		logger.Debug("getZxCoinBalance ZxCoinAccountBalance err %v\n", err)
		return 0, err
	}

	// parse json response
	br := &struct {
		Data struct {
			Balance         uint64 `json:"balance"`
			Reserved        uint64 `json:"reserved"`
			UserId          string `json:"userId"`
			BlockHeight     uint64 `json:"blockHeight"`
			TxnIDs          string `json:"txnIDs"`
			PrevBlockHeight uint64 `json:"prevBlockHeight"`
		}
		Code        int    `json:"Code"`
		CodeMessage string `json:"CodeMessage"`
	}{}
	err = json.Unmarshal(balanceResponse, br)
	if err != nil {
		logger.Debug("getZxCoinBalance json Unmarshal err %v\n", err)
		return 0, err
	}
	// result check
	if br.Code != SUCCESS {
		logger.Debug("getZxCoinBalance code err %v\n", br.CodeMessage)
		return 0, errors.New(br.CodeMessage)
	}
	return br.Data.Balance, nil
}

func (l *Lottery) DeleteLottery(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	// check args and admin
	if err := checkArgs(args); err != nil {
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}

	// parse json
	lottery := db.NewLottery(stub)
	err := lottery.Unmarshal(args[1])
	if err != nil {
		logger.Errorf("[EditLottery] Unmarshal error=%v\n", err)
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}
	if lottery.TxnID == "" || lottery.UpdateTime == 0 {
		logger.Errorf("[EditLottery] parameter error\n")
		return writeMessage(function, stub, FAILED, "parameter error", nil)
	}

	//	if _, exist := lotteryMapStart[lottery.TxnID]; exist {
	//		logger.Errorf("[EditLottery] lottery had started\n")
	//		return writeMessage(function, stub, FAILED, "lottery had started", nil)
	//	}

	// get current lottery
	currentLottery, err := lottery.GetOneByID(lottery.TxnID)
	if err != nil {
		logger.Errorf("DeleteLottery Unmarshal error=%v\n", err)
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}
	if currentLottery == nil {
		return writeMessage(function, stub, SUCCESS, "", lottery.TxnID)
	}
	if currentLottery.Status != LOTTERY_STATE_NOT_START {
		logger.Errorf("Lottery can not be delete\n")
		return writeMessage(function, stub, FAILED, "Lottery can not be delete", nil)
	}
	// is PublisherHash or not
	if !isAdmin(stub, args[0]) {
		if currentLottery.PublisherHash != args[0] {
			logger.Errorf("Permission denied.\n")
			return writeMessage(function, stub, FAILED, "Permission denied.", nil)
		}
	}

	if lottery.UpdateTime >= currentLottery.StartTime {
		logger.Errorf("Lottery activity had started.\n")
		return writeMessage(function, stub, FAILED, "Lottery activity had started.", nil)

	}
	if lottery.UpdateTime >= currentLottery.CloseTime {
		logger.Errorf("Lottery activity had been closed.\n")
		return writeMessage(function, stub, FAILED, "Lottery activity had been closed.", nil)

	}

	if currentLottery.CountTotal > 0 {
		logger.Errorf("DeleteLottery lottery had been used\n")
		return writeMessage(function, stub, FAILED, "lottery had been used", nil)
	}

	// delete db
	err = lottery.Delete(true)
	if err != nil {
		logger.Errorf("DeleteLottery err=%v\n", err)
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}

	//	lotteryMapStartDelete(lottery.TxnID)
	//	lotteryMapRefundDelete(lottery.TxnID)
	// return success
	return writeMessage(function, stub, SUCCESS, "", lottery.TxnID)
}

func (l *Lottery) EditLottery(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	// check args
	if err := checkArgs(args); err != nil {
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}

	// parse json
	lottery := db.NewLottery(stub)
	err := lottery.Unmarshal(args[1])
	if err != nil {
		logger.Errorf("[EditLottery] Unmarshal error=%v\n", err)
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}

	//	if _, exist := lotteryMapStart[lottery.TxnID]; exist {
	//		logger.Errorf("[EditLottery] lottery had started\n")
	//		return writeMessage(function, stub, FAILED, "lottery had started", nil)
	//	}

	currentLottery, err := lottery.GetOneByID(lottery.TxnID)
	if err != nil {
		logger.Errorf("[EditLottery] getlottery error=%v\n", err)
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}
	if currentLottery == nil {
		logger.Errorf("Lottery not exist\n")
		return writeMessage(function, stub, FAILED, "Lottery not exist", nil)
	}

	// update num pool
	if currentLottery.MaxAttendeeCnt != lottery.MaxAttendeeCnt {
		// generate lottery numbers
		if lottery.RuleType == "PrizeRule" {
			logger.Debug(currentLottery.TxnID)
			if err := db.GenerateNumbers(currentLottery.TxnID, int(lottery.MaxAttendeeCnt), stub); err != nil {
				logger.Errorf("EditLottery Generate lottery numbers failed, %v\n", err)
				return writeMessage(function, stub, FAILED, "EditLottery Generate lottery numbers failed", nil)
			}
		}
	}

	lottery.CreateTime = currentLottery.CreateTime
	if currentLottery.Status != LOTTERY_STATE_NOT_START {
		logger.Errorf("Lottery can not be delete\n")
		return writeMessage(function, stub, FAILED, "Lottery can not be delete", nil)
	}
	// is PublisherHash or not
	if currentLottery.PublisherHash != args[0] {
		logger.Errorf("Permission denied.\n")
		return writeMessage(function, stub, FAILED, "Permission denied.", nil)
	}

	// had start
	if lottery.UpdateTime >= currentLottery.StartTime {
		logger.Errorf("Lottery activity had started.\n")
		return writeMessage(function, stub, FAILED, "Lottery activity had started.", nil)
	}
	// had closed
	if lottery.UpdateTime >= currentLottery.CloseTime {
		logger.Errorf("Lottery activity had been closed.\n")
		return writeMessage(function, stub, FAILED, "Lottery activity had been closed.", nil)

	}
	// lottery already close.
	if currentLottery.LastCloseTime != 0 {
		logger.Errorf("Lottery activity had been closed.\n")
		return writeMessage(function, stub, FAILED, "Lottery activity had been closed.", nil)
	}
	// had been bet
	if currentLottery.CountTotal > 0 {
		logger.Errorf("Lottery had been used.\n")
		return writeMessage(function, stub, FAILED, "Lottery had been used.", nil)

	}

	// zxcoin get balance
	userBalance, err := getZxCoinBalance(stub, args[0])
	if err != nil {
		logger.Errorf("[EditLottery] getZxCoinBalance error : %v\n", err)
		return writeMessage(function, stub, FAILED, "get balance err:"+err.Error(), nil)
	}

	// get lottery cost
	lotteryCost := getLotteryCost(stub, LOTTERY_MODIFY_COST)
	if userBalance < lotteryCost {
		logger.Error("[EditLottery] user balance not enough\n")
		return writeMessage(function, stub, FAILED, "user balance not enough", nil)
	}

	if lottery.Fee < lotteryCost {
		logger.Error("[EditLottery] modefy lottery fee not enough\n")
		return writeMessage(function, stub, FAILED, "modefy lottery fee not enough", nil)
	}

	// zxcoin deducion cost
	if err := doZxCoinCost(stub, lottery.Fee, lottery.UpdateTime, args[0], "修改活动扣费", lottery.PublisherName, uint32(LOTTERY_MODIFY_COST_TYPE)); err != nil {
		logger.Errorf("[EditLottery] doZxCoinCost error: %v\n", err)
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}

	lottery.Version += 1
	lottery.NewTxnID = stub.GetTxID()
	// add by yangzhibin, fix bug in publisher hash missing after edit lottery
	lottery.PublisherHash = args[0]
	// update lottery
	err = lottery.Update()
	if err != nil {
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}

	tmpTime := &lotteryTime{}

	if lottery.StartTime != 0 {
		tmpTime.StartTime = lottery.StartTime
	}
	if lottery.CloseTime != 0 {
		tmpTime.CloseTime = lottery.CloseTime
	}
	//	lotteryMapWrite(lottery.TxnID, tmpTime)

	// return success
	return writeMessage(function, stub, SUCCESS, "", &db.SimpleLottery{
		TxnID:         lottery.TxnID,
		NewTxnID:      lottery.NewTxnID,
		Name:          lottery.Name,
		Version:       lottery.Version,
		PublisherName: lottery.PublisherName,
		PublisherHash: lottery.PublisherHash,
	})
}

func (l *Lottery) GetLotterys(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	// check args
	if err := checkArgs(args); err != nil {
		return writeMessage(function, stub, 1, err.Error(), nil)
	}

	// parse json
	lottery := db.NewLottery(stub)
	err := lottery.Unmarshal(args[1])
	if err != nil {
		logger.Errorf("[EditLottery] Unmarshal error=%v\n", err)
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}

	logger.Debugf("[GetLotterys] lotteryID=%v\n", lottery.TxnID)
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

func (l *Lottery) LotteryStart(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	// check args
	if err := checkArgs(args); err != nil {
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}

	// parse refund reqeust
	request := &refundRequest{}
	if err := parseRefundReuest(args[1], request); err != nil {
		logger.Debugf("[LotteryStart] parseRefundReuest err:%v\n", err)
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}

	logger.Debugf("request LotteryID=%v,CurrentTime=%v\n", request.LotteryID, request.CurrentTime)
	if request.LotteryID == "" {
		logger.Debugf("[LotteryStart] lotteryID is nil\n")
		return writeMessage(function, stub, FAILED, "lotteryID is nil", nil)
	}

	currentLottery, err := db.NewLottery(stub).GetOneByID(request.LotteryID)
	if err != nil {
		logger.Errorf("[LotteryStart] getlottery error=%v\n", err)
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}

	if currentLottery == nil {
		logger.Error("[LotteryStart] lottery not found")
		return writeMessage(function, stub, FAILED, "[LotteryStart] lottery not found", nil)
	}

	// when StartFlag=false invoke this interface
	tmp := &lotteryStart{
		closeTime: currentLottery.CloseTime,
		StartFlag: true,
	}
	lotteryMapStartWrite(request.LotteryID, tmp)

	currentLottery.NewTxnID = stub.GetTxID()
	currentLottery.Status = LOTTERY_STATE_IN_PROCESS
	currentLottery.UpdateTime = request.CurrentTime
	currentLottery.Version += 1

	err = currentLottery.Update()
	if err != nil {
		logger.Debugf("[LotteryStart] lottery UpdateState err=%v\n", err)
		return nil, err
	}

	lotteryMapDelete(currentLottery.TxnID)

	// return success
	return writeMessage(function, stub, SUCCESS, "", currentLottery.TxnID)
}

func (l *Lottery) Refund(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	// check args
	if err := checkArgs(args); err != nil {
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}

	// parse refund reqeust
	request := &refundRequest{}
	if err := parseRefundReuest(args[1], request); err != nil {
		logger.Debugf("[Refund] parseRefundReuest err:%v\n", err)
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}
	logger.Debugf("request LotteryID=%v,CurrentTime=%v\n", request.LotteryID, request.CurrentTime)
	if request.LotteryID == "" {
		logger.Debugf("[LotteryStart] lotteryID is nil\n")
		return writeMessage(function, stub, FAILED, "lotteryID is nil", nil)
	}

	data, err := doRefund(stub, request.LotteryID, request.CurrentTime)
	if err != nil || data == nil {
		return writeMessage(function, stub, FAILED, err.Error(), nil)
	}

	// delete lottery map
	//	lotteryMapStartDelete(request.LotteryID)
	// delete lottery refund map
	//	lotteryMapRefundDelete(request.LotteryID)
	// add lottery to history map
	lotteryHistoryMapWrite(request.LotteryID, request.CurrentTime)
	stub.DelState(request.LotteryID + "_Time")
	return data, nil
}

func doRefund(stub shim.ChaincodeStubInterface, lotteryID string, currentTime uint64) ([]byte, error) {
	// check lottery has over close time
	lottery, err := db.NewLottery(stub).GetOneByID(lotteryID)
	if err != nil {
		logger.Debugf("[Refund] get lottery err:%v\n", err)
		return nil, err
	}

	if lottery == nil {
		logger.Errorf("[Refund] lottery don't exist\n")
		return nil, errors.New("lottery don't exist")
	}

	if lottery.Status == LOTTERY_STATE_HAD_REFUND {
		logger.Errorf("[Refund] lottery had refund\n")
		return nil, errors.New("lottery had refund")
	}

	if lottery.Status == LOTTERY_STATE_HAD_OPENREWARD {
		logger.Errorf("[Refund] lottery had openreward\n")
		return nil, errors.New("lottery had openreward")
	}

	if lottery.Status == LOTTERY_STATE_FAILED {
		logger.Errorf("[Refund] lottery had openreward\n")
		return nil, errors.New("lottery had openreward")
	}

	// check lottery close time
	if currentTime < lottery.CloseTime {
		logger.Debugf("[Refund] now less than close time.\n", err)
		return nil, errors.New("now less than close time.")
	}
	// get tickets
	ticket := db.NewTicket(stub)
	tickets, err := ticket.GetTickets(lotteryID, "")
	if err != nil {
		logger.Debugf("[Refund] GetTickets err:%v\n", err)
		return nil, err
	}

	var data []byte
	if len(tickets) == 0 {
		// update lottery state
		lottery.Status = LOTTERY_STATE_FAILED
	} else {
		// update lottery state
		lottery.Status = LOTTERY_STATE_HAD_REFUND
	}

	// do refund
	ids, amounts, attendeers := getTicketInfos(tickets)

	zxCoinRefundRequest := &struct {
		TransactionID []string `json:"transactionID"`
		Amount        []uint64 `json:"amount"`
		Attendee      []string `json:"attendee"`
		Publisher     string   `json:"publisher"`
		Remark        string   `json:"remark"`
		Time          uint64   `json:"time"`
		LotteryID     string   `json:"lotteryID"`
	}{
		TransactionID: ids,
		Amount:        amounts,
		Attendee:      attendeers,
		Publisher:     lottery.PublisherHash,
		Remark:        "退款",
		Time:          currentTime,
		LotteryID:     lotteryID,
	}

	requestJson, err := json.Marshal(zxCoinRefundRequest)
	if err != nil {
		logger.Debugf("[Refund] Marshal err:%v\n", err)
		return nil, err
	}

	// request.CurrentTime, _ = util.GetTxTime(stub) // for test
	data, err = zxcoin.NewZxCoin().ZxCoinLotteryRefund(stub, "zxCoinLotteryRefund", []string{"", string(requestJson)})
	if err != nil {
		logger.Debugf("[Refund] GetTickets err:%v\n", err)
		return nil, err
	}

	//		err = db.NewTicket(stub).DeleteTable(lotteryID)
	//		if err != nil {
	//			return nil, err
	//		}

	lottery.Version += 1

	err = lottery.Delete(false)
	if err != nil {
		logger.Debugf("[Refund] lottery delete err=%v\n", err)
		return nil, err
	}

	return data, nil
}

type refundRequest struct {
	LotteryID   string `json:"lotteryID"`
	CurrentTime uint64 `json:"currentTime"`
}

func parseRefundReuest(args string, request *refundRequest) error {
	if err := json.Unmarshal([]byte(args), request); err != nil {
		logger.Debugf("[Refund] json para error %v\n", err)
		return err
	}
	return nil
}

func getTicketInfos(tickets []*db.Ticket) ([]string, []uint64, []string) {
	var ids, beters []string
	var amounts []uint64
	for _, v := range tickets {
		ids = append(ids, v.TxnID)
		beters = append(beters, v.Attendee)
		amounts = append(amounts, v.Amount)
	}
	return ids, amounts, beters
}
