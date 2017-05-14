package handler

import (
	"encoding/json"
	"errors"
	"fmt"
	"os"
	"strconv"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	"github.com/op/go-logging"
)

var myLogger = logging.MustGetLogger("zxcoin.handler")

var LOG_LEVEL = false

func init() {
	if LOG_LEVEL {
		format := logging.MustStringFormatter("%{shortfile} %{time:15:04:05.000} [%{module}] %{level:.4s} : %{message}")
		backend := logging.NewLogBackend(os.Stderr, "", 0)
		backendFormatter := logging.NewBackendFormatter(backend, format)
		logging.SetBackend(backendFormatter).SetLevel(logging.DEBUG, "zxcoin.handler")
	}
}

func getNormalFee(normalFee NormalFee, stub shim.ChaincodeStubInterface) error {
	b, err := stub.GetState(LOTTERY_CREATE_COST)
	if err != nil {
		err = errors.New(fmt.Sprintf("get create lottery fee from ledger error : %s", err.Error()))
		return err
	}
	normalFee.CreateLotteryFee, err = strconv.ParseUint(string(b), 10, 64)
	if err != nil {
		err = errors.New(fmt.Sprintf("parse create lottery fee to int error : %s", err.Error()))
		return err
	}

	b, err = stub.GetState(LOTTERY_MODIFY_COST)
	if err != nil {
		err = errors.New(fmt.Sprintf("get modefy lottery fee from ledger error : %s", err.Error()))
		return err
	}
	normalFee.ModefyLotteryFee, err = strconv.ParseUint(string(b), 10, 64)
	if err != nil {
		err = errors.New(fmt.Sprintf("parse modefy lottery fee to int error : %s", err.Error()))
		return err
	}

	b, err = stub.GetState(TRANSFER_COST)
	if err != nil {
		err = errors.New(fmt.Sprintf("get transfer fee from ledger error : %s", err.Error()))
		return err
	}
	normalFee.TransferFee, err = strconv.ParseUint(string(b), 10, 64)
	if err != nil {
		err = errors.New(fmt.Sprintf("parse transfer fee to int error : %s", err.Error()))
		return err
	}

	return nil
}

func returnFailed(stub shim.ChaincodeStubInterface, function string, err error) ([]byte, error) {
	myLogger.Warning(stub.GetStringArgs(), err)
	return encodeZxCoinResponseJson(1, err, function, nil, stub, false)
}

func encodeZxCoinResponseJson(code int, err error, function string, data interface{}, stub shim.ChaincodeStubInterface, flag bool) ([]byte, error) {
	// transfer response
	errString := ""
	if code == 1 && err != nil {
		errString = err.Error()
	} else {
		errString = "Success"
	}
	response := ZxCoinResponse{
		Data:        data,
		Code:        code,
		CodeMessage: errString,
	}

	b, err_tmp := json.Marshal(response)

	myLogger.Debug("Marshal Response", string(b))

	if err_tmp != nil {
		myLogger.Debug("Json encode error.")
	}

	if flag {
		myLogger.Debugf("Set event  %s\n.", function)
		if err_tmp := stub.SetEvent(function, b); err_tmp != nil {
			myLogger.Errorf("set event error : %s", err_tmp.Error())
		}
	}

	return b, err
}

// parse json to ZxCoinInitRequest obj
func (this *ZxCoinInitRequest) DecodeZxCoinInitRequestJson(jsonStr string) error {
	if err := json.Unmarshal([]byte(jsonStr), this); err != nil {
		return errors.New("json decode ZxCoinInitRequestJson error")
	}

	// check json request
	if this.InitAmount == 0 {
		return errors.New(fmt.Sprintf("zxcoin init request param InitAmount invalid : %d.", this.InitAmount))
	} else if this.Time == 0 {
		return errors.New(fmt.Sprintf("zxcoin init request param Time invalid : %d.", this.Time))
	} else if this.TransferCost == 0 {
		return errors.New(fmt.Sprintf("zxcoin init request param TransferCost invalid : %d.", this.TransferCost))
	} else if this.DeployLotteryCost == 0 {
		return errors.New(fmt.Sprintf("zxcoin init request param DeployLotteryCost invalid : %d.", this.DeployLotteryCost))
	} else if this.ModifyLotteryCost == 0 {
		return errors.New(fmt.Sprintf("zxcoin init request param ModifyLotteryCost invalid : %d.", this.ModifyLotteryCost))
	} else if len(this.AdminUserId) == 0 {
		return errors.New(fmt.Sprintf("zxcoin init request param AdminUserId invalid : %s.", this.AdminUserId))
	} else if this.HistoryLotteryKeepTime == 0 {
		return errors.New(fmt.Sprintf("zxcoin init request param HistoryLotteryKeepTime invalid : %d.", this.InitAmount))
	}

	return nil
}

// parse json to ZxCoinTransferRequest obj
func (this *ZxCoinTransferRequest) DecodeZxCoinTransferRequestJson(jsonStr string) error {
	if err := json.Unmarshal([]byte(jsonStr), this); err != nil {
		return errors.New("json decode ZxCoinTransferRequest error")
	}

	// check json request
	if this.Amount == 0 && this.Type == 0 {
		return errors.New("normal transfer amount can not be 0.")
	} else if this.Fee == 0 && this.Type == 0 {
		return errors.New("normal transfer fee can not be 0.")
	} else if this.Fee == 0 && this.Amount == 0 && this.Type == 1 {
		return errors.New("amount is 0 and fee is 0 too in consume.")
	} else if this.Type > 3 {
		return errors.New("param type can bigger than 3.")
	} else if this.Time == 0 {
		return errors.New("param time can not be 0.")
	} else if this.UserId == "" {
		return errors.New("param UserId can not be nil.")
	}
	return nil
}

// parse json to ZxCoinQueryBalanceRequest obj
func (this *ZxCoinQueryBalanceRequest) DecodeZxCoinQueryBalanceRequestJson(jsonStr string) error {
	if err := json.Unmarshal([]byte(jsonStr), this); err != nil {
		return errors.New("json decode ZxCoinQueryBalanceRequest error")
	}

	// check json request
	if len(this.UserCert) == 0 {
		return errors.New(fmt.Sprintf("zxcoin query balance request param UserCert invalid : %s.", this.UserCert))
	}
	return nil
}

// parse json to ZxCoinBetRequest obj
func (this *ZxCoinBetRequest) DecodeZxCoinBetRequestJson(jsonStr string) error {
	if err := json.Unmarshal([]byte(jsonStr), this); err != nil {
		return errors.New("json decode ZxCoinBetRequest error")
	}

	// check json request
	if this.Amount == 0 {
		return errors.New(fmt.Sprintf("zxcoin bet request param UserCert Amount : %d.", this.Amount))
	}
	return nil
}

// parse json to ZxCoinFrezonRequest obj
func (this *ZxCoinFrezonRequest) DecodeZxCoinFrezonRequestJson(jsonStr string) error {
	if err := json.Unmarshal([]byte(jsonStr), this); err != nil {
		return errors.New("json decode ZxCoinFrezonRequest error")
	}

	// check json request
	if this.Amount == 0 {
		return errors.New(fmt.Sprintf("zxcoin Frezon request param Amount invalid : %d.", this.Amount))
	} else if this.Time == 0 {
		return errors.New(fmt.Sprintf("zxcoin Frezon request param Time invalid : %d.", this.Time))
	} else if this.Type > 1 {
		return errors.New(fmt.Sprintf("zxcoin Frezon request param Type invalid : %d.", this.Type))
	} else if this.UserId == "" {
		return errors.New("param UserId can not be nil.")
	}

	return nil
}

// parse json to ZxCoinFefundRequest obj
func (this *ZxCoinLotteryRefundRequest) DecodeZxCoinLotteryRefundRequestJson(jsonStr string) error {
	if err := json.Unmarshal([]byte(jsonStr), this); err != nil {
		return errors.New(fmt.Sprintf("json decode ZxCoinRefundRequest error : %s", err.Error()))
	}

	// check json request
	if this.Time == 0 {
		return errors.New(fmt.Sprintf("zxcoin refund request param Time invalid : %d.", this.Time))
	}

	return nil
}

// parse json to ZxCoinGetUserInfoRequest obj
func (this *ZxCoinGetUserInfoRequest) DecodeZxCoinGetUserInfoRequestJson(jsonStr string) error {
	if err := json.Unmarshal([]byte(jsonStr), this); err != nil {
		return errors.New("json decode ZxCoinGetUserInfoRequest error")
	}

	// check json request
	if len(this.Owner) == 0 && len(this.UserId) == 0 {
		return errors.New("zxcoin get user info request param both UserId and Owner nil")
	}

	return nil
}

// parse json to ZxCoinSetUserNameRequest obj
func (this *ZxCoinSetUserNameRequest) DecodeZxCoinSetUserNameRequestJson(jsonStr string) error {
	if err := json.Unmarshal([]byte(jsonStr), this); err != nil {
		return errors.New("json decode ZxCoinSetUserName error")
	}

	// check json request
	if len(this.Name) == 0 {
		return errors.New(fmt.Sprintf("length zxcoin transfer request param Name invalid : %s.", this.Name))
	}

	return nil
}
