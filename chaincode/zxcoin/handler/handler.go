package handler

import (
	"errors"
	"fmt"
	"strconv"

	"encoding/json"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	"peersafe.com/onelottery/chaincode/zxcoin/db"
)

const (
	TRANSFER_TYPE            = 0
	FREZON_TYPE              = 1
	LOTTERY_CREATE_COST_TYPE = 2
	LOTTERY_MODIFY_COST_TYPE = 3
	TRANSFER_COST_TYPE       = 4
	REFUND_TYPE              = 5
	CONSUME_TYPE             = 6
)

var normalFee NormalFee

// issue ZxCoin
func Init(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	var err error
	var request ZxCoinInitRequest

	zxcoin := db.NewZxCoin(stub)

	if len(args) != 2 {
		err = errors.New("Incorrect number of arguments. Expecting 2")
		return returnFailed(stub, function, err)
	}

	// Get args from json
	if err = request.DecodeZxCoinInitRequestJson(args[1]); err != nil {
		return returnFailed(stub, function, err)
	}

	normalFee = NormalFee{
		CreateLotteryFee: request.DeployLotteryCost,
		ModefyLotteryFee: request.ModifyLotteryCost,
		TransferFee:      request.TransferCost,
	}

	trancferCost := strconv.FormatInt(int64(request.TransferCost), 10)
	deployLotteryCost := strconv.FormatInt(int64(request.DeployLotteryCost), 10)
	modefyLotteryCost := strconv.FormatInt(int64(request.ModifyLotteryCost), 10)
	historyLotteryKeepTime := strconv.FormatInt(int64(request.HistoryLotteryKeepTime), 10)
	// write TransferCost to ledger
	if err = stub.PutState(TRANSFER_COST, []byte(trancferCost)); err != nil {
		err = errors.New(fmt.Sprintf("put state TRANSFER_COST err : %s", err.Error()))
		return returnFailed(stub, function, err)
	}

	// write LOTTERY_CREATE_COST to ledger
	if err = stub.PutState(LOTTERY_CREATE_COST, []byte(deployLotteryCost)); err != nil {
		err = errors.New(fmt.Sprintf("put state LOTTERY_CREATE_COST err : %s", err.Error()))
		return returnFailed(stub, function, err)
	}

	// write LOTTERY_MODIFY_COST to ledger
	if err = stub.PutState(LOTTERY_MODIFY_COST, []byte(modefyLotteryCost)); err != nil {
		err = errors.New(fmt.Sprintf("put state LOTTERY_MODIFY_COST err : %s", err.Error()))
		return returnFailed(stub, function, err)
	}

	// write HISTORY_LOTTERY_KEEP_TIME to ledger
	if err = stub.PutState(HISTORY_LOTTERY_KEEP_TIME, []byte(historyLotteryKeepTime)); err != nil {
		err = errors.New(fmt.Sprintf("put state HISTORY_LOTTERY_KEEP_TIME err : %s", err.Error()))
		return returnFailed(stub, function, err)
	}

	// save admin cert
	adminCert, err := stub.GetCallerCertificate()
	if err != nil {
		myLogger.Debug("Failed getting metadata")
		err = errors.New("Failed getting metadata.")
		return returnFailed(stub, function, err)
	}
	err = stub.PutState(db.ZXCOIN_ADMIN, adminCert)
	if err != nil {
		myLogger.Debug("Failed save adminCert")
		err = errors.New("Failed save adminCert")
		return returnFailed(stub, function, err)
	}

	//check table exist ,it exist return succes
	table, err := stub.GetTable(db.ZxCoinTableName)
	if table != nil && err == nil {
		myLogger.Debug("ReInit ZxCoin Chaincode...done")
		return encodeZxCoinResponseJson(0, nil, function, nil, stub, false)
	}

	// Create table ZxCoin
	if err = zxcoin.CreateTable(); err != nil {
		return returnFailed(stub, function, err)
	}

	// get block height
	var height uint64
	if height, err = stub.GetBlockHight(); err != nil {
		err = errors.New(fmt.Sprintf("get block height err, err num : %s", err.Error()))
		return returnFailed(stub, function, err)
	}

	// Write init user record to db
	owner := db.ZxCoinTable{
		Owner:           args[0],
		Name:            request.AdminUserId,
		Balance:         request.InitAmount,
		Reserved:        0,
		TxnIDs:          stub.GetTxID() + " ",
		BlockHeight:     height,
		PrevBlockHeight: height,
	}
	if err = zxcoin.AddZxCoinUser(owner); err != nil {
		return returnFailed(stub, function, err)
	}

	myLogger.Debug("Init ZxCoin Chaincode...done")

	return encodeZxCoinResponseJson(0, nil, function, nil, stub, false)
}

func checkFee(fee uint64, feeType uint32) error {
	var needFee uint64
	if feeType == 0 { // normal transfer
		needFee = normalFee.TransferFee
	} else if feeType == 2 {
		needFee = normalFee.CreateLotteryFee
	} else if feeType == 3 {
		needFee = normalFee.ModefyLotteryFee
	}
	if fee < needFee {
		return errors.New("fee is little than the most little fee")
	}
	return nil
}

// ZxCoin trancfer accounts
func ZxCoinTransfer(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	var err error
	if len(args) != 2 {
		err = errors.New("Incorrect number of arguments. Expecting 2.")
		myLogger.Debug(err.Error())
		return encodeZxCoinResponseJson(1, err, function, nil, stub, true)
	}

	var request ZxCoinTransferRequest
	var data ZxCoinTransferResponse

	zxcoin := db.NewZxCoin(stub)

	if normalFee.TransferFee == 0 {
		if err = getNormalFee(normalFee, stub); err != nil {
			myLogger.Debug(err.Error())
			return encodeZxCoinResponseJson(1, err, function, nil, stub, true)
		}
	}

	// Get args from json
	if err = request.DecodeZxCoinTransferRequestJson(args[1]); err != nil {
		myLogger.Debug(err.Error())
		return encodeZxCoinResponseJson(1, err, function, nil, stub, true)
	}

	data.Amount = request.Amount
	data.OwnUserId = request.UserId
	data.Owner = args[0]
	data.Oppisite = request.UserCertTo
	data.Fee = request.Fee
	data.Extras = request.Extras

	err = checkFee(request.Fee, request.Type)
	if err != nil {
		return encodeZxCoinResponseJson(1, err, function, data, stub, true)
	}

	// Get owner record
	var owner *db.ZxCoinTable
	if owner, err = zxcoin.GetZxCoinUserByHash(args[0]); err != nil {
		myLogger.Debug(err.Error())
		return encodeZxCoinResponseJson(1, err, function, data, stub, true)
	}
	if owner.Name == "" {
		owner.Name = request.UserId
	}

	//	 ignore transfer to myself
	if request.UserCertTo == args[0] && request.Type == 0 { // normal transfer
		err = errors.New("can not trancfer to myself")
		myLogger.Debug(err.Error())
		return encodeZxCoinResponseJson(1, err, function, data, stub, true)
	}

	// Check owner balance if more than transfer amount
	if owner.Balance < (request.Amount + request.Fee) { // normal transfer
		err = errors.New("owner balance little than amount+cost")
		myLogger.Debug(err.Error())
		return encodeZxCoinResponseJson(1, err, function, data, stub, true)
	}

	// get block height
	var height uint64
	if height, err = stub.GetBlockHight(); err != nil {
		err = errors.New(fmt.Sprintf("get block height err, err num : %s", err.Error()))
		myLogger.Debug(err.Error())
		return encodeZxCoinResponseJson(1, err, function, data, stub, false)
	}
	if height < owner.BlockHeight {
		err = errors.New("current block height little than that in db in owner record.")
		myLogger.Debug(err.Error())
		return encodeZxCoinResponseJson(1, err, function, data, stub, true)
	}

	// check if need to update owner block height
	owner.Balance = owner.Balance - request.Amount - request.Fee
	if height > owner.BlockHeight {
		if owner.BlockHeight != 0 {
			owner.PrevBlockHeight = owner.BlockHeight
		} else {
			owner.PrevBlockHeight = height
		}

		owner.BlockHeight = height
		owner.TxnIDs = stub.GetTxID() + " "
	} else {
		owner.TxnIDs = owner.TxnIDs + stub.GetTxID() + " "
	}
	// update owner record
	if err = zxcoin.EditZxCoinUser(*owner); err != nil {
		myLogger.Debug(err.Error())
		return encodeZxCoinResponseJson(1, err, function, data, stub, true)
	}

	if request.Type == 2 || request.Type == 3 {
		// cost fee
		myLogger.Debug("Invoke ZxCoin Chaincode ZxCoinTransfer...done")
		return encodeZxCoinResponseJson(0, nil, function, data, stub, true)
	}

	if request.UserCertTo == "" {
		err = errors.New("UserCerTo is nil...")
		myLogger.Debug(err.Error())
		return encodeZxCoinResponseJson(1, err, function, data, stub, true)
	}

	// get oppisite record
	var oppisite *db.ZxCoinTable
	oppisite, err = zxcoin.GetZxCoinUserByHash(request.UserCertTo)
	if err != nil {
		// opposite not exist, then insert
		oppisite = &db.ZxCoinTable{
			Owner:           request.UserCertTo,
			Name:            "",
			Balance:         request.Amount,
			Reserved:        0,
			TxnIDs:          stub.GetTxID() + " ",
			BlockHeight:     height,
			PrevBlockHeight: height,
		}
		if err = zxcoin.AddZxCoinUser(*oppisite); err != nil {
			myLogger.Debug(err.Error())
			return encodeZxCoinResponseJson(1, err, function, data, stub, true)
		}
	} else {
		// opposite exist, then update
		if height < oppisite.BlockHeight {
			err = errors.New("current block height little than that in db in oppisite record.")
			myLogger.Debug(err.Error())
			return encodeZxCoinResponseJson(1, err, function, data, stub, true)
		}
		oppisite.Balance = oppisite.Balance + request.Amount
		// check if need to update opposite block height
		if height > oppisite.BlockHeight {
			oppisite.PrevBlockHeight = oppisite.BlockHeight
			oppisite.BlockHeight = height
			oppisite.TxnIDs = stub.GetTxID() + " "
		} else {
			oppisite.TxnIDs = oppisite.TxnIDs + stub.GetTxID() + " "
		}
		if err = zxcoin.EditZxCoinUser(*oppisite); err != nil {
			myLogger.Debug(err.Error())
			return encodeZxCoinResponseJson(1, err, function, data, stub, true)
		}
	}

	data.OppisiteUserId = oppisite.Name
	myLogger.Debug("Invoke ZxCoin Chaincode ZxCoinTransfer...done")
	return encodeZxCoinResponseJson(0, err, function, data, stub, true)
}

// ZxCoin query the balance
func ZxCoinAccountBalance(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	var err error
	var request ZxCoinQueryBalanceRequest

	zxcoin := db.NewZxCoin(stub)

	if len(args) != 2 {
		err = errors.New("Incorrect number of arguments. Expecting 2.")
		myLogger.Debug(err.Error())
		return encodeZxCoinResponseJson(1, err, function, nil, stub, true)
	}

	// Get args from json
	if err = request.DecodeZxCoinQueryBalanceRequestJson(args[1]); err != nil {
		myLogger.Debug(err.Error())
		return encodeZxCoinResponseJson(1, err, function, nil, stub, true)
	}

	// Get user record
	var data *db.ZxCoinTable
	if data, err = zxcoin.GetZxCoinUserByHash(request.UserCert); err != nil {
		myLogger.Debug(err.Error())
		return encodeZxCoinResponseJson(1, err, function, nil, stub, true)
	}

	myLogger.Debug("Query ZxCoin Chaincode ZxCoinAccountBalance...done")

	return encodeZxCoinResponseJson(0, nil, function, data, stub, true)
}

// ZxCoin Frezon
func ZxCoinFrezon(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	var err error
	var request ZxCoinFrezonRequest

	if len(args) != 2 {
		err = errors.New("Incorrect number of arguments. Expecting 2.")
		myLogger.Debug(err.Error())
		return encodeZxCoinResponseJson(1, err, function, nil, stub, true)
	}

	// Get args from json
	if err = request.DecodeZxCoinFrezonRequestJson(args[1]); err != nil {
		myLogger.Debug(err.Error())
		return encodeZxCoinResponseJson(1, err, function, nil, stub, true)
	}

	if request.Amount >= 0 {
		err = reserveBalance(stub, request.UserId, args[0], uint64(request.Amount), false)
	} else {
		err = reserveBalance(stub, request.UserId, args[0], uint64(-request.Amount), true)
	}
	if err != nil {
		myLogger.Debug(err.Error())
		return encodeZxCoinResponseJson(1, err, function, nil, stub, true)
	}

	data := ZxCoinFrezonResponse{
		Amount: request.Amount,
		Type:   request.Type,
	}

	myLogger.Debug("Invoke ZxCoin Chaincode ZxCoinFrezon...done")

	return encodeZxCoinResponseJson(0, err, function, data, stub, true)
}

// ZxCoin Lottery Refund
func ZxCoinLotteryRefund(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	var err error
	var request ZxCoinLotteryRefundRequest

	zxcoin := db.NewZxCoin(stub)

	if len(args) != 2 {
		err = errors.New("Incorrect number of arguments. Expecting 2.")
		myLogger.Debug(err.Error())
		return encodeZxCoinResponseJson(1, err, function, nil, stub, true)
	}

	// Get args from json
	if err = request.DecodeZxCoinLotteryRefundRequestJson(args[1]); err != nil {
		myLogger.Debug(err.Error())
		return encodeZxCoinResponseJson(1, err, function, nil, stub, true)
	}

	var array []*RefundObj
	var totalAmomnt uint64
	var publisherInfo *db.ZxCoinTable

	// get lottery publisher user info
	publisherInfo, err = zxcoin.GetZxCoinUserByHash(request.Publisher)
	if err != nil {
		myLogger.Debug(err.Error())
		return encodeZxCoinResponseJson(1, err, function, nil, stub, true)
	}
	for i := 0; i < len(request.TransactionID); i++ {
		tmp := &RefundObj{
			Owner:    request.Publisher,
			Oppisite: request.Attendee[i],
			Amount:   request.Amount[i],
		}

		array = append(array, tmp)
		totalAmomnt += request.Amount[i]
	}

	myLogger.Debugf("*********************** len array = %d  *****************************\n", len(array))

	if totalAmomnt > publisherInfo.Reserved {
		err = errors.New("refund total amount is more than publisher`s reserved.")
		myLogger.Debug(err.Error())
		return encodeZxCoinResponseJson(1, err, function, nil, stub, true)
	}

	// get block height
	var height uint64
	if height, err = stub.GetBlockHight(); err != nil {
		err = errors.New(fmt.Sprintf("get block height err, err num : %s", err.Error()))
		return returnFailed(stub, function, err)
	}

	// update lottery attenee user info
	for i := 0; i < len(array); i++ {
		atteneeInfo, err := zxcoin.GetZxCoinUserByHash(request.Attendee[i])
		if err != nil {
			myLogger.Debug(err.Error())
			return encodeZxCoinResponseJson(1, err, function, nil, stub, true)
		}

		if height < atteneeInfo.BlockHeight {
			err := errors.New("current block height is small than that in db.")
			myLogger.Debug(err.Error())
			return encodeZxCoinResponseJson(1, err, function, nil, stub, true)
		}

		// check if need to update atteneeInfo block height
		if height > atteneeInfo.BlockHeight {
			atteneeInfo.PrevBlockHeight = atteneeInfo.BlockHeight
			atteneeInfo.BlockHeight = height
			atteneeInfo.TxnIDs = stub.GetTxID() + " "
		} else {
			atteneeInfo.TxnIDs = atteneeInfo.TxnIDs + stub.GetTxID() + " "
		}

		atteneeInfo.Balance = atteneeInfo.Balance + array[i].Amount
		myLogger.Debugf("*****************\n %v", atteneeInfo)

		if err = zxcoin.EditZxCoinUser(*atteneeInfo); err != nil {
			myLogger.Debug(err.Error())
			return encodeZxCoinResponseJson(1, err, function, nil, stub, true)
		}
	}

	// get lottery publisher user info
	publisherInfo, err = zxcoin.GetZxCoinUserByHash(request.Publisher)
	if err != nil {
		myLogger.Debug(err.Error())
		return encodeZxCoinResponseJson(1, err, function, nil, stub, true)
	}

	// update lottery publisher user info
	if height < publisherInfo.BlockHeight {
		err := errors.New("current block height is small than that in db.")
		myLogger.Debug(err.Error())
		return encodeZxCoinResponseJson(1, err, function, nil, stub, true)
	}

	// check if need to update atteneeInfo block height
	if height > publisherInfo.BlockHeight {
		publisherInfo.PrevBlockHeight = publisherInfo.BlockHeight
		publisherInfo.BlockHeight = height
		publisherInfo.TxnIDs = stub.GetTxID() + " "
	} else {
		publisherInfo.TxnIDs = publisherInfo.TxnIDs + stub.GetTxID() + " "
	}

	publisherInfo.Reserved -= totalAmomnt

	if err = zxcoin.EditZxCoinUser(*publisherInfo); err != nil {
		myLogger.Debug(err.Error())
		return encodeZxCoinResponseJson(1, err, function, nil, stub, true)
	}

	response := &ZxCoinLotteryRefundResponse{
		Array:       array,
		LotteryID:   request.LotteryID,
		CurrentTime: request.Time,
	}

	myLogger.Debugf("refund return value = %v\n", response)

	return encodeZxCoinResponseJson(0, err, function, response, stub, true)
}

// ZxCoin get user hash by name or get user name by hash
func ZxCoinGetUserInfo(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	var err error
	var request ZxCoinGetUserInfoRequest

	zxcoin := db.NewZxCoin(stub)

	if len(args) != 2 {
		err = errors.New("Incorrect number of arguments. Expecting 2.")
		myLogger.Debug(err.Error())
		return encodeZxCoinResponseJson(1, err, function, nil, stub, true)
	}

	// Get args from json
	if err = request.DecodeZxCoinGetUserInfoRequestJson(args[1]); err != nil {
		myLogger.Debug(err.Error())
		return encodeZxCoinResponseJson(1, err, function, nil, stub, true)
	}

	var owner *db.ZxCoinTable
	// if user id exists, get hash
	if request.UserId != "" {
		if owner, err = zxcoin.GetZxCoinUserByName(request.UserId); err != nil {
			myLogger.Debug(err.Error())
			return encodeZxCoinResponseJson(1, err, function, nil, stub, true)
		} else {
			request.Owner = owner.Owner
			return encodeZxCoinResponseJson(0, nil, function, request, stub, true)
		}
	} else {
		// if user id not exists, get user id
		if owner, err = zxcoin.GetZxCoinUserByHash(request.Owner); err != nil {
			myLogger.Debug(err.Error())
			return encodeZxCoinResponseJson(1, err, function, nil, stub, true)
		} else {
			request.UserId = owner.Name
			return encodeZxCoinResponseJson(0, nil, function, request, stub, true)
		}
	}
}

func updateBlockHeight(stub shim.ChaincodeStubInterface, owner *db.ZxCoinTable) error {
	// get block height
	height, err := stub.GetBlockHight()
	if err != nil {
		err = errors.New(fmt.Sprintf("get block height err, err num : %s", err.Error()))
		myLogger.Debug(err.Error())
		return err
	}
	if height < owner.BlockHeight {
		err = errors.New("current block height little than that in db in owner record.")
		myLogger.Debug(err.Error())
		return err
	}

	// check if need to update owner block height
	if height > owner.BlockHeight {
		if owner.BlockHeight != 0 {
			owner.PrevBlockHeight = owner.BlockHeight
		} else {
			owner.PrevBlockHeight = height
		}

		owner.BlockHeight = height
		owner.TxnIDs = stub.GetTxID() + " "
	} else {
		owner.TxnIDs = owner.TxnIDs + stub.GetTxID() + " "
	}
	return nil
}

//提现申请(客户)
func ZxCoinWithdraw(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	//判断参数匹配
	if len(args) != 1 {
		err := errors.New(function + " no fit arguments!")
		return returnFailed(stub, function, err)
	}

	//解析请求参数信息
	var request WithdrawApply
	err := json.Unmarshal([]byte(args[0]), &request)
	if err != nil {
		return returnFailed(stub, function, err)
	}

	//获取所有者Hash
	ownerHash, err := db.GetOwnerHash(stub)
	if err != nil {
		return returnFailed(stub, function, err)
	}

	err = reserveBalance(stub, request.UserId, ownerHash, request.Amount, false)
	if err != nil {
		return returnFailed(stub, function, err)
	}

	//创建提现调用句柄
	handle, err := db.GetHandle(stub, request.UserId, ownerHash)
	if err != nil {
		return returnFailed(stub, function, err)
	}

	//新建申请记录
	handle.AddInfo(&db.WithdrawInfo{
		TxId:        stub.GetTxID(),
		State:       Withdraw_pending,
		AccountInfo: request.AccountInfo,
		Amount:      request.Amount,
		ModifyTime:  request.ModifyTime,
		CreateTime:  request.ModifyTime,
	})
	userInfo := WithdrawUserInfo{UserName: handle.User, UserHash: handle.Hash}
	return encodeZxCoinResponseJson(0, nil, function, userInfo, stub, true)
}

//冻结金额
func reserveBalance(stub shim.ChaincodeStubInterface, userId, userHash string, amount uint64, bThaw bool) error {
	//获取所有者钱包信息
	zxcoin := db.NewZxCoin(stub)
	coinInfo, err := zxcoin.GetZxCoinUserByHash(userHash)
	if err != nil {
		return err
	}

	//是否为解冻金额
	if bThaw {
		if coinInfo.Reserved < amount {
			err = errors.New("The reserved is not enough!")
			return err
		}
		//解冻余额
		coinInfo.Reserved -= amount
		coinInfo.Balance += amount
	} else {
		if coinInfo.Balance < amount {
			err = errors.New("The balance is not enough!")
			return err
		}
		//冻结余额
		coinInfo.Balance -= amount
		coinInfo.Reserved += amount
	}

	if coinInfo.Name == "" {
		coinInfo.Name = userId
	}
	err = updateBlockHeight(stub, coinInfo)
	if err != nil {
		return err
	}

	err = zxcoin.EditZxCoinUser(*coinInfo)
	if err != nil {
		return err
	}
	return nil
}

//转移解冻金额
func thawBalance(stub shim.ChaincodeStubInterface, fromUserHash, toUserHash string, amount uint64) error {
	//获取所有者钱包信息以及管理员钱包信息
	zxcoin := db.NewZxCoin(stub)

	//获取转账人
	fromCoinInfo, err := zxcoin.GetZxCoinUserByHash(fromUserHash)
	if err != nil {
		return err
	}
	//检查保留金额是否足够
	if fromCoinInfo.Reserved < amount {
		return errors.New("The reserved number is less then amout!")
	}
	//获取收款人(可能是自己)
	var toCoinInfo *db.ZxCoinTable
	if fromUserHash == toUserHash {
		toCoinInfo = fromCoinInfo
	} else {
		toCoinInfo, err = zxcoin.GetZxCoinUserByHash(toUserHash)
		if err != nil {
			return err
		}
	}
	//减少保留金额
	fromCoinInfo.Reserved -= amount
	//添加余额
	toCoinInfo.Balance += amount
	//更新转账人的账户信息
	err = updateBlockHeight(stub, fromCoinInfo)
	if err != nil {
		return err
	}
	err = zxcoin.EditZxCoinUser(*fromCoinInfo)
	if err != nil {
		return err
	}
	//更新收款人的账户信息，若和转账人相同，则不更新
	if fromCoinInfo != toCoinInfo {
		err = updateBlockHeight(stub, toCoinInfo)
		if err != nil {
			return err
		}
		err = zxcoin.EditZxCoinUser(*toCoinInfo)
		if err != nil {
			return err
		}
	}
	return nil
}

//撤销提现申请(客户)
func ZxCoinWithdrawRecall(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	//判断参数匹配
	if len(args) != 1 {
		err := errors.New(function + " no fit arguments!")
		return returnFailed(stub, function, err)
	}
	//解析请求参数信息
	var request WithdrawTxInfo
	err := json.Unmarshal([]byte(args[0]), &request)
	if err != nil {
		return returnFailed(stub, function, err)
	}

	//提现表句柄获取指定交易信息
	handle, err := db.FindHandleByTxid(stub, request.TxId)
	if err != nil {
		myLogger.Warning(function, args, err)
		return returnFailed(stub, function, err)
	}

	err = handle.CheckOwnerUser()
	if err != nil {
		return returnFailed(stub, function, err)
	}

	//交易已经非申请中状态
	info, err := handle.GetInfo(request.TxId)
	if info.State != Withdraw_pending {
		err = errors.New("The transaction is not pending!Can't recall!")
		return returnFailed(stub, function, err)
	}

	err = thawBalance(stub, handle.Hash, handle.Hash, info.Amount)
	if err != nil {
		return returnFailed(stub, function, err)
	}

	//更新交易字段
	info.State = Withdraw_Recall
	info.ModifyTime = request.ModifyTime
	err = handle.UpdateInfo(info)
	if err != nil {
		return returnFailed(stub, function, err)
	}

	//发送事件
	return encodeZxCoinResponseJson(0, nil, function, request.TxId, stub, true)
}

//提款失败(管理员)
func ZxCoinWithdrawFail(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	//判断参数匹配
	if len(args) != 1 {
		err := errors.New(function + " no fit arguments!")
		return returnFailed(stub, function, err)
	}
	//判断是否为管理员
	err := db.CheckAdmin(stub)
	if err != nil {
		return returnFailed(stub, function, err)
	}
	//解析请求参数信息
	var request WithdrawRemark
	err = json.Unmarshal([]byte(args[0]), &request)
	if err != nil {
		return returnFailed(stub, function, err)
	}
	//通过交易id创建用户表句柄,并获取交易消息
	handle, err := db.FindHandleByTxid(stub, request.TxId)
	if err != nil {
		return returnFailed(stub, function, err)
	}
	info, err := handle.GetInfo(request.TxId)
	if err != nil {
		return returnFailed(stub, function, err)
	}
	if info.State != Withdraw_pending {
		err = errors.New("The state is not pending!")
		return returnFailed(stub, function, err)
	}

	err = thawBalance(stub, handle.Hash, handle.Hash, info.Amount)
	if err != nil {
		return returnFailed(stub, function, err)
	}

	//更新提现记录状态
	info.State = Withdraw_failed
	info.ModifyTime = request.ModifyTime
	info.Remark = request.Remark
	err = handle.UpdateInfo(info)
	if err != nil {
		return returnFailed(stub, function, err)
	}
	request.UserName = handle.User
	request.UserHash = handle.Hash
	return encodeZxCoinResponseJson(0, nil, function, request, stub, true)
}

//提款成功(管理员)
func ZxCoinWithdrawRemitSuccess(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	//判断参数匹配
	if len(args) != 1 {
		err := errors.New(function + " no fit arguments!")
		return returnFailed(stub, function, err)
	}
	//判断是否为管理员
	err := db.CheckAdmin(stub)
	if err != nil {
		return returnFailed(stub, function, err)
	}
	//解析请求参数信息
	var request WithdrawSuccess
	err = json.Unmarshal([]byte(args[0]), &request)
	if err != nil {
		return returnFailed(stub, function, err)
	}
	//通过交易id创建用户表句柄,并获取交易消息
	handle, err := db.FindHandleByTxid(stub, request.TxId)
	if err != nil {
		return returnFailed(stub, function, err)
	}
	info, err := handle.GetInfo(request.TxId)
	if err != nil {
		return returnFailed(stub, function, err)
	}
	if info.State != Withdraw_pending {
		err = errors.New("The state is not pending!")
		return returnFailed(stub, function, err)
	}
	//更新提现记录状态
	info.State = Withdraw_hasPaid
	info.ModifyTime = request.ModifyTime
	info.RemitOrderNumber = request.RemitOrderNumber
	err = handle.UpdateInfo(info)
	if err != nil {
		return returnFailed(stub, function, err)
	}
	request.UserName = handle.User
	request.UserHash = handle.Hash
	return encodeZxCoinResponseJson(0, nil, function, request, stub, true)
}

//打款确认(管理员&客户)
func ZxCoinWithdrawConfirm(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	//判断参数匹配
	if len(args) != 1 {
		err := errors.New(function + " no fit arguments!")
		return returnFailed(stub, function, err)
	}
	//解析请求参数信息
	var request WithdrawTxInfo
	err := json.Unmarshal([]byte(args[0]), &request)
	if err != nil {
		return returnFailed(stub, function, err)
	}

	//通过交易id创建用户表句柄,并获取交易消息
	handle, err := db.FindHandleByTxid(stub, request.TxId)
	if err != nil {
		return returnFailed(stub, function, err)
	}
	info, err := handle.GetInfo(request.TxId)
	if err != nil {
		return returnFailed(stub, function, err)
	}

	//是否为已付款状态
	if info.State != Withdraw_hasPaid {
		err = errors.New("The state is not has Paid!")
		return returnFailed(stub, function, err)
	}

	//是否是合法用户
	if err = handle.CheckOwnerUser(); err != nil {
		if db.CheckAdmin(stub) == nil {
			if request.ModifyTime-info.ModifyTime < db.AutoConfirmTime(stub) {
				err = errors.New("The user is admin but the time is less than 5 days!")
				return returnFailed(stub, function, err)
			}
		} else {
			err = errors.New("The user is not the owner of tx and not the admin!")
			return returnFailed(stub, function, err)
		}
	}

	adminHash, err := db.GetAdminHash(stub)
	if err != nil {
		return returnFailed(stub, function, err)
	}
	err = thawBalance(stub, handle.Hash, adminHash, info.Amount)
	if err != nil {
		return returnFailed(stub, function, err)
	}

	//更新提现记录状态
	info.State = Withdraw_hasConfirm
	info.ModifyTime = request.ModifyTime
	err = handle.UpdateInfo(info)
	if err != nil {
		return returnFailed(stub, function, err)
	}
	request.UserName = handle.User
	request.UserHash = handle.Hash
	return encodeZxCoinResponseJson(0, nil, function, request, stub, true)
}

//申诉(客户)
func ZxCoinWithdrawAppeal(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	//判断参数匹配
	if len(args) != 1 {
		err := errors.New(function + " no fit arguments!")
		return returnFailed(stub, function, err)
	}

	//解析请求参数信息
	var request WithdrawRemark
	err := json.Unmarshal([]byte(args[0]), &request)
	if err != nil {
		return returnFailed(stub, function, err)
	}
	//获取交易用户详情
	handle, err := db.FindHandleByTxid(stub, request.TxId)
	if err != nil {
		return returnFailed(stub, function, err)
	}
	//获取当前用户hash
	err = handle.CheckOwnerUser()
	if err != nil {
		return returnFailed(stub, function, err)
	}
	//当前提现状态为已打款
	txInfo, err := handle.GetInfo(request.TxId)
	if err != nil {
		return returnFailed(stub, function, err)
	}
	if txInfo.State != Withdraw_hasPaid {
		err = errors.New("The State is not hasPaid!")
		return returnFailed(stub, function, err)
	}
	//状态：申诉中
	txInfo.State = Withdraw_appeal
	txInfo.Remark = request.Remark
	txInfo.ModifyTime = request.ModifyTime
	err = handle.UpdateInfo(txInfo)
	if err != nil {
		return returnFailed(stub, function, err)
	}
	//发送事件
	return encodeZxCoinResponseJson(0, nil, function, request.TxId, stub, true)
}

//申诉处理(管理员)
func ZxCoinWithdrawAppealDone(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	//判断参数匹配
	if len(args) != 1 {
		err := errors.New(function + " no fit arguments!")
		return returnFailed(stub, function, err)
	}
	//解析请求参数信息
	var request WithdrawAppealDone
	err := json.Unmarshal([]byte(args[0]), &request)
	if err != nil {
		return returnFailed(stub, function, err)
	}
	//判断是否为管理员
	err = db.CheckAdmin(stub)
	if err != nil {
		return returnFailed(stub, function, err)
	}
	//获取交易用户详情
	handle, err := db.FindHandleByTxid(stub, request.TxId)
	if err != nil {
		return returnFailed(stub, function, err)
	}
	txInfo, err := handle.GetInfo(request.TxId)
	if err != nil {
		return returnFailed(stub, function, err)
	}
	//判断是否为申诉中
	if txInfo.State != Withdraw_appeal {
		err = errors.New("The state is not appeal")
		return returnFailed(stub, function, err)
	}
	//更新状态
	txInfo.ModifyTime = request.ModifyTime
	txInfo.Remark = request.Remark
	if request.Result == 1 { //申诉驳回
		adminHash, err := db.GetAdminHash(stub)
		if err != nil {
			return returnFailed(stub, function, err)
		}
		err = thawBalance(stub, handle.Hash, adminHash, txInfo.Amount)
		if err != nil {
			return returnFailed(stub, function, err)
		}

		txInfo.State = Withdraw_hasConfirm
		err = handle.UpdateInfo(txInfo)
		if err != nil {
			return returnFailed(stub, function, err)
		}
	} else { //申诉接受
		err = thawBalance(stub, handle.Hash, handle.Hash, txInfo.Amount)
		if err != nil {
			return returnFailed(stub, function, err)
		}

		txInfo.State = Withdraw_failed
		err = handle.UpdateInfo(txInfo)
		if err != nil {
			return returnFailed(stub, function, err)
		}
	}
	//发送事件
	request.UserName = handle.User
	request.UserHash = handle.Hash
	return encodeZxCoinResponseJson(0, nil, function, request, stub, true)
}

//查询概要记录
func ZxCoinWithdrawQuery(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	//判断参数匹配
	if len(args) != 1 {
		err := errors.New(function + " no fit arguments!")
		return returnFailed(stub, function, err)
	}
	userName := args[0]
	var resp []WithdrawRecordInfo
	if userName == "" {
		handle := &db.WithdrawHandle{Stub: stub}
		userList, err := handle.GetUserList()
		if err != nil {
			return returnFailed(stub, function, err)
		}
		for _, userName := range userList {
			handle.User = userName
			chanInfo, err := handle.GetInfos()
			if err != nil {
				return returnFailed(stub, function, err)
			}
			for {
				info, ok := <-chanInfo
				if !ok {
					chanInfo = nil
					break
				}
				resp = append(resp, WithdrawRecordInfo{TxId: info.TxId, State: info.State})
			}
		}
	} else {
		handle := &db.WithdrawHandle{Stub: stub, User: userName}
		if !handle.IsTableExist() {
			err := errors.New("The table of user " + userName + " is not exist!")
			return returnFailed(stub, function, err)
		}
		chanInfo, err := handle.GetInfos()
		if err != nil {
			return returnFailed(stub, function, err)
		}
		for {
			info, ok := <-chanInfo
			if !ok {
				chanInfo = nil
				break
			}
			resp = append(resp, WithdrawRecordInfo{TxId: info.TxId, State: info.State})
		}
	}
	return encodeZxCoinResponseJson(0, nil, function, resp, stub, false)
}

//查询详细记录
func ZxCoinWithdrawInfoQuery(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	//判断参数匹配
	if len(args) != 1 {
		err := errors.New(function + " no fit arguments!")
		return returnFailed(stub, function, err)
	}
	txid := args[0]
	//获取当前交易
	handle, err := db.FindHandleByTxid(stub, txid)
	if err != nil {
		return returnFailed(stub, function, err)
	}
	info, err := handle.GetInfo(txid)
	if err != nil {
		return returnFailed(stub, function, err)
	}
	var detialInfo db.WithdrawDetialInfo
	detialInfo.WithdrawInfo = *info
	detialInfo.UserHash = handle.Hash
	detialInfo.UserName = handle.User
	return encodeZxCoinResponseJson(0, nil, function, detialInfo, stub, false)
}

func ZxCoinSetConfig(stub shim.ChaincodeStubInterface, function string, args []string) ([]byte, error) {
	//判断参数匹配
	if len(args) != 2 {
		err := errors.New(function + " no fit arguments!")
		return returnFailed(stub, function, err)
	}
	var pair ConfigPair
	err := json.Unmarshal([]byte(args[1]), &pair)
	if err != nil {
		return returnFailed(stub, function, err)
	}
	//判断是否为管理员
	err = db.CheckAdmin(stub)
	if err != nil {
		return returnFailed(stub, function, err)
	}
	if pair.Key == "AutoConfirmTime" {
		if err = db.SetAutoConfirmTime(stub, pair.Val); err != nil {
			return returnFailed(stub, function, err)
		}
	}
	return nil, nil
}
